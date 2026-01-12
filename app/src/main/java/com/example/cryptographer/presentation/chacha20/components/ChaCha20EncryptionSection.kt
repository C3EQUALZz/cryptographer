package com.example.cryptographer.presentation.chacha20.components

import android.content.ClipData
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.cryptographer.R
import com.example.cryptographer.presentation.chacha20.ChaCha20UiState
import kotlinx.coroutines.launch

@Composable
fun ChaCha20EncryptionSection(
    uiState: ChaCha20UiState,
    onInputChange: (String) -> Unit,
    onEncryptClick: () -> Unit,
    clipboard: Clipboard,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp),
            ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ChaCha20SectionHeader(
                icon = Icons.Default.Lock,
                title = stringResource(R.string.encryption_section),
                color = MaterialTheme.colorScheme.primary,
            )

            OutlinedTextField(
                value = uiState.inputText,
                onValueChange = onInputChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.enter_text_to_encrypt)) },
                placeholder = { Text(stringResource(R.string.enter_text_placeholder)) },
                minLines = 3,
                maxLines = 5,
                enabled = !uiState.isLoading && uiState.selectedKey != null,
                shape = RoundedCornerShape(8.dp),
            )

            ChaCha20EncryptionButton(
                isLoading = uiState.isLoading,
                enabled = !uiState.isLoading && uiState.inputText.isNotBlank() && uiState.selectedKey != null,
                onClick = onEncryptClick,
            )

            if (uiState.encryptedText.isNotEmpty()) {
                ChaCha20EncryptedResultCard(
                    encryptedText = uiState.encryptedText,
                    nonceText = uiState.nonceText,
                    clipboard = clipboard,
                )
            }
        }
    }
}

@Composable
private fun ChaCha20EncryptionButton(isLoading: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = stringResource(R.string.encrypt),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun ChaCha20EncryptedResultCard(encryptedText: String, nonceText: String, clipboard: Clipboard) {
    val scope = rememberCoroutineScope()
    ChaCha20ResultCard(
        title = stringResource(R.string.encrypted_text),
        content = encryptedText,
        onCopyClick = {
            scope.launch {
                clipboard.setClipEntry(
                    ClipData.newPlainText("", encryptedText).toClipEntry(),
                )
            }
        },
        copyButtonText = stringResource(R.string.copy),
    )

    if (nonceText.isNotEmpty()) {
        ChaCha20ResultCard(
            title = stringResource(R.string.nonce_label),
            content = nonceText,
            onCopyClick = {
                scope.launch {
                    clipboard.setClipEntry(
                        ClipData.newPlainText("", nonceText).toClipEntry(),
                    )
                }
            },
            copyButtonText = stringResource(R.string.copy_nonce),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}
