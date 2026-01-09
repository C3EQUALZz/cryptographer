package com.example.cryptographer

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Cryptographer app.
 * Hilt uses this class to generate the dependency injection container.
 */
@HiltAndroidApp
class CryptographerApplication : Application()

