package com.hdil.saluschart.data.model.model

import java.time.Instant

data class Diet(
//    val uid: String,
    val startTime: Instant,
    val endTime: Instant,
    val calories: Double,
    val protein: Mass,
    val carbohydrate: Mass,
    val fat: Mass,
    val mealType: Int
) : HealthData()

/*
- mealType : breakfast, lunch, dinner, snack, unknown
*/