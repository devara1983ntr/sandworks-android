package com.roshan.sandworks

import com.roshan.sandworks.domain.MoneyEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Random

class Golden33TripTest {

    data class SimTrip(
        val tripNumber: Int,
        val tractorId: String,
        val participants: List<MoneyEngine.ParticipantInput>,
        val ratePaise: Long = 20_000L,
        var isVoided: Boolean = false
    )

    @Test
    fun test33TripMandatoryScenario() {
        val tractors = listOf("T1", "T2", "T3")
        val labourers = listOf("Labour_A", "Labour_B", "Labour_C", "Labour_D", "Labour_E")
        val drivers = listOf("Driver_1", "Driver_2", "Driver_3")
        val allPeople = labourers + drivers

        val rnd = Random(42) // Deterministic seed
        val trips = mutableListOf<SimTrip>()

        // Generate 33 trips
        for (i in 1..33) {
            val tractor = tractors[rnd.nextInt(tractors.size)]
            // Random participant count 1..6
            val count = rnd.nextInt(6) + 1
            val shuffled = allPeople.shuffled(rnd).take(count)
            val participants = shuffled.map { MoneyEngine.ParticipantInput(it) }
            trips.add(SimTrip(tripNumber = i, tractorId = tractor, participants = participants))
        }

        assertEquals(33, trips.size)

        // Calculate all 33 trips
        var grossPaise = 0L
        var distributedPaise = 0L
        var remainingPaise = 0L
        val personEarned = mutableMapOf<String, Long>()

        for (trip in trips) {
            grossPaise += trip.ratePaise
            val calc = MoneyEngine.calculateTrip(trip.ratePaise, trip.participants)
            distributedPaise += calc.distributedPaise
            remainingPaise += calc.remainingPaise

            for (share in calc.participantShares) {
                personEarned[share.personId] = (personEarned[share.personId] ?: 0L) + share.finalSharePaise
            }
        }

        // Total gross for 33 trips = 33 * ₹200 = ₹6,600 (660,000 paise)
        assertEquals(660_000L, grossPaise)
        // Invariant: Gross = Distributed + Remaining
        assertEquals(grossPaise, distributedPaise + remainingPaise)
        // Sum of all person earnings must equal distributedPaise
        assertEquals(distributedPaise, personEarned.values.sum())

        // Invariant: Trip Rate = Distributed + Remaining for EVERY trip
        for (trip in trips) {
            val calc = MoneyEngine.calculateTrip(trip.ratePaise, trip.participants)
            assertTrue(MoneyEngine.validateReconciliation(trip.ratePaise, calc.distributedPaise, calc.remainingPaise))
        }

        // Test Voiding Trip #17
        trips[16].isVoided = true
        val activeTrips = trips.filter { !it.isVoided }
        assertEquals(32, activeTrips.size)

        var activeGross = 0L
        var activeDistributed = 0L
        var activeRemaining = 0L
        for (t in activeTrips) {
            activeGross += t.ratePaise
            val c = MoneyEngine.calculateTrip(t.ratePaise, t.participants)
            activeDistributed += c.distributedPaise
            activeRemaining += c.remainingPaise
        }
        assertEquals(640_000L, activeGross) // 32 * ₹200 = ₹6,400
        assertEquals(activeGross, activeDistributed + activeRemaining)

        // Test repeated calculation (idempotency)
        var repeatDistributed = 0L
        var repeatRemaining = 0L
        for (t in activeTrips) {
            val c = MoneyEngine.calculateTrip(t.ratePaise, t.participants)
            repeatDistributed += c.distributedPaise
            repeatRemaining += c.remainingPaise
        }
        assertEquals(activeDistributed, repeatDistributed)
        assertEquals(activeRemaining, repeatRemaining)
    }
}
