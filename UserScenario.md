# User Scenario

> **일주일간 수면 데이터(HealthData 객체)와 걸음 수(JSON)를 시각화한다! (초보자용/전문가용)**
> 

### Process

1. **Input**: 헬스 데이터 구조 정의
2. **Transform**: 시각화용 데이터 변환
3. **Generate**: 시각화 코드 생성
4. **Output**: 모바일 최적화 시각화 출력

# 1. Input

### 수면 데이터 (HealthData 객체)

```kotlin
// 리스트 형식
val sleepData = listOf(
    HealthData.Sleep(
        startTime = Instant.parse("2025-05-01T23:00:00Z"),
        endTime = Instant.parse("2025-05-02T06:30:00Z")
    ),
    HealthData.Sleep(
        startTime = Instant.parse("2025-05-02T23:30:00Z"),
        endTime = Instant.parse("2025-05-03T07:00:00Z")
    )
)

sealed class HealthData {
    data class Step(
        val date: LocalDate,
        val count: Int
    ) : HealthData()

    data class Sleep(
        val startTime: Instant,
        val endTime: Instant
    ) : HealthData() {
        val durationHours: Double
            get() = Duration.between(startTime, endTime).toMinutes() / 60.0
    }

    data class HeartRate(
        val timestamp: Instant,
        val bpm: Int
    ) : HealthData()
}
```

# 2. Transform

### 1) 정렬: 수면 시작 시간 기준 정렬

```kotlin
val sorted = sleepData.sortedBy { it.startTime }
```

### 2) 단위 변환: 사용자가 원하는 단위(일/주/월)로 그룹핑

- 결과: [ChartMark(x = "Week 2025-18", y = 1f), ChartMark(x = "Week 2025-19", y = 1f)]
- 추가: health data 에 대한 단위 변환? (weight: kg, ounce, pound)

```kotlin
val key = when (groupBy) {
    is TimeUnitGroup.DAY -> date.toString()
    is TimeUnitGroup.WEEK -> {
        val week = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR)
        val year = date.get(IsoFields.WEEK_BASED_YEAR)
        "Week $year-$week"
    }
    is TimeUnitGroup.MONTH -> "${date.year}-${date.monthValue.toString().padStart(2, '0')}"
}
```

### 최종 사용 예시

```kotlin
sleepData.transform(
    sorting = true,
    unit = TimeUnitGroup.WEEK
)
```

### 3) HealthData  → ChartMark 변환

# 3. **Generate**

### 초보자용

- 목적
    - 선언형 구성
    - 차트 타입 제한 (데이터 기반 추천)
        - IDE 자동완성에서 chartType 선택 시 availableChartTypes만 추천하도록 유도하는 방식
    - 쉽게 읽고 작성할 수 있음 (DSL 기반)
        
```kotlin
SalusChart {
    data = sleepData.transform(
	    sorting = true,
	    unit = TimeUnitGroup.Month
		)
    type = ChartType.LINE         // sleepData이므로 PieChart는 선택 불가
    x = Axis.Date                 // JSON 파일일 경우
    y = Axis.SleepDuration
    color = "green"
    title = "최근 일주일 수면 시간"
}

SalusChart {
    data = sleepData.transform(
	    sorting = true,
	    unit = TimeUnitGroup.Month
		)
    type = ChartType.StackBar         // sleepData이므로 PieChart는 선택 불가
    x = Date                 // JSON 파일일 경우
    y = [탄수화물, 단백질, 지방]
    color = "green"
    title = "최근 일주일 수면 시간"
}

SalusChart {
    data = sleepData.transform(
	    sorting = true,
	    unit = TimeUnitGroup.Month
		)
    type = ChartType.StackBar         // sleepData이므로 PieChart는 선택 불가
    x = Date                 // JSON 파일일 경우
    y = [탄수화물, 단백질, 지방]
    color = "green"
    title = "최근 일주일 수면 시간"
}
```
### 전문가용

- 목적
    - 세밀한 UI 조작 가능
    - 차트 타입을 명시적으로 결정
    - Compose 방법, Fluent Builder Pattern 방법   (Android - Kotlin (Compose, XML), Java)

```kotlin
@Composable
LineChart(
    data = sleepData.transform(
        timeUnit = TimeUnitGroup.DAY
    ),
    modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)
) {
    xAxis("Start Time", showGrid = true)
    yAxis("Sleep Duration", range = 0f..10f)
    showPoints(true)
    zoomable(true)
    tooltip { point -> DefaultTooltip(point) }
    ...
}
```

```kotlin
val config = LineChartConfig.Builder(sleepData)
    .xAxis("Start Time")
    .yAxis("Sleep Duration")
    .zoomable(true)
    .showPoints(true)
    .color("indigo")
    .build()

val chartView = LineChartView(context)
chartView.setEngine(LineChartEngine(config))
chartView.updateData(sleepData)
```