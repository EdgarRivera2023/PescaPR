package com.bradmir.pescapr.data

import com.google.gson.annotations.SerializedName

data class SwellResponse(
    val hourly: HourlySwell?
)

data class HourlySwell(
    val time: List<String>,
    @SerializedName("wave_height") val waveHeight: List<Float?>,
    @SerializedName("wave_period") val wavePeriod: List<Float?>,
    @SerializedName("wave_direction") val waveDirection: List<Float?>
)

data class ProSwellMetrics(
    val heightFt: Float,
    val periodSec: Float,
    val directionDeg: Float,
    val score: Int
)

data class GoldenDayPrediction(
    val date: String,
    val windowType: String,
    val highTideTime: String,
    val sunEventTime: String,
    val timeDifferenceMinutes: Long,
    val explanation: String
)

enum class SwellConditionRating {
    GOLDEN, GOOD, FAIR, POOR
}

fun calculateGoldenDayScore(
    heightFt: Double,
    periodSec: Double,
    windSpeedKts: Double = 0.0
): SwellConditionRating {
    val isGolden = periodSec >= 9.0 && heightFt in 2.0..5.0
    return when {
        isGolden -> SwellConditionRating.GOLDEN
        periodSec >= 7.0 && heightFt in 1.5..6.0 -> SwellConditionRating.GOOD
        periodSec >= 5.0 && heightFt in 1.0..8.0 -> SwellConditionRating.FAIR
        else -> SwellConditionRating.POOR
    }
}

fun isGoldenDay(heightFt: Double, periodSec: Double): Boolean {
    return periodSec >= 9.0 && heightFt in 2.0..5.0
}

fun getGoldenDayExplanation(heightFt: Double, periodSec: Double): String {
    return when {
        periodSec < 9.0 -> {
            "Mar Picado / Período Corto (${String.format(java.util.Locale.US, "%.1f", periodSec)}s): Requiere período ≥ 9.0s y altura 2.0-5.0ft para Día Dorado."
        }
        heightFt < 2.0 -> {
            "Oleaje Calmo / Bajo (${String.format(java.util.Locale.US, "%.1f", heightFt)}ft): Requiere altura entre 2.0ft y 5.0ft para Día Dorado."
        }
        heightFt > 5.0 -> {
            "Mar Marejado / Fuerte (${String.format(java.util.Locale.US, "%.1f", heightFt)}ft): Requiere altura entre 2.0ft y 5.0ft para Día Dorado."
        }
        else -> {
            "¡Condiciones Óptimas de Día Dorado! Oleaje y período ideales para surf casting en Puerto Rico."
        }
    }
}

fun calculatePlannerScore(heightFt: Double, periodSec: Double): Int {
    if (isGoldenDay(heightFt, periodSec)) return 10
    var score = 0
    if (periodSec >= 9.0) score += 5
    else if (periodSec >= 7.0) score += 3
    else if (periodSec >= 5.0) score += 1

    if (heightFt in 2.0..5.0) score += 5
    else if (heightFt in 1.5..6.0) score += 3
    else if (heightFt in 1.0..8.0) score += 1

    return score.coerceIn(0, 10)
}

val ProSwellMetrics.isGoldenDay: Boolean
    get() = isGoldenDay(heightFt.toDouble(), periodSec.toDouble())

fun generate30DayGoldenTideWindows(
    baseLatitude: Double = 18.2208,
    baseLongitude: Double = -66.5901
): List<GoldenDayPrediction> {
    val predictions = mutableListOf<GoldenDayPrediction>()
    val sdfDateOutput = java.text.SimpleDateFormat("EEEE, d 'de' MMMM", java.util.Locale("es", "PR"))
    val sdfTimeOutput = java.text.SimpleDateFormat("h:mm a", java.util.Locale.US)

    val periodMs = (12 * 60 + 25.2) * 60 * 1000

    val refCal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("America/Puerto_Rico")).apply {
        set(java.util.Calendar.HOUR_OF_DAY, 5)
        set(java.util.Calendar.MINUTE, 30)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }
    val refMs = refCal.timeInMillis

    for (dayOffset in 0..29) {
        val currentDayCal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("America/Puerto_Rico")).apply {
            add(java.util.Calendar.DAY_OF_YEAR, dayOffset)
        }

        val dayOfYear = currentDayCal.get(java.util.Calendar.DAY_OF_YEAR)

        val sunVar = Math.sin((dayOfYear - 80) * 2 * Math.PI / 365.25)
        val sunriseDec = 6.15 - (sunVar * 0.4)
        val sunsetDec = 18.5 + (sunVar * 0.6)

        val sunriseCal = (currentDayCal.clone() as java.util.Calendar).apply {
            set(java.util.Calendar.HOUR_OF_DAY, sunriseDec.toInt())
            set(java.util.Calendar.MINUTE, ((sunriseDec - sunriseDec.toInt()) * 60).toInt())
            set(java.util.Calendar.SECOND, 0)
        }

        val sunsetCal = (currentDayCal.clone() as java.util.Calendar).apply {
            set(java.util.Calendar.HOUR_OF_DAY, sunsetDec.toInt())
            set(java.util.Calendar.MINUTE, ((sunsetDec - sunsetDec.toInt()) * 60).toInt())
            set(java.util.Calendar.SECOND, 0)
        }

        val dayStartMs = (currentDayCal.clone() as java.util.Calendar).apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
        }.timeInMillis

        val dayEndMs = dayStartMs + 24 * 3600 * 1000

        val kStart = Math.floor((dayStartMs - refMs) / periodMs).toLong() - 1
        val kEnd = Math.ceil((dayEndMs - refMs) / periodMs).toLong() + 1

        for (k in kStart..kEnd) {
            val tideTimeMs = (refMs + k * periodMs).toLong()
            if (tideTimeMs in dayStartMs until dayEndMs) {
                val diffSunriseMin = Math.abs(tideTimeMs - sunriseCal.timeInMillis) / (60 * 1000)
                if (diffSunriseMin <= 120) {
                    val dateFormatted = sdfDateOutput.format(java.util.Date(tideTimeMs)).replaceFirstChar { it.uppercase() }
                    val highTideStr = sdfTimeOutput.format(java.util.Date(tideTimeMs))
                    val sunStr = sdfTimeOutput.format(sunriseCal.time)
                    val sign = if (tideTimeMs >= sunriseCal.timeInMillis) "+" else "-"
                    predictions.add(
                        GoldenDayPrediction(
                            date = dateFormatted,
                            windowType = "Amanecer",
                            highTideTime = highTideStr,
                            sunEventTime = sunStr,
                            timeDifferenceMinutes = diffSunriseMin,
                            explanation = "Marea alta coincide con el amanecer ($sign${diffSunriseMin} min). Ideal para la pesca de orilla."
                        )
                    )
                }

                val diffSunsetMin = Math.abs(tideTimeMs - sunsetCal.timeInMillis) / (60 * 1000)
                if (diffSunsetMin <= 120) {
                    val dateFormatted = sdfDateOutput.format(java.util.Date(tideTimeMs)).replaceFirstChar { it.uppercase() }
                    val highTideStr = sdfTimeOutput.format(java.util.Date(tideTimeMs))
                    val sunStr = sdfTimeOutput.format(sunsetCal.time)
                    val sign = if (tideTimeMs >= sunsetCal.timeInMillis) "+" else "-"
                    predictions.add(
                        GoldenDayPrediction(
                            date = dateFormatted,
                            windowType = "Atardecer",
                            highTideTime = highTideStr,
                            sunEventTime = sunStr,
                            timeDifferenceMinutes = diffSunsetMin,
                            explanation = "Marea alta coincide con el atardecer ($sign${diffSunsetMin} min). Excelente picada al caer la tarde."
                        )
                    )
                }
            }
        }
    }

    return predictions
}
