package com.hdil.saluschart.data.model.model

import java.time.Instant

data class StepCount(
//    val uid: String,
    val startTime: Instant,
    val endTime: Instant,
    val stepCount: Long
) : HealthData()