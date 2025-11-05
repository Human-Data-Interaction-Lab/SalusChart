package com.hdil.saluschart.data.model.model

sealed class HealthData

/**
 * 무게 단위 변환을 위한 클래스입니다.
 * 각 단위는 그램을 기준으로 변환됩니다.
 */
data class Mass private constructor(
    val inGrams: Double
) {
    companion object {
        fun grams(value: Double): Mass = Mass(value)
        fun kilograms(value: Double): Mass = Mass(value * 1000.0)
        fun pounds(value: Double): Mass = Mass(value * 453.59237)
        fun ounces(value: Double): Mass = Mass(value * 28.3495231)
    }

    fun toGrams(): Double = inGrams
    fun toKilograms(): Double = inGrams / 1000.0
    fun toPounds(): Double = inGrams / 453.59237
    fun toOunces(): Double = inGrams / 28.3495231

    override fun toString(): String = "${toKilograms()} kg"
}

/**
 * Mass unit enumeration for API convenience
 * Represents the unit type, not actual mass values
 */
enum class MassUnit(val displayName: String, val converter: (Mass) -> Double) {
    KILOGRAM("kg", { it.toKilograms() }),
    POUND("lb", { it.toPounds() }),
    GRAM("g", { it.toGrams() }),
    OUNCE("oz", { it.toOunces() })
}