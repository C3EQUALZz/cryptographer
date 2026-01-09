package com.example.cryptographer.setup.configs

import android.util.Log

/**
 * Logger configuration and utilities.
 * Similar to Python's logging.getLogger(__name__) approach.
 * 
 * Usage:
 * ```
 * private val logger = getLogger()
 * logger.d("Debug message")
 * logger.e("Error message", exception)
 * ```
 */
object LoggerConfig {
    private const val MAX_TAG_LENGTH = 23 // Android's tag limit
    private const val MAX_LOG_LENGTH = 4000 // Android's log message limit
    
    /**
     * Formats tag from class name.
     * Removes package prefix and limits length.
     */
    fun formatTag(clazz: Class<*>): String {
        val simpleName = clazz.simpleName
        return if (simpleName.length > MAX_TAG_LENGTH) {
            simpleName.take(MAX_TAG_LENGTH)
        } else {
            simpleName
        }
    }
    
    /**
     * Splits long messages into chunks.
     */
    fun splitMessage(message: String): List<String> {
        return if (message.length <= MAX_LOG_LENGTH) {
            listOf(message)
        } else {
            message.chunked(MAX_LOG_LENGTH)
        }
    }
}

/**
 * Logger instance for a specific class.
 * Automatically uses class name as tag.
 */
class Logger(private val tag: String) {
    
    fun v(message: String, throwable: Throwable? = null) {
        log(Log.VERBOSE, message, throwable)
    }
    
    fun d(message: String, throwable: Throwable? = null) {
        log(Log.DEBUG, message, throwable)
    }
    
    fun i(message: String, throwable: Throwable? = null) {
        log(Log.INFO, message, throwable)
    }
    
    fun w(message: String, throwable: Throwable? = null) {
        log(Log.WARN, message, throwable)
    }
    
    fun e(message: String, throwable: Throwable? = null) {
        log(Log.ERROR, message, throwable)
    }
    
    private fun log(priority: Int, message: String, throwable: Throwable?) {
        val chunks = LoggerConfig.splitMessage(message)
        
        chunks.forEachIndexed { index, chunk ->
            if (index == 0 && throwable != null) {
                when (priority) {
                    Log.VERBOSE -> Log.v(tag, chunk, throwable)
                    Log.DEBUG -> Log.d(tag, chunk, throwable)
                    Log.INFO -> Log.i(tag, chunk, throwable)
                    Log.WARN -> Log.w(tag, chunk, throwable)
                    Log.ERROR -> Log.e(tag, chunk, throwable)
                }
            } else {
                when (priority) {
                    Log.VERBOSE -> Log.v(tag, chunk)
                    Log.DEBUG -> Log.d(tag, chunk)
                    Log.INFO -> Log.i(tag, chunk)
                    Log.WARN -> Log.w(tag, chunk)
                    Log.ERROR -> Log.e(tag, chunk)
                }
            }
        }
    }
}

/**
 * Extension function to get logger for a class.
 * Similar to Python's logging.getLogger(__name__).
 * 
 * Usage in class:
 * ```
 * class MyClass {
 *     private val logger = getLogger()
 * }
 * ```
 */
inline fun <reified T> getLogger(): Logger {
    return Logger(LoggerConfig.formatTag(T::class.java))
}

