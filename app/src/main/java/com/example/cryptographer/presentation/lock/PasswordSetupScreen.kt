package com.example.cryptographer.presentation.lock

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cryptographer.R
import com.example.cryptographer.presentation.common.CryptographerTheme

/**
 * Screen for setting up app lock password (first time setup).
 */
@Composable
fun PasswordSetupScreen(
    onPasswordSet: () -> Unit,
    onSkip: () -> Unit,
    viewModel: LockViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showBiometricOption by remember { mutableStateOf(false) }

    CryptographerTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            PasswordSetupHeader()
            Spacer(modifier = Modifier.height(32.dp))
            PasswordSetupCard(
                password = password,
                confirmPassword = confirmPassword,
                uiState = uiState,
                onPasswordChange = {
                    password = it
                    viewModel.clearError()
                },
                onConfirmPasswordChange = {
                    confirmPassword = it
                    viewModel.clearError()
                },
                onSetPassword = {
                    val result = viewModel.setPassword(password, confirmPassword, context)
                    if (result.isSuccess) {
                        showBiometricOption = viewModel.isBiometricAvailable(context)
                        if (!showBiometricOption) {
                            onPasswordSet()
                        }
                    }
                },
                showBiometricOption = showBiometricOption,
                onEnableBiometric = {
                    viewModel.setBiometricEnabled(true)
                    onPasswordSet()
                },
                onSkipBiometric = {
                    viewModel.setBiometricEnabled(false)
                    onPasswordSet()
                },
                onSkip = onSkip,
            )
        }
    }
}

@Composable
private fun PasswordSetupHeader() {
    Icon(
        imageVector = Icons.Default.Lock,
        contentDescription = null,
        modifier = Modifier.size(80.dp),
        tint = MaterialTheme.colorScheme.primary,
    )

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = stringResource(R.string.password_setup_title),
        style = MaterialTheme.typography.headlineMedium,
        textAlign = TextAlign.Center,
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = stringResource(R.string.password_setup_subtitle),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun PasswordSetupCard(
    password: String,
    confirmPassword: String,
    uiState: LockUiState,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSetPassword: () -> Unit,
    showBiometricOption: Boolean,
    onEnableBiometric: () -> Unit,
    onSkipBiometric: () -> Unit,
    onSkip: () -> Unit,
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
            PasswordFields(
                password = password,
                confirmPassword = confirmPassword,
                onPasswordChange = onPasswordChange,
                onConfirmPasswordChange = onConfirmPasswordChange,
            )

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Button(
                onClick = onSetPassword,
                modifier = Modifier.fillMaxWidth(),
                enabled = password.isNotEmpty() && confirmPassword.isNotEmpty(),
            ) {
                Text(stringResource(R.string.password_setup_set_button))
            }

            if (showBiometricOption && uiState.hasPassword) {
                BiometricSetupOptions(
                    onEnableBiometric = onEnableBiometric,
                    onSkipBiometric = onSkipBiometric,
                )
            }

            if (!uiState.hasPassword) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onSkip,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.password_setup_skip))
                }
            }
        }
    }
}

@Composable
private fun PasswordFields(
    password: String,
    confirmPassword: String,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text(stringResource(R.string.password_setup_password_label)) },
        placeholder = { Text(stringResource(R.string.password_setup_password_placeholder)) },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )

    OutlinedTextField(
        value = confirmPassword,
        onValueChange = onConfirmPasswordChange,
        label = { Text(stringResource(R.string.password_setup_confirm_password_label)) },
        placeholder = { Text(stringResource(R.string.password_setup_confirm_password_placeholder)) },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
}

@Composable
private fun BiometricSetupOptions(
    onEnableBiometric: () -> Unit,
    onSkipBiometric: () -> Unit,
) {
    TextButton(
        onClick = onEnableBiometric,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(stringResource(R.string.password_setup_enable_biometric))
    }
    TextButton(
        onClick = onSkipBiometric,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(stringResource(R.string.password_setup_skip))
    }
}
