package com.hdil.saluschart.data.model.model

import java.time.Instant

data class BloodPressure(
    val time: Instant,
    val systolic: Double,
    val diastolic: Double,
) : HealthData()
