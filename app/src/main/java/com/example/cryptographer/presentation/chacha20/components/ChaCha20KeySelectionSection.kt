package com.example.cryptographer.presentation.chacha20.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.cryptographer.R
import com.example.cryptographer.application.common.views.KeyView

private const val KEY_ID_PREVIEW_LENGTH = 8

@Composable
fun ChaCha20KeySelectionSection(
    availableKeys: List<KeyView>,
    selectedKeyId: String?,
    onKeySelected: (String) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
            ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.select_key),
                style = MaterialTheme.typography.titleMedium,
            )

            if (availableKeys.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_keys_available),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                availableKeys.forEach { keyItem ->
                    ChaCha20KeySelectionItem(
                        keyItem = keyItem,
                        isSelected = selectedKeyId == keyItem.id,
                        onSelected = { onKeySelected(keyItem.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ChaCha20KeySelectionItem(keyItem: KeyView, isSelected: Boolean, onSelected: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onSelected,
        )
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(
                text = keyItem.algorithm.name,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = stringResource(R.string.key_id_prefix, keyItem.id.take(KEY_ID_PREVIEW_LENGTH)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
