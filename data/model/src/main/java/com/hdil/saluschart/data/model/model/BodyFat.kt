package com.hdil.saluschart.data.model.model

import java.time.Instant

data class BodyFat(
//    val uid: String,
    val time: Instant,
    val bodyFatPercentage: Double
) : HealthData()