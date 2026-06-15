package com.example.data

object CarbonCalculator {
    // Standard CO2e emission factors (in kg CO2e)
    const val BUS_CO2_PER_KM = 0.08
    const val TRAIN_CO2_PER_KM = 0.04
    const val MOTORCYCLE_CO2_PER_KM = 0.12
    const val CAR_CO2_PER_KM = 0.18
    const val ELECTRICITY_CO2_PER_KWH = 0.40
    const val FLIGHT_CO2_PER_KM = 0.15

    data class CarbonBreakdown(
        val totalDaily: Double,
        val transport: Double,
        val energy: Double,
        val diet: Double,
        val flight: Double,
        val shopping: Double,
        val waste: Double
    ) {
        val transportPercent: Float get() = if (totalDaily > 0) (transport / totalDaily * 100).toFloat() else 0f
        val energyPercent: Float get() = if (totalDaily > 0) (energy / totalDaily * 100).toFloat() else 0f
        val dietPercent: Float get() = if (totalDaily > 0) (diet / totalDaily * 100).toFloat() else 0f
        val flightPercent: Float get() = if (totalDaily > 0) (flight / totalDaily * 100).toFloat() else 0f
        val shoppingPercent: Float get() = if (totalDaily > 0) (shopping / totalDaily * 100).toFloat() else 0f
        val wastePercent: Float get() = if (totalDaily > 0) (waste / totalDaily * 100).toFloat() else 0f

        fun getLargestCategoryName(): String {
            val list = listOf(
                "Transportation" to transport,
                "Home Energy" to energy,
                "Diet" to diet,
                "Flights" to flight,
                "Shopping" to shopping,
                "Waste" to waste
            )
            return list.maxByOrNull { it.second }?.first ?: "Transportation"
        }
    }

    /**
     * Calculates estimated baseline daily footprint in kg CO2e
     */
    fun calculateBaseline(profile: UserProfileEntity): CarbonBreakdown {
        // 1. Transportation
        val distanceKm = parseDistance(profile.transportDistance) ?: when (profile.transportType.lowercase()) {
            "walk" -> 1.0
            "bicycle" -> 5.0
            "bus" -> 20.0
            "train" -> 30.0
            "motorcycle" -> 15.0
            "car" -> 25.0
            else -> 10.0
        }
        val transCoeff = when (profile.transportType.lowercase()) {
            "walk", "bicycle" -> 0.0
            "bus" -> BUS_CO2_PER_KM
            "train" -> TRAIN_CO2_PER_KM
            "motorcycle" -> MOTORCYCLE_CO2_PER_KM
            "car" -> CAR_CO2_PER_KM
            else -> CAR_CO2_PER_KM / 2
        }
        val transportEmissions = distanceKm * transCoeff

        // 2. Home Energy (daily share)
        val monthlyKwh = parseElectricity(profile.energyUsage) ?: when (profile.energyHabit.lowercase()) {
            "low" -> 150.0 // 5 kwh/day
            "average" -> 360.0 // 12 kwh/day
            "high" -> 750.0 // 25 kwh/day
            else -> 360.0
        }
        val energyEmissions = (monthlyKwh / 30.0) * ELECTRICITY_CO2_PER_KWH / profile.householdSize.coerceAtLeast(1)

        // 3. Diet
        val dietEmissions = when (profile.dietType.lowercase()) {
            "vegan" -> 1.5
            "vegetarian" -> 2.5
            "mixed" -> 4.5
            "meat-heavy" -> 7.2
            else -> 4.5
        }

        // 4. Flights (expressed as daily average contribution)
        val flightEmissions = when (profile.flightFreq.lowercase()) {
            "none" -> 0.0
            "rare" -> 500.0 / 365.0      // 1-2 small flights a year
            "frequent" -> 4000.0 / 365.0  // multiple international flights
            else -> 1000.0 / 365.0
        }

        // 5. Shopping (daily share)
        val shoppingEmissions = when (profile.shoppingFreq.lowercase()) {
            "minimalist" -> 1.8
            "average" -> 4.5
            "shopper" -> 11.0
            else -> 4.5
        }

        // 6. Waste
        val wasteEmissions = when (profile.wasteHabit.lowercase()) {
            "low" -> 0.7
            "average" -> 1.6
            "high" -> 3.2
            else -> 1.6
        }

        val totalDaily = transportEmissions + energyEmissions + dietEmissions + flightEmissions + shoppingEmissions + wasteEmissions

        return CarbonBreakdown(
            totalDaily = totalDaily,
            transport = transportEmissions,
            energy = energyEmissions,
            diet = dietEmissions,
            flight = flightEmissions,
            shopping = shoppingEmissions,
            waste = wasteEmissions
        )
    }

    /**
     * Re-calculates carbon for a day based on baseline and selected daily actions lists
     */
    fun calculateDailyEmissions(baseline: CarbonBreakdown, changesList: List<String>): Double {
        var dailyTransport = baseline.transport
        var dailyEnergy = baseline.energy
        var dailyDiet = baseline.diet
        var dailyFlight = baseline.flight
        var dailyShopping = baseline.shopping
        var dailyWaste = baseline.waste

        for (change in changesList) {
            when (change) {
                "Used public transport" -> {
                    // Supplant car drive with bus/train
                    dailyTransport = dailyTransport * 0.4
                }
                "Walked or cycled" -> {
                    dailyTransport = 0.0
                }
                "Drove a car" -> {
                    dailyTransport += 4.5 // Represent standard single trip emission
                }
                "Took a flight" -> {
                    dailyFlight += 150.0 // Single domestic or short flight weight
                }
                "Ate more meat" -> {
                    dailyDiet = (dailyDiet * 1.3) + 1.5
                }
                "Ate vegetarian meals" -> {
                    dailyDiet = dailyDiet * 0.5
                }
                "Large shopping purchase" -> {
                    dailyShopping += 8.5
                }
                "Higher electricity usage" -> {
                    dailyEnergy += 2.8
                }
                "Other custom activity" -> {
                    // Slight standard carbon impact bump
                    dailyShopping += 1.5
                }
            }
        }

        return dailyTransport + dailyEnergy + dailyDiet + dailyFlight + dailyShopping + dailyWaste
    }

    // Helper to scrape values from exact text entry: "20 km per day" -> 20.0
    private fun parseDistance(text: String): Double? {
        if (text.isEmpty()) return null
        val cleaned = text.replace(Regex("[^0-9.]"), "")
        return cleaned.toDoubleOrNull()
    }

    // Parse kWh: "200 kWh" -> 200.0
    private fun parseElectricity(text: String): Double? {
        if (text.isEmpty()) return null
        val cleaned = text.replace(Regex("[^0-9.]"), "")
        return cleaned.toDoubleOrNull()
    }

    fun explainInSimpleLanguage(category: String, amt: Double): String {
        val formattedAmt = String.format("%.1f", amt)
        return when (category) {
            "Transportation" -> {
                if (amt < 1.0) "Your transport footprint is almost zero today! Cycling or walking is keeping things clean."
                else "Your transport emitted $formattedAmt kg CO₂. Choosing a bus, train, or carpool next time could cut this by 60%."
            }
            "Home Energy" -> {
                "Your share of electricity created $formattedAmt kg CO₂. Dimming unused lights, turning off standbys, and using eco modes will lower both emission points and energy bills."
            }
            "Diet" -> {
                if (amt < 2.0) "An excellent climate-friendly diet today! Highly vegetable-based meals have remarkably small footprint footprints."
                else "Your diet accounted for $formattedAmt kg CO₂. Enjoying organic and plant-based foods can save tons of emissions."
            }
            "Flights" -> {
                "Flight travel adds heavy localized carbon loads and accounts for $formattedAmt kg CO₂. Offsetting flights with sustainable transit maintains balance."
            }
            "Shopping" -> {
                "Shopping purchases contributed $formattedAmt kg CO₂. Prioritizing secondhand purchases and avoiding fast fashion cuts waste instantly."
            }
            else -> {
                "Waste decomposition contributed $formattedAmt kg CO₂. Proper sorting, composting, and reducing single-use plastics keeps recycling high and carbon low."
            }
        }
    }
}
