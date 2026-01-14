package com.example.cryptographer.presentation.lock

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptographer.infrastructure.security.PasswordManager
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for app lock screen.
 * Manages authentication state and biometric prompt.
 */
@Suppress("TooManyFunctions") // ViewModel requires multiple functions for state management
@HiltViewModel
class LockViewModel @Inject constructor(
    val passwordManager: PasswordManager,
) : ViewModel() {
    private val logger = KotlinLogging.logger {}

    private val _uiState = MutableStateFlow(LockUiState())
    val uiState: StateFlow<LockUiState> = _uiState.asStateFlow()

    companion object {
        private const val MIN_PASSWORD_LENGTH = 4
    }

    init {
        checkLockStatus()
        val hasPassword = passwordManager.hasPassword()
        val isLockEnabled = passwordManager.isLockEnabled()
        logger.debug {
            "LockViewModel initialized: hasPassword=$hasPassword, isLockEnabled=$isLockEnabled"
        }
    }

    /**
     * Checks if app lock is enabled and if password is set.
     */
    fun checkLockStatus() {
        val isLockEnabled = passwordManager.isLockEnabled()
        val hasPassword = passwordManager.hasPassword()

        logger.debug { "checkLockStatus: isLockEnabled=$isLockEnabled, hasPassword=$hasPassword" }

        _uiState.value = _uiState.value.copy(
            isLockEnabled = isLockEnabled,
            hasPassword = hasPassword,
            // Keep current isUnlocked state - don't reset it here
        )

        // Auto-trigger biometric if enabled and available
        if (isLockEnabled && hasPassword && passwordManager.isBiometricEnabled()) {
            _uiState.value = _uiState.value.copy(shouldTriggerBiometric = true)
        }
    }

    /**
     * Unlocks the app (used when user skips password setup).
     */
    fun unlockApp() {
        _uiState.value = _uiState.value.copy(isUnlocked = true)
    }

    /**
     * Verifies password and unlocks the app.
     */
    fun verifyPassword(password: String, context: android.content.Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
            )

            val isValid = passwordManager.verifyPassword(password)

            if (isValid) {
                _uiState.value = _uiState.value.copy(
                    isUnlocked = true,
                    isLoading = false,
                    errorMessage = null,
                )
                logger.info { "Password verified successfully" }
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = context.getString(com.example.cryptographer.R.string.lock_error_wrong_password),
                    passwordAttempts = _uiState.value.passwordAttempts + 1,
                )
                logger.warn { "Password verification failed" }
            }
        }
    }

    /**
     * Sets password for app lock (first time setup).
     */
    fun setPassword(
        password: String,
        confirmPassword: String,
        context: android.content.Context,
    ): Result<Unit> {
        val validationError = validatePasswordInput(password, confirmPassword, context)
        if (validationError != null) {
            _uiState.value = _uiState.value.copy(errorMessage = validationError)
            return Result.failure(IllegalArgumentException(validationError))
        }

        val result = passwordManager.setPassword(password)
        if (result.isSuccess) {
            _uiState.value = _uiState.value.copy(
                isLockEnabled = true,
                hasPassword = true,
                isUnlocked = true,
            )
        } else {
            val errorMsg = context.getString(
                com.example.cryptographer.R.string.lock_error_failed_to_set_password,
            )
            _uiState.value = _uiState.value.copy(errorMessage = errorMsg)
        }

        return result
    }

    private fun validatePasswordInput(
        password: String,
        confirmPassword: String,
        context: android.content.Context,
    ): String? {
        if (password != confirmPassword) {
            return context.getString(
                com.example.cryptographer.R.string.password_setup_error_passwords_not_match,
            )
        }
        if (password.length < MIN_PASSWORD_LENGTH) {
            return context.getString(
                com.example.cryptographer.R.string.password_setup_error_password_too_short,
            )
        }
        return null
    }

    /**
     * Handles successful biometric authentication.
     */
    fun onBiometricSuccess() {
        _uiState.value = _uiState.value.copy(
            isUnlocked = true,
            shouldTriggerBiometric = false,
        )
        logger.info { "Biometric authentication successful" }
    }

    /**
     * Handles biometric authentication error.
     */
    fun onBiometricError(errorCode: Int, errorMessage: String, context: android.content.Context) {
        logger.warn { "Biometric error: code=$errorCode, message=$errorMessage" }
        _uiState.value = _uiState.value.copy(
            shouldTriggerBiometric = false,
            errorMessage = when (errorCode) {
                BiometricPrompt.ERROR_USER_CANCELED -> null // User canceled, no error message
                BiometricPrompt.ERROR_LOCKOUT -> context.getString(
                    com.example.cryptographer.R.string.lock_error_too_many_attempts,
                )
                BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> context.getString(
                    com.example.cryptographer.R.string.lock_error_biometric_locked,
                )
                else -> context.getString(com.example.cryptographer.R.string.lock_error_biometric_failed)
            },
        )
    }

    /**
     * Clears error message.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Resets biometric trigger flag.
     */
    fun resetBiometricTrigger() {
        _uiState.value = _uiState.value.copy(shouldTriggerBiometric = false)
    }

    /**
     * Checks if biometric authentication is available.
     */
    fun isBiometricAvailable(context: android.content.Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    /**
     * Checks if biometric is enabled in settings.
     */
    fun isBiometricEnabled(): Boolean {
        return passwordManager.isBiometricEnabled()
    }

    /**
     * Enables or disables biometric authentication.
     */
    fun setBiometricEnabled(enabled: Boolean) {
        passwordManager.setBiometricEnabled(enabled)
    }
}

/**
 * UI state for lock screen.
 */
data class LockUiState(
    val isLockEnabled: Boolean = false,
    val hasPassword: Boolean = false,
    val isUnlocked: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val passwordAttempts: Int = 0,
    val shouldTriggerBiometric: Boolean = false,
)
