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
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.cryptographer.presentation.tdes.TripleDesFileUiState
import kotlinx.coroutines.launch

@Composable
fun TripleDesFileEncryptionSection(
    uiState: TripleDesFileUiState,
    onInputChange: (String) -> Unit,
    onOutputChange: (String) -> Unit,
    onPickInput: () -> Unit,
    onPickOutput: () -> Unit,
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
                title = stringResource(R.string.file_encryption_section),
                color = MaterialTheme.colorScheme.primary,
            )

            TripleDesFilePathFields(
                uiState = uiState,
                onInputChange = onInputChange,
                onOutputChange = onOutputChange,
                onPickInput = onPickInput,
                onPickOutput = onPickOutput,
            )

            TripleDesFileEncryptButton(
                isLoading = uiState.isLoading,
                enabled = !uiState.isLoading &&
                    uiState.encryptInputPath.isNotBlank() &&
                    uiState.encryptOutputPath.isNotBlank() &&
                    uiState.selectedKey != null,
                onClick = onEncryptClick,
            )

            TripleDesFileEncryptionResults(
                uiState = uiState,
                clipboard = clipboard,
            )
        }
    }
}

@Composable
private fun TripleDesFilePathFields(
    uiState: TripleDesFileUiState,
    onInputChange: (String) -> Unit,
    onOutputChange: (String) -> Unit,
    onPickInput: () -> Unit,
    onPickOutput: () -> Unit,
) {
    val enabled = !uiState.isLoading

    OutlinedTextField(
        value = uiState.encryptInputPath,
        onValueChange = onInputChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.input_file_path)) },
        placeholder = { Text(stringResource(R.string.input_file_path_placeholder)) },
        enabled = enabled,
        trailingIcon = {
            IconButton(onClick = onPickInput, enabled = enabled) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = stringResource(R.string.select_input_file),
                )
            }
        },
        shape = RoundedCornerShape(8.dp),
    )

    OutlinedTextField(
        value = uiState.encryptOutputPath,
        onValueChange = onOutputChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.output_file_path)) },
        placeholder = { Text(stringResource(R.string.output_file_path_placeholder)) },
        enabled = enabled,
        trailingIcon = {
            IconButton(onClick = onPickOutput, enabled = enabled) {
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = stringResource(R.string.select_output_file),
                )
            }
        },
        shape = RoundedCornerShape(8.dp),
    )
}

@Composable
private fun TripleDesFileEncryptionResults(uiState: TripleDesFileUiState, clipboard: Clipboard) {
    val scope = rememberCoroutineScope()

    if (uiState.encryptResultPath.isNotEmpty()) {
        TripleDesResultCard(
            title = stringResource(R.string.encrypted_file_path),
            content = uiState.encryptResultPath,
            onCopyClick = {
                scope.launch {
                    clipboard.setClipEntry(
                        ClipData.newPlainText("", uiState.encryptResultPath).toClipEntry(),
                    )
                }
            },
            copyButtonText = stringResource(R.string.copy),
        )
    }

    if (uiState.ivBase64.isNotEmpty()) {
        TripleDesResultCard(
            title = stringResource(R.string.iv_label),
            content = uiState.ivBase64,
            onCopyClick = {
                scope.launch {
                    clipboard.setClipEntry(
                        ClipData.newPlainText("", uiState.ivBase64).toClipEntry(),
                    )
                }
            },
            copyButtonText = stringResource(R.string.copy_iv),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}

@Composable
private fun TripleDesFileEncryptButton(isLoading: Boolean, enabled: Boolean, onClick: () -> Unit) {
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
