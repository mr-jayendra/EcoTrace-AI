package com.example

import com.example.data.CarbonCalculator
import com.example.data.UserProfileEntity
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testCarbonCalculator_calculateBaseline_transportation() {
        // Car commute calculation
        val profileCar = UserProfileEntity(
            isOnboarded = true,
            transportType = "car",
            transportDistance = "20.5 km per day",
            householdSize = 1
        )
        val breakdownCar = CarbonCalculator.calculateBaseline(profileCar)
        // 20.5 km * 0.18 kg CO2e/km = 3.69 kg
        assertEquals(3.69, breakdownCar.transport, 0.001)

        // Train commute calculation
        val profileTrain = UserProfileEntity(
            isOnboarded = true,
            transportType = "train",
            transportDistance = "10 km",
            householdSize = 1
        )
        val breakdownTrain = CarbonCalculator.calculateBaseline(profileTrain)
        // 10.0 * 0.04 = 0.4 kg
        assertEquals(0.4, breakdownTrain.transport, 0.001)

        // Walk zero commute
        val profileWalk = UserProfileEntity(
            isOnboarded = true,
            transportType = "walk",
            transportDistance = "5km",
            householdSize = 1
        )
        val breakdownWalk = CarbonCalculator.calculateBaseline(profileWalk)
        assertEquals(0.0, breakdownWalk.transport, 0.001)
    }

    @Test
    fun testCarbonCalculator_calculateBaseline_diet() {
        val options = listOf(
            "vegan" to 1.5,
            "vegetarian" to 2.5,
            "mixed" to 4.5,
            "meat-heavy" to 7.2,
            "invalid_choice" to 4.5
        )

        for ((type, expected) in options) {
            val profile = UserProfileEntity(dietType = type)
            val breakdown = CarbonCalculator.calculateBaseline(profile)
            assertEquals("Expected $expected for diet type: $type", expected, breakdown.diet, 0.001)
        }
    }

    @Test
    fun testCarbonCalculator_calculateBaseline_energyAndHouseholdSize() {
        // Low usage: 150 kWh / 30 days = 5.0 kWh/day
        // 5.0 kWh/day * 0.40 kg/kWh = 2.0 kg CO2e/day
        // Household size = 2 -> 2.0 / 2 = 1.0 kg CO2e/day per person
        val profileLow = UserProfileEntity(
            energyHabit = "low",
            energyUsage = "150 kWh",
            householdSize = 2
        )
        val breakdownLow = CarbonCalculator.calculateBaseline(profileLow)
        assertEquals(1.0, breakdownLow.energy, 0.001)

        // Negative/invalid household size should be coerced to 1 safely
        val profileCoerce = UserProfileEntity(
            energyUsage = "300 kWh",
            householdSize = -5
        )
        val breakdownCoerce = CarbonCalculator.calculateBaseline(profileCoerce)
        // 300 / 30 * 0.4 / 1 = 4.0 kg CO2e/day
        assertEquals(4.0, breakdownCoerce.energy, 0.001)
    }

    @Test
    fun testCarbonCalculator_calculateBaseline_otherHabits() {
        val profile = UserProfileEntity(
            flightFreq = "frequent",
            shoppingFreq = "shopper",
            wasteHabit = "low"
        )
        val breakdown = CarbonCalculator.calculateBaseline(profile)
        // flight: 4000.0 / 365 = 10.9589
        assertEquals(4000.0 / 365.0, breakdown.flight, 0.01)
        // shopping shopper = 11.0
        assertEquals(11.0, breakdown.shopping, 0.01)
        // waste low = 0.7
        assertEquals(0.7, breakdown.waste, 0.01)
    }

    @Test
    fun testCarbonCalculator_calculateBaseline_edgeCasesAndInvalidInputs() {
        // Empty distances and usage strings
        val profileEmpty = UserProfileEntity(
            transportType = "car",
            transportDistance = "",
            energyUsage = ""
        )
        val breakdown = CarbonCalculator.calculateBaseline(profileEmpty)
        // Empty distance for car should fallback to default distance (25.0 km) -> 25.0 * 0.18 = 4.5
        assertEquals(4.5, breakdown.transport, 0.001)
        // Empty electricity usage should fallback to average (360 kWh) -> 360/30 * 0.4 = 4.8
        assertEquals(4.8, breakdown.energy, 0.001)

        // Completely garbage alphanumeric entry
        val profileGarbage = UserProfileEntity(
            transportType = "motorcycle",
            transportDistance = "Garbage text!! abc",
            energyUsage = "Not a number 500" // contains numbers inside though, parsed to 500
        )
        val breakdownGarbage = CarbonCalculator.calculateBaseline(profileGarbage)
        // "Garbage text!! abc" has no numbers -> default for motorcycle is 15.0 km -> 15.0 * 0.12 = 1.8
        assertEquals(1.8, breakdownGarbage.transport, 0.001)
        // "Not a number 500" -> cleans down to "500" -> 500.0 kWh -> 500/30 * 0.4 = 6.667
        assertEquals(6.666, breakdownGarbage.energy, 0.01)
    }

    @Test
    fun testCarbonCalculator_calculateDailyEmissions() {
        val profile = UserProfileEntity(
            transportType = "car",
            transportDistance = "10 km", // 1.8 kg baseline
            dietType = "mixed", // 4.5 kg baseline
            energyUsage = "300 kWh", // 4.0 kg baseline (householdSize = 1)
            flightFreq = "none", // 0.0
            shoppingFreq = "minimalist", // 1.8 kg baseline
            wasteHabit = "low" // 0.7 kg baseline
        )
        // Total baseline: 1.8 + 4.0 + 4.5 + 0.0 + 1.8 + 0.7 = 12.8 kg
        val baseline = CarbonCalculator.calculateBaseline(profile)
        assertEquals(12.8, baseline.totalDaily, 0.001)

        // Simulate selected eco activities: "Used public transport", "Ate vegetarian meals"
        val changes = listOf("Used public transport", "Ate vegetarian meals")
        val withChanges = CarbonCalculator.calculateDailyEmissions(baseline, changes)
        // Transport: 1.8 * 0.4 = 0.72
        // Diet: 4.5 * 0.5 = 2.25
        // Energy: 4.0, Flight: 0.0, Shopping: 1.8, Waste: 0.7
        // Total changes daily: 0.72 + 4.0 + 2.25 + 0.0 + 1.8 + 0.7 = 9.47 kg
        assertEquals(9.47, withChanges, 0.001)
    }

    @Test
    fun testCarbonCalculator_explainInSimpleLanguage() {
        val transportLow = CarbonCalculator.explainInSimpleLanguage("Transportation", 0.5)
        assertTrue(transportLow.contains("keeping things clean"))

        val transportHigh = CarbonCalculator.explainInSimpleLanguage("Transportation", 4.5)
        assertTrue(transportHigh.contains("Choosing a bus, train"))

        val dietLow = CarbonCalculator.explainInSimpleLanguage("Diet", 1.2)
        assertTrue(dietLow.contains("excellent climate-friendly"))
    }
}
