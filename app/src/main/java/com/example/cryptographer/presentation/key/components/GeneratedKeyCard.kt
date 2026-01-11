package com.example.cryptographer.presentation.key.components

import android.content.ClipData
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.cryptographer.domain.text.entities.EncryptionKey
import com.example.cryptographer.presentation.key.KeyGenerationUiState
import kotlinx.coroutines.launch

private const val KEY_ID_PREVIEW_LENGTH = 8

@Composable
fun GeneratedKeyCard(key: EncryptionKey, uiState: KeyGenerationUiState, clipboard: Clipboard) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp),
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(com.example.cryptographer.R.string.generated_key),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            )

            Text(
                text = stringResource(
                    com.example.cryptographer.R.string.algorithm_label,
                    key.algorithm.name,
                ),
                style = MaterialTheme.typography.bodyLarge,
            )

            Text(
                text = stringResource(
                    com.example.cryptographer.R.string.key_id_label,
                    uiState.keyId?.take(KEY_ID_PREVIEW_LENGTH) ?: "",
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            KeyValueCard(
                keyBase64 = uiState.keyBase64 ?: "",
                clipboard = clipboard,
            )
        }
    }
}

@Composable
private fun KeyValueCard(keyBase64: String, clipboard: Clipboard) {
    val scope = rememberCoroutineScope()
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = keyBase64,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
                maxLines = 3,
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(
                onClick = {
                    scope.launch {
                        clipboard.setClipEntry(
                            ClipData.newPlainText("", keyBase64).toClipEntry(),
                        )
                    }
                },
                shape = RoundedCornerShape(8.dp),
            ) {
                Text(stringResource(com.example.cryptographer.R.string.copy))
            }
        }
    }
}
