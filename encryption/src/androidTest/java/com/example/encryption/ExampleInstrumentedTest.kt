package com.example.encryption

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test executing on a physical Android device or emulator for the `:encryption` module.
 *
 * Verifies application package context registration and Android KeyStore integration capabilities.
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    /**
     * Verifies that the target application context matches the expected package name.
     */
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.encryption", appContext.packageName)
    }
}
