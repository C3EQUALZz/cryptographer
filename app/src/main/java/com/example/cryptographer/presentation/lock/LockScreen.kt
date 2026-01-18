package com.example.cryptographer.presentation.lock

import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cryptographer.R
import com.example.cryptographer.presentation.common.CryptographerTheme

/**
 * Lock screen that requires password or biometric authentication to access the app.
 * Shows password setup screen if password is not set, lock screen if password is set,
 * or main app if unlocked.
 */
@Composable
fun LockScreen(viewModel: LockViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    when {
        uiState.isUnlocked -> {
            com.example.cryptographer.presentation.main.MainScreen()
        }
        !uiState.hasPassword -> {
            PasswordSetupScreen(
                onPasswordSet = { viewModel.unlockApp() },
                onSkip = {
                    viewModel.passwordManager.removePassword()
                    viewModel.unlockApp()
                },
                viewModel = viewModel,
            )
        }
        else -> {
            CryptographerTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    LockScreenContent(
                        uiState = uiState,
                        viewModel = viewModel,
                        context = context,
                    )
                }
            }
        }
    }
}

@Composable
private fun LockScreenContent(uiState: LockUiState, viewModel: LockViewModel, context: android.content.Context) {
    var password by remember { mutableStateOf("") }
    val isBiometricAvailable = remember { viewModel.isBiometricAvailable(context) }
    val isBiometricEnabled = remember { viewModel.isBiometricEnabled() }

    // Trigger biometric prompt if available and enabled
    LaunchedEffect(uiState.shouldTriggerBiometric) {
        if (uiState.shouldTriggerBiometric && isBiometricAvailable && isBiometricEnabled) {
            if (context is androidx.fragment.app.FragmentActivity) {
                showBiometricPrompt(context = context, viewModel = viewModel)
            }
            viewModel.resetBiometricTrigger()
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        LockScreenHeader()
        Spacer(modifier = Modifier.height(32.dp))
        LockScreenPasswordCard(
            uiState = uiState,
            password = password,
            onPasswordChange = {
                password = it
                viewModel.clearError()
            },
            onUnlockClick = { viewModel.verifyPassword(password, context) },
            isBiometricAvailable = isBiometricAvailable,
            isBiometricEnabled = isBiometricEnabled,
            onBiometricClick = {
                if (context is androidx.fragment.app.FragmentActivity) {
                    showBiometricPrompt(context, viewModel)
                }
            },
        )
    }
}

@Composable
private fun LockScreenHeader() {
    Icon(
        imageVector = Icons.Default.Lock,
        contentDescription = null,
        modifier = Modifier.size(80.dp),
        tint = MaterialTheme.colorScheme.primary,
    )

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = stringResource(R.string.lock_screen_title),
        style = MaterialTheme.typography.headlineMedium,
        textAlign = TextAlign.Center,
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = stringResource(R.string.lock_screen_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun LockScreenPasswordCard(
    uiState: LockUiState,
    password: String,
    onPasswordChange: (String) -> Unit,
    onUnlockClick: () -> Unit,
    isBiometricAvailable: Boolean,
    isBiometricEnabled: Boolean,
    onBiometricClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text(stringResource(R.string.lock_password_label)) },
                placeholder = { Text(stringResource(R.string.lock_password_placeholder)) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !uiState.isLoading,
            )

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            } else {
                Button(
                    onClick = onUnlockClick,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = password.isNotEmpty(),
                ) {
                    Text(stringResource(R.string.lock_unlock_button))
                }
            }

            if (isBiometricAvailable && isBiometricEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                BiometricButton(onClick = onBiometricClick)
            }
        }
    }
}

@Composable
private fun BiometricButton(onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(
            imageVector = Icons.Default.Fingerprint,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(stringResource(R.string.lock_use_fingerprint))
    }
}

/**
 * Shows biometric prompt for authentication.
 */
private fun showBiometricPrompt(context: android.content.Context, viewModel: LockViewModel) {
    val fragmentActivity = context as? androidx.fragment.app.FragmentActivity ?: return

    val executor = ContextCompat.getMainExecutor(fragmentActivity)
    val biometricPrompt = BiometricPrompt(
        fragmentActivity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                viewModel.onBiometricSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                viewModel.onBiometricError(errorCode, errString.toString(), fragmentActivity)
            }

            override fun onAuthenticationFailed() {
                viewModel.onBiometricError(
                    BiometricPrompt.ERROR_UNABLE_TO_PROCESS,
                    fragmentActivity.getString(R.string.lock_biometric_failed_to_recognize),
                    fragmentActivity,
                )
            }
        },
    )

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(fragmentActivity.getString(R.string.lock_biometric_title))
        .setSubtitle(fragmentActivity.getString(R.string.lock_biometric_subtitle))
        .setNegativeButtonText(fragmentActivity.getString(R.string.lock_biometric_cancel))
        .build()

    biometricPrompt.authenticate(promptInfo)
}
