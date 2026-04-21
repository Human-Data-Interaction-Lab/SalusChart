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
    implementation(libs.kotlinx.datetime)
}

mavenPublishing {
    coordinates(project.findProperty("GROUP").toString(), "saluschart-data-model", project.findProperty("VERSION_NAME").toString())
    publishToMavenCentral()
    signAllPublications()
}
