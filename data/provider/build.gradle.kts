plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.hdil.saluschart.data.provider"
    compileSdk = 36

    defaultConfig {
        minSdk = 30
        targetSdk = 36
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(project(":data:model"))
    implementation(libs.kotlinx.serialization.json)
    implementation(project(":ui:compose"))
    implementation(project(":core:chart"))
    implementation(project(":core:transform"))
    implementation(project(":core:util"))
}
