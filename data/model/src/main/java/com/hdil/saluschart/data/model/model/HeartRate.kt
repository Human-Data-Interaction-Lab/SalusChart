package com.hdil.saluschart.data.model.model

import java.time.Instant

data class HeartRate(
//    val uid: String,
    val startTime: Instant,
    val endTime: Instant,
    val samples: List<HeartRateSample> = listOf(),
) : HealthData()

data class HeartRateSample(
    val time: Instant,
    val beatsPerMinute: Long
)