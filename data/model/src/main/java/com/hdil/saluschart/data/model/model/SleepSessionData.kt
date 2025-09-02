package com.hdil.DataCollection.common.model

import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

data class SleepSessionData(
    val uid: String,
    val title: String? = null,
    val notes: String? = null,
    val startTime: Instant,
    val startZoneOffset: ZoneOffset? = null,
    val endTime: Instant,
    val endZoneOffset: ZoneOffset? = null,
    val duration: Duration?,
    val stages: List<SleepStage> = listOf(),
    val score: Int? = null,
)


data class SleepStage(
    val startTime: Instant,
    val endTime: Instant,
    val stage: SleepStageType
)

enum class SleepStageType {
    UNKNOWN,
    AWAKE,
    AWAKE_IN_BED,
    OUT_OF_BED,
    LIGHT,
    DEEP,
    REM,
    SLEEPING
}