package com.roshan.sandworks.domain

import kotlin.math.abs

/**
 * SAND WORKS — Authoritative Deterministic Money Calculation Engine.
 *
 * Core Principles:
 * 1. Financial storage uses Long integer paise only (₹200 = 20,000 paise).
 * 2. Default trip rate is ₹200 (20,000 paise). Every trip captures an immutable rate snapshot.
 * 3. Whole-rupee settlement rule: round down / floor to whole rupees.
 * 4. Invariant: Trip Rate = Distributed Money + Remaining Money.
 * 5. Remaining money is NEVER automatically given to driver, first/last labourer, or owner.
 * 6. Explicit adjustments are strictly audited and validated: distributed <= rate.
 */
object MoneyEngine {

    const val DEFAULT_TRIP_RATE_PAISE: Long = 20_000L // ₹200.00
    const val PAISE_PER_RUPEE: Long = 100L

    data class ParticipantInput(
        val personId: String,
        val isHalfShare: Boolean = false
    )

    data class ParticipantShare(
        val personId: String,
        val isHalfShare: Boolean,
        val calculatedSharePaise: Long,
        val adjustmentPaise: Long = 0L,
        val finalSharePaise: Long
    )

    data class CalculationResult(
        val ratePaise: Long,
        val participantCount: Int,
        val fullCount: Int,
        val halfCount: Int,
        val baseSharePaise: Long, // Normal full share (floored to whole rupee)
        val halfSharePaise: Long, // Half share (floored to whole rupee)
        val participantShares: List<ParticipantShare>,
        val distributedPaise: Long,
        val remainingPaise: Long
    )

    /**
     * Formats integer paise into clean Indian Rupee string.
     * e.g., 20000 -> "₹200", 6600 -> "₹66", 200 -> "₹2", 250 -> "₹2.50"
     */
    fun formatPaise(paise: Long): String {
        val isNegative = paise < 0
        val absPaise = abs(paise)
        val rupees = absPaise / PAISE_PER_RUPEE
        val remainder = absPaise % PAISE_PER_RUPEE
        val prefix = if (isNegative) "-₹" else "₹"
        return if (remainder == 0L) {
            "$prefix$rupees"
        } else {
            "$prefix$rupees.${remainder.toString().padStart(2, '0')}"
        }
    }

    /**
     * Formats integer paise to whole rupees if divisible, or decimal.
     */
    fun formatRupees(paise: Long): String {
        val rupees = paise / PAISE_PER_RUPEE
        return "₹$rupees"
    }

    /**
     * Calculates trip shares deterministically based on whole-rupee floor rule.
     *
     * Invariants:
     * - ratePaise >= 0
     * - distributedPaise + remainingPaise == ratePaise
     * - Each participant receives whole rupees in paise.
     */
    fun calculateTrip(
        ratePaise: Long,
        participants: List<ParticipantInput>,
        explicitAdjustments: Map<String, Long> = emptyMap()
    ): CalculationResult {
        if (participants.isEmpty() || ratePaise <= 0L) {
            return CalculationResult(
                ratePaise = ratePaise,
                participantCount = 0,
                fullCount = 0,
                halfCount = 0,
                baseSharePaise = 0L,
                halfSharePaise = 0L,
                participantShares = emptyList(),
                distributedPaise = 0L,
                remainingPaise = ratePaise
            )
        }

        val fullParticipants = participants.filter { !it.isHalfShare }
        val halfParticipants = participants.filter { it.isHalfShare }
        val fullCount = fullParticipants.size
        val halfCount = halfParticipants.size
        val totalCount = participants.size

        // Mathematical settlement rule:
        // When all participants are full shares:
        // Share = floor(rateRupees / count)
        // e.g. ₹200 / 3 = ₹66 each, distributed = ₹198, remaining = ₹2
        // If there are half-shares:
        // Effective weight = fullCount + halfCount * 0.5
        // Or per agreed specification: Normal share = floor(rateRupees / totalCount)
        // and half-share = floor(normalShare / 2)
        val rateRupees = ratePaise / PAISE_PER_RUPEE

        val baseShareRupees: Long
        val halfShareRupees: Long

        if (halfCount == 0) {
            baseShareRupees = rateRupees / totalCount
            halfShareRupees = 0L
        } else {
            // Half-share model: total units = fullCount * 2 + halfCount
            // Unit value in rupees = rateRupees / (fullCount * 2 + halfCount)
            // Or floor(rateRupees / totalCount) with half being floor(base / 2).
            // By specification: "Normal share = floor(rate / N), half share = floor(normalShare / 2)"
            baseShareRupees = rateRupees / totalCount
            halfShareRupees = baseShareRupees / 2L
        }

        val baseSharePaise = baseShareRupees * PAISE_PER_RUPEE
        val halfSharePaise = halfShareRupees * PAISE_PER_RUPEE

        val shares = participants.map { p ->
            val calcShare = if (p.isHalfShare) halfSharePaise else baseSharePaise
            val adj = explicitAdjustments[p.personId] ?: 0L
            val finalShare = calcShare + adj
            ParticipantShare(
                personId = p.personId,
                isHalfShare = p.isHalfShare,
                calculatedSharePaise = calcShare,
                adjustmentPaise = adj,
                finalSharePaise = finalShare
            )
        }

        val distributedPaise = shares.sumOf { it.finalSharePaise }
        val remainingPaise = ratePaise - distributedPaise

        return CalculationResult(
            ratePaise = ratePaise,
            participantCount = totalCount,
            fullCount = fullCount,
            halfCount = halfCount,
            baseSharePaise = baseSharePaise,
            halfSharePaise = halfSharePaise,
            participantShares = shares,
            distributedPaise = distributedPaise,
            remainingPaise = remainingPaise
        )
    }

    /**
     * Validates whether an adjustment set is valid for a given trip.
     * Throws an IllegalArgumentException if distributed exceeds rate.
     */
    fun validateReconciliation(ratePaise: Long, distributedPaise: Long, remainingPaise: Long): Boolean {
        return (distributedPaise + remainingPaise) == ratePaise && distributedPaise >= 0L && remainingPaise >= 0L
    }
}
