package com.hdil.DataCollection.common.model

import java.time.Instant
import java.time.ZoneOffset

data class ExerciseData(
    val uid: String,
    val startTime: Instant,
    val startZoneOffset: ZoneOffset? = null,
    val endTime: Instant,
    val endZoneOffset: ZoneOffset? = null,
    val exerciseType: String,
)