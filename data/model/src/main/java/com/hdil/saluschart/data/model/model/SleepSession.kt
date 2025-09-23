package com.hdil.saluschart.data.model.model

import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

data class SleepSession(
//    val uid: String,
//    val title: String? = null,
//    val notes: String? = null,
    val startTime: Instant,
    val endTime: Instant,
    val stages: List<SleepStage> = listOf(),
) : HealthData()


data class SleepStage(
    val startTime: Instant,
    val endTime: Instant,
    val stage: SleepStageType
)

enum class SleepStageType {
    AWAKE,
    REM,
    LIGHT,
    DEEP,
    UNKNOWN
}