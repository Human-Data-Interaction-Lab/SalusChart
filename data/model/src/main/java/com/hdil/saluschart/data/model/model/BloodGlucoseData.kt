package com.hdil.DataCollection.common.model

import java.time.Instant

// level 은 BloodGlucose 타입에서 Double 타입으로 변경

data class BloodGlucoseData(
    val uid: String,
    val time: Instant,
    val level: Double,
    val specimenSource: Int,
    val mealType: Int,
    val relationToMeal: Int
)


/*
- mealType : breakfast, launch, dinner, snack, unknown
- level : Blood glucose level or concentration. Required field. Valid range: 0-50 mmol/L.
 */