plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.vanniktech.maven.publish")
}

android {
    namespace = "com.hdil.saluschart.ui.compose"
    compileSdk = 36

    defaultConfig {
        minSdk = 30

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    implementation(project(":core:chart"))
    implementation(project(":core:transform"))
    implementation(project(":ui:theme"))
    implementation(project(":data:model"))

    implementation(libs.androidx.ui.versioned)
    implementation(libs.androidx.ui.graphics.versioned)
    implementation(libs.androidx.ui.tooling.preview.versioned)
    implementation(libs.androidx.material3.versioned)
    implementation(libs.androidx.foundation.versioned)
    implementation(libs.androidx.foundation.layout.versioned)
    implementation(libs.androidx.animation.core.versioned)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

mavenPublishing {
    coordinates("io.github.hdilys", "saluschart-ui-compose", "0.1.2")
    publishToMavenCentral()
    signAllPublications()
}