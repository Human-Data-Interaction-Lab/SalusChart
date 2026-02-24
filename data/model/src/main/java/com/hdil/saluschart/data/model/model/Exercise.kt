package com.hdil.saluschart.data.model.model

import java.time.Instant

data class Exercise(
    val startTime: Instant,
    val endTime: Instant,
    val caloriesBurned: Double,
) : HealthData()
