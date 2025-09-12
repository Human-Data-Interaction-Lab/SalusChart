package com.hdil.saluschart.data.model.model

sealed class HealthData

data class Mass private constructor(
    val inGrams: Double
) {
    companion object {
        fun grams(value: Double): Mass = Mass(value)
        fun kilograms(value: Double): Mass = Mass(value * 1000.0)
        fun pounds(value: Double): Mass = Mass(value * 453.59237)
    }

    fun toGrams(): Double = inGrams
    fun toKilograms(): Double = inGrams / 1000.0
    fun toPounds(): Double = inGrams / 453.59237
    fun toOunces(): Double = inGrams / 28.3495231

    override fun toString(): String = "${toKilograms()} kg"
}