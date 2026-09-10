package com.roshan.sandworks

import com.roshan.sandworks.domain.MoneyEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyEngineTest {

    private val defaultRate = 20_000L // ₹200.00

    @Test
    fun testOneParticipant() {
        val participants = listOf(
            MoneyEngine.ParticipantInput("p1")
        )
        val result = MoneyEngine.calculateTrip(defaultRate, participants)
        assertEquals(1, result.participantCount)
        assertEquals(20_000L, result.baseSharePaise) // ₹200
        assertEquals(20_000L, result.distributedPaise)
        assertEquals(0L, result.remainingPaise)
        assertEquals(20_000L, result.participantShares[0].finalSharePaise)
        assertTrue(MoneyEngine.validateReconciliation(defaultRate, result.distributedPaise, result.remainingPaise))
    }

    @Test
    fun testTwoParticipants() {
        val participants = listOf(
            MoneyEngine.ParticipantInput("p1"),
            MoneyEngine.ParticipantInput("p2")
        )
        val result = MoneyEngine.calculateTrip(defaultRate, participants)
        assertEquals(2, result.participantCount)
        assertEquals(10_000L, result.baseSharePaise) // ₹100
        assertEquals(20_000L, result.distributedPaise)
        assertEquals(0L, result.remainingPaise)
        assertTrue(MoneyEngine.validateReconciliation(defaultRate, result.distributedPaise, result.remainingPaise))
    }

    @Test
    fun testThreeParticipants_WholeRupeeFloorRule() {
        val participants = listOf(
            MoneyEngine.ParticipantInput("p1"),
            MoneyEngine.ParticipantInput("p2"),
            MoneyEngine.ParticipantInput("p3")
        )
        val result = MoneyEngine.calculateTrip(defaultRate, participants)
        assertEquals(3, result.participantCount)
        // ₹200 / 3 = ₹66 floor
        assertEquals(6_600L, result.baseSharePaise)
        // 3 * 66 = ₹198 (19,800 paise)
        assertEquals(19_800L, result.distributedPaise)
        // ₹2 (200 paise) remaining
        assertEquals(200L, result.remainingPaise)
        assertEquals("₹66", MoneyEngine.formatPaise(result.baseSharePaise))
        assertEquals("₹2", MoneyEngine.formatPaise(result.remainingPaise))
        assertTrue(MoneyEngine.validateReconciliation(defaultRate, result.distributedPaise, result.remainingPaise))
    }

    @Test
    fun testFourParticipants() {
        val participants = (1..4).map { MoneyEngine.ParticipantInput("p$it") }
        val result = MoneyEngine.calculateTrip(defaultRate, participants)
        assertEquals(4, result.participantCount)
        assertEquals(5_000L, result.baseSharePaise) // ₹50
        assertEquals(20_000L, result.distributedPaise)
        assertEquals(0L, result.remainingPaise)
        assertTrue(MoneyEngine.validateReconciliation(defaultRate, result.distributedPaise, result.remainingPaise))
    }

    @Test
    fun testFiveParticipants() {
        val participants = (1..5).map { MoneyEngine.ParticipantInput("p$it") }
        val result = MoneyEngine.calculateTrip(defaultRate, participants)
        assertEquals(5, result.participantCount)
        assertEquals(4_000L, result.baseSharePaise) // ₹40
        assertEquals(20_000L, result.distributedPaise)
        assertEquals(0L, result.remainingPaise)
        assertTrue(MoneyEngine.validateReconciliation(defaultRate, result.distributedPaise, result.remainingPaise))
    }

    @Test
    fun testSixParticipants_WholeRupeeFloorRule() {
        val participants = (1..6).map { MoneyEngine.ParticipantInput("p$it") }
        val result = MoneyEngine.calculateTrip(defaultRate, participants)
        assertEquals(6, result.participantCount)
        // ₹200 / 6 = ₹33 floor
        assertEquals(3_300L, result.baseSharePaise)
        // 6 * 33 = ₹198
        assertEquals(19_800L, result.distributedPaise)
        assertEquals(200L, result.remainingPaise)
        assertTrue(MoneyEngine.validateReconciliation(defaultRate, result.distributedPaise, result.remainingPaise))
    }

    @Test
    fun testSevenParticipants() {
        val participants = (1..7).map { MoneyEngine.ParticipantInput("p$it") }
        val result = MoneyEngine.calculateTrip(defaultRate, participants)
        assertEquals(7, result.participantCount)
        // ₹200 / 7 = ₹28 floor
        assertEquals(2_800L, result.baseSharePaise)
        // 7 * 28 = ₹196
        assertEquals(19_600L, result.distributedPaise)
        // ₹4 remaining
        assertEquals(400L, result.remainingPaise)
        assertTrue(MoneyEngine.validateReconciliation(defaultRate, result.distributedPaise, result.remainingPaise))
    }

    @Test
    fun testEightParticipants() {
        val participants = (1..8).map { MoneyEngine.ParticipantInput("p$it") }
        val result = MoneyEngine.calculateTrip(defaultRate, participants)
        assertEquals(8, result.participantCount)
        assertEquals(2_500L, result.baseSharePaise) // ₹25
        assertEquals(20_000L, result.distributedPaise)
        assertEquals(0L, result.remainingPaise)
        assertTrue(MoneyEngine.validateReconciliation(defaultRate, result.distributedPaise, result.remainingPaise))
    }

    @Test
    fun testHalfShareWithExplicitAdjustment() {
        // 3 participants: 2 full, 1 half
        val participants = listOf(
            MoneyEngine.ParticipantInput("labour_a", isHalfShare = false),
            MoneyEngine.ParticipantInput("labour_b", isHalfShare = false),
            MoneyEngine.ParticipantInput("labour_c", isHalfShare = true)
        )
        // Normal share = floor(200 / 3) = 66
        // Half share = floor(66 / 2) = 33
        // Distribution: 66 + 66 + 33 = 165, remainder = 35
        val result = MoneyEngine.calculateTrip(defaultRate, participants)
        assertEquals(6_600L, result.baseSharePaise)
        assertEquals(3_300L, result.halfSharePaise)
        assertEquals(16_500L, result.distributedPaise)
        assertEquals(3_500L, result.remainingPaise)
        assertTrue(MoneyEngine.validateReconciliation(defaultRate, result.distributedPaise, result.remainingPaise))

        // If labourers decide to give the extra 35 to labour_b
        val adjustments = mapOf("labour_b" to 3_500L)
        val adjustedResult = MoneyEngine.calculateTrip(defaultRate, participants, adjustments)
        assertEquals(20_000L, adjustedResult.distributedPaise)
        assertEquals(0L, adjustedResult.remainingPaise)
        assertTrue(MoneyEngine.validateReconciliation(defaultRate, adjustedResult.distributedPaise, adjustedResult.remainingPaise))
    }

    @Test
    fun testIdempotency() {
        val participants = (1..3).map { MoneyEngine.ParticipantInput("p$it") }
        val r1 = MoneyEngine.calculateTrip(defaultRate, participants)
        val r2 = MoneyEngine.calculateTrip(defaultRate, participants)
        assertEquals(r1, r2)
    }
}
