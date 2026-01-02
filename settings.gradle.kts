pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SalusChart"
include(":app")
include(":core:chart")
include(":core:transform")
include(":core:dsl")
include(":core:util")
include(":data:model")
include(":data:provider")
include(":ui:compose")
include(":ui:theme")
include(":sample")
