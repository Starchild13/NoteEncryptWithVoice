// Top-level build file for NoteEncrypt where configuration options common to all sub-projects and modules are defined.
plugins {
    // Android Application Gradle Plugin configuration for the :app module
    alias(libs.plugins.android.application) apply false

    // Android Library Gradle Plugin configuration for sub-modules (:data and :encryption)
    alias(libs.plugins.android.library) apply false

    // Jetpack Compose Compiler Gradle Plugin for processing Compose code across modules
    alias(libs.plugins.compose.compiler) apply false

    // Kotlin Symbol Processing (KSP) Plugin for processing Room database annotations
    id("com.google.devtools.ksp") version "2.1.20-1.0.31" apply false
}

