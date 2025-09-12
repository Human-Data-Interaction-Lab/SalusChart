package com.hdil.saluschart.data.model.model

import java.time.Instant

data class Exercise(
//    val uid: String,
    val startTime: Instant,
    val endTime: Instant,
    val caloriesBurned: Double,
    val exerciseType: String,
) : HealthData()

// exerciseType : unknown, running, walking, cycling, swimming, other