plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    id("com.vanniktech.maven.publish")
}
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}

dependencies {
//    implementation(project(":core:chart")) // ChartMark 사용
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
}

mavenPublishing {
    coordinates("io.github.hdilys", "saluschart-data-model", "0.1.2")
    publishToMavenCentral()
    signAllPublications()
}