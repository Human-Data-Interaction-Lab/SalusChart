plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
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
}

dependencies {
    implementation(project(":core:chart"))
    implementation(project(":core:util"))
    implementation(project(":data:model"))
    implementation(libs.kotlinx.datetime)
    testImplementation(libs.junit)
}

mavenPublishing {
    coordinates(project.findProperty("GROUP").toString(), "saluschart-core-transform", project.findProperty("VERSION_NAME").toString())
    publishToMavenCentral()
    signAllPublications()
}
