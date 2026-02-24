package com.hdil.saluschart.data.model.model

import java.time.Instant

data class Weight(
    val time: Instant,
    val weight: Mass,
) : HealthData()
