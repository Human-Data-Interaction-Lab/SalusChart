package com.hdil.saluschart.data.model.model

import com.hdil.saluschart.data.model.model.HealthData
import java.time.Instant

data class BloodGlucose(
//    val uid: String,
    val time: Instant,
    val level: Double,
//    val specimenSource: Int,
//    val mealType: Int,
//    val relationToMeal: Int
) : HealthData()

/*
- mealType : breakfast, lunch, dinner, snack, unknown
- level : Blood glucose level or concentration. Required field. Valid range: 0-50 mmol/L.
- relationToMeal : before, after, unknown
 */