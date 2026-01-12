package com.example.cryptographer.infrastructure

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File

/**
 * Test helpers for context and storage operations.
 * Provides utilities for accessing test context and cleaning up test data.
 */

/**
 * Gets the test application context.
 */
fun getTestContext(): Context {
    return InstrumentationRegistry.getInstrumentation().targetContext
}

/**
 * Gets the target application context.
 */
fun getTargetContext(): Context {
    return InstrumentationRegistry.getInstrumentation().targetContext
}

/**
 * Clears SharedPreferences for testing.
 * Useful for cleaning up test data between tests.
 */
fun clearSharedPreferences(context: Context, prefsName: String) {
    val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    prefs.edit().clear().apply()
}

/**
 * Clears all SharedPreferences used by the app.
 * Useful for complete test cleanup.
 */
fun clearAllSharedPreferences(context: Context) {
    val prefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
    if (prefsDir.exists() && prefsDir.isDirectory) {
        prefsDir.listFiles()?.forEach { file ->
            if (file.name.endsWith(".xml")) {
                val prefsName = file.name.substring(0, file.name.length - 4)
                clearSharedPreferences(context, prefsName)
            }
        }
    }
}
