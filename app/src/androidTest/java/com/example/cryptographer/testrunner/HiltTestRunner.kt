package com.example.cryptographer.testrunner

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Custom test runner that uses HiltTestApplication for instrumentation tests.
 *
 * This runner is configured in build.gradle.kts:
 * testInstrumentationRunner = "com.example.cryptographer.testrunner.HiltTestRunner"
 *
 * It enables Hilt dependency injection in Android instrumentation tests.
 */
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, className: String?, context: Context?): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}
