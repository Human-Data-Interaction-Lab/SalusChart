package com.hdil.DataCollection.common.model

import java.time.Instant

data class HeartRateData(
    val uid: String,
    val startTime: Instant,
    val endTime: Instant,
    val samples: List<HeartRateSample> = listOf(),
)

data class HeartRateSample(
    val time: Instant,
    val beatsPerMinute: Long
)