package com.hdil.DataCollection.common.model

import java.time.Instant

// weight 가 Mass 타입이지만 Double(kg) 타입으로 변환

data class WeightData(
    val uid: String,
    val time: Instant,
    val weight: Double,
)