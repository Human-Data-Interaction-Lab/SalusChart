plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.vanniktech.maven.publish")

}
android {
    namespace = "com.hdil.saluschart.core.transform"
    compileSdk = 36

    defaultConfig {
        minSdk = 30

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:chart")) // ChartMark 사용
    implementation(project(":core:util")) // TimeUnitGroup 사용
    implementation(project(":data:model")) // HealthData models 사용
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0") // 시간 필터링 등
    // Core Android dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose BOM - manages all Compose library versions
    implementation(platform(libs.androidx.compose.bom))

    // Compose UI dependencies
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)

    // Compose Foundation dependencies (for Canvas, layouts, shapes)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.runtime)

    // Material3 for UI components and theming
    implementation(libs.androidx.material3)

    // Testing dependencies
    testImplementation(libs.junit)
}

mavenPublishing {
    coordinates("io.github.hdilys", "saluschart-core-transform", "0.1.1")
    publishToMavenCentral()
    signAllPublications()
}