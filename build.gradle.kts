// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.android.library) apply false
    id("com.vanniktech.maven.publish") version "0.36.0" apply false
    id("org.jetbrains.dokka") version "2.2.0"
}

val dokkaModules = setOf(
    "core:chart",
    "core:transform",
    "core:util",
    "data:model",
    "ui:compose",
    "ui:theme",
    "ui:wear-compose",
)

dependencies {
    dokkaModules.forEach { modulePath ->
        dokka(project(":$modulePath"))
    }
}

tasks.register<Sync>("syncDokkaToVitePress") {
    description = "Copy generated Dokka HTML into VitePress public assets."
    group = "documentation"
    dependsOn(tasks.named("dokkaGenerate"))
    from(layout.buildDirectory.dir("dokka/html"))
    into(layout.projectDirectory.dir("docs/public/api"))
}

// Apply lint configuration to all Android modules
subprojects {
    if (path.removePrefix(":") in dokkaModules) {
        apply(plugin = "org.jetbrains.dokka")
    }

    afterEvaluate {
        if (plugins.hasPlugin("com.android.application") || plugins.hasPlugin("com.android.library")) {
            configure<com.android.build.gradle.BaseExtension> {
//                lint {
//                    lintConfig = rootProject.file("lint.xml")
//                    abortOnError = false
//                    warningsAsErrors = false
//                    checkAllWarnings = true
//                    ignoreWarnings = false
//                    quiet = false
//
//                    // Generate reports
//                    htmlReport = true
//                    xmlReport = true
//                    textReport = true
//
//                    // Baseline for managing existing issues
//                    baseline = file("lint-baseline.xml")
//                }
            }
        }
    }
}

// Custom task for project-wide lint check
tasks.register("lintAll") {
    description = "Run lint on all modules"
    group = "verification"
    
    dependsOn(subprojects.mapNotNull { subproject ->
        subproject.tasks.findByName("lint")
    })
}
