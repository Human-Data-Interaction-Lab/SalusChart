package com.hdil.saluschart.data.model.model

import java.time.Instant

data class BloodPressure(
//    val uid: String,
    val time: Instant,
    val systolic: Double,
    val diastolic: Double,
//    val bodyPosition: Int? = null,
//    val measurementLocation: Int? = null
) : HealthData()

/*
- bodyPosition : unknown, standing up, sitting down, lying down, reclining
- measurementLocation : unknown, left writs, right wrist, left upper arm, right upper arm
 */