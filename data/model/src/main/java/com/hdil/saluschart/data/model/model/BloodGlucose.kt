package com.hdil.saluschart.data.model.model

import java.time.Instant

data class BloodGlucose(
    val time: Instant,
    val level: Double,
) : HealthData()
