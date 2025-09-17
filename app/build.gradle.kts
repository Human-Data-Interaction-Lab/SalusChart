plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.hdil.saluschart"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.hdil.saluschart"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

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
    
    lint {
        lintConfig = file("../lint.xml")
        abortOnError = false
        warningsAsErrors = false
        checkAllWarnings = true
        ignoreWarnings = false
        quiet = false
        
        // HTML and XML reports
        htmlReport = true
        xmlReport = true
        
        // Console output
        textReport = true
        textOutput = file("stdout")
        
        // Baseline file for managing existing issues
        baseline = file("lint-baseline.xml")
    }
}

dependencies {
    implementation(project(":data:model"))
    implementation(project(":core:chart"))
    implementation(project(":core:util"))
    implementation(project(":core:transform"))
    implementation(project(":ui:compose"))
    implementation(project(":data:provider"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}