package com.hdil.saluschart.data.model.model

import java.time.Instant

data class SkeletalMuscleMass(
//    val uid: String,
    val time: Instant,
    val skeletalMuscleMass: Double
) : HealthData()