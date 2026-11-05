// Gradle settings file defining plugin management, repository resolution, and project sub-modules for NoteEncrypt.

pluginManagement {
    repositories {
        // Official Google Maven repository for AndroidX and Google libraries
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        // Central repository for Java/Kotlin open-source libraries
        mavenCentral()
        // Official Gradle plugin portal
        gradlePluginPortal()
    }
}

plugins {
    // Toolchain resolver convention plugin to automatically download and configure Java JDKs
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    // Enforce strict centralized repository declarations in settings.gradle.kts
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// Project Root Identifier
rootProject.name = "NoteEncrypt"

// Include Sub-Projects / Modular Architecture Components:
// :app - Main Android Application module containing UI, Navigation, and ViewModels
include(":app")

// :encryption - Hardware-backed cryptographic module wrapping Android KeyStore (AES/GCM)
include(":encryption")

// :data - Data persistence layer wrapping Room DB (Entity, DAO, Repository)
include(":data")

