package com.hdil.DataCollection.common.model

import java.time.Instant

data class BodyFatData(
    val uid: String,
    val time: Instant,
    val bodyFatPercentage: Double
)