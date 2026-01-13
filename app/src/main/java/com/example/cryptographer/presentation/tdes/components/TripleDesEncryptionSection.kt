package com.example.cryptographer.presentation.tdes.components

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
import com.example.cryptographer.presentation.tdes.TripleDesUiState
import kotlinx.coroutines.launch

@Composable
fun TripleDesEncryptionSection(
    uiState: TripleDesUiState,
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
            TripleDesSectionHeader(
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

            TripleDesEncryptionButton(
                isLoading = uiState.isLoading,
                enabled = !uiState.isLoading && uiState.inputText.isNotBlank() && uiState.selectedKey != null,
                onClick = onEncryptClick,
            )

            if (uiState.encryptedText.isNotEmpty()) {
                TripleDesEncryptedResultCard(
                    encryptedText = uiState.encryptedText,
                    ivText = uiState.ivText,
                    clipboard = clipboard,
                )
            }
        }
    }
}

@Composable
private fun TripleDesEncryptionButton(isLoading: Boolean, enabled: Boolean, onClick: () -> Unit) {
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
private fun TripleDesEncryptedResultCard(encryptedText: String, ivText: String, clipboard: Clipboard) {
    val scope = rememberCoroutineScope()
    TripleDesResultCard(
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

    if (ivText.isNotEmpty()) {
        TripleDesResultCard(
            title = stringResource(R.string.iv_label),
            content = ivText,
            onCopyClick = {
                scope.launch {
                    clipboard.setClipEntry(
                        ClipData.newPlainText("", ivText).toClipEntry(),
                    )
                }
            },
            copyButtonText = stringResource(R.string.copy_iv),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}
