pluginManagement {
    repositories { gradlePluginPortal(); mavenCentral() }
    plugins {
        id("org.springframework.boot") version "3.3.2"
        id("io.spring.dependency-management") version "1.1.6"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { mavenCentral() }
}

rootProject.name = "ott-transcoding"
include("common", "app", "worker")
