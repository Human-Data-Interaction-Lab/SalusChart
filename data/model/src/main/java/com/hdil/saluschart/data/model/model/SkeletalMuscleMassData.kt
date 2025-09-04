package com.hdil.DataCollection.common.model

import java.time.Instant

data class SkeletalMuscleMassData(
    val uid: String,
    val time: Instant,
    val skeletalMuscleMass: Double
)