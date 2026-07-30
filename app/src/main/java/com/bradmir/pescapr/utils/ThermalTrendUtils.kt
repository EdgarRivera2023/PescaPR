package com.bradmir.pescapr.utils

enum class ThermalTrend(val displayName: String) {
    WARMING("En Calentamiento"),
    COOLING("En Enfriamiento"),
    STABLE("Estable"),
    INSUFFICIENT_DATA("Datos Insuficientes")
}

data class ThermalTrendResult(
    val trend: ThermalTrend,
    val deltaDegrees: Float,
    val averageTemp: Float,
    val description: String
)

object ThermalTrendUtils {

    fun calculate7DayThermalTrend(history: List<Float>): ThermalTrendResult {
        if (history.size < 2) {
            val avg = if (history.isNotEmpty()) history.average().toFloat() else 0f
            return ThermalTrendResult(
                trend = ThermalTrend.INSUFFICIENT_DATA,
                deltaDegrees = 0f,
                averageTemp = avg,
                description = "Se requieren más lecturas para calcular la tendencia térmica"
            )
        }

        val averageTemp = history.average().toFloat()
        val firstTemp = history.first()
        val lastTemp = history.last()
        val delta = lastTemp - firstTemp

        return when {
            delta > 0.5f -> {
                ThermalTrendResult(
                    trend = ThermalTrend.WARMING,
                    deltaDegrees = delta,
                    averageTemp = averageTemp,
                    description = "Tendencia al alza en la temperatura del agua"
                )
            }
            delta < -0.5f -> {
                ThermalTrendResult(
                    trend = ThermalTrend.COOLING,
                    deltaDegrees = delta,
                    averageTemp = averageTemp,
                    description = "Tendencia a la baja en la temperatura del agua"
                )
            }
            else -> {
                ThermalTrendResult(
                    trend = ThermalTrend.STABLE,
                    deltaDegrees = delta,
                    averageTemp = averageTemp,
                    description = "Temperatura del agua estable"
                )
            }
        }
    }
}
