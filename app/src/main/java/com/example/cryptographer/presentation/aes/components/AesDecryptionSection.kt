package com.example.cryptographer.presentation.aes.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.cryptographer.R
import com.example.cryptographer.presentation.aes.AesUiState
import com.example.cryptographer.presentation.encryption.components.DecryptedResultCard
import com.example.cryptographer.presentation.encryption.components.SectionHeader

@Composable
fun AesDecryptionSection(
    uiState: AesUiState,
    onEncryptedTextChange: (String) -> Unit,
    onIvChange: (String) -> Unit,
    onDecryptClick: () -> Unit,
    clipboard: Clipboard,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp),
            ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionHeader(
                icon = Icons.Default.CheckCircle,
                title = stringResource(R.string.decryption_section),
                color = MaterialTheme.colorScheme.secondary,
            )

            OutlinedTextField(
                value = uiState.encryptedTextInput,
                onValueChange = onEncryptedTextChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.enter_encrypted_text)) },
                placeholder = { Text(stringResource(R.string.enter_encrypted_text_placeholder)) },
                minLines = 3,
                maxLines = 5,
                enabled = !uiState.isLoading && uiState.selectedKey != null,
                shape = RoundedCornerShape(8.dp),
            )

            OutlinedTextField(
                value = uiState.ivInput,
                onValueChange = onIvChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.iv_label)) },
                placeholder = { Text(stringResource(R.string.enter_iv_placeholder)) },
                enabled = !uiState.isLoading && uiState.selectedKey != null,
                shape = RoundedCornerShape(8.dp),
            )

            AesDecryptionButton(
                isLoading = uiState.isLoading,
                enabled = !uiState.isLoading &&
                    uiState.encryptedTextInput.isNotBlank() &&
                    uiState.selectedKey != null,
                onClick = onDecryptClick,
            )

            if (uiState.decryptedText.isNotEmpty()) {
                DecryptedResultCard(
                    decryptedText = uiState.decryptedText,
                    clipboard = clipboard,
                )
            }
        }
    }
}

@Composable
private fun AesDecryptionButton(isLoading: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary,
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onSecondary,
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = stringResource(R.string.decrypt),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

