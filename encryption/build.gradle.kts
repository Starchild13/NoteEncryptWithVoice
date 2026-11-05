// Gradle build configuration script for the cryptographic encryption module (:encryption).

plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.example.encryption"
    compileSdk = 37.toInt()

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

dependencies {
    // Android Core KTX extensions
    implementation(libs.androidx.core.ktx)

    // Unit & Instrumented testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
}

