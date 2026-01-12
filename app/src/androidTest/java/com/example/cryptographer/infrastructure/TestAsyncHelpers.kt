package com.example.cryptographer.infrastructure

/**
 * Test helpers for asynchronous operations.
 * Provides utilities for waiting on async conditions in tests.
 */

/**
 * Waits for a condition to become true.
 * Useful for waiting for async operations in tests.
 *
 * @param timeoutMs Maximum time to wait in milliseconds (default: 5000)
 * @param intervalMs Interval between condition checks in milliseconds (default: 100)
 * @param condition The condition to wait for
 * @return true if condition became true, false if timeout was reached
 */
fun waitForCondition(timeoutMs: Long = 5000, intervalMs: Long = 100, condition: () -> Boolean): Boolean {
    val startTime = System.currentTimeMillis()
    while (System.currentTimeMillis() - startTime < timeoutMs) {
        if (condition()) {
            return true
        }
        Thread.sleep(intervalMs)
    }
    return false
}
