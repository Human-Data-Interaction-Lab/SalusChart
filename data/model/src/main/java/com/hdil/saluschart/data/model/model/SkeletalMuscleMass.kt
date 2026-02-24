package com.hdil.saluschart.data.model.model

import java.time.Instant

data class SkeletalMuscleMass(
    val time: Instant,
    val skeletalMuscleMass: Double
) : HealthData()