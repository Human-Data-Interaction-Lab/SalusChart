package com.hdil.DataCollection.common.model

import java.time.Instant

data class BloodPressureData(
    val uid: String,
    val time: Instant,
    val systolic: Double,
    val diastolic: Double,
    val bodyPosition: Int? = null,
    val measurementLocation: Int? = null
)

/*
- bodyPosition : unknown, standing up, sitting down, lying down, reclining
- measurementLocation : unknown, left writs, right wrist, left upper arm, right upper arm
 */