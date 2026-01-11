package com.example.cryptographer.presentation.key.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.cryptographer.R
import com.example.cryptographer.presentation.key.SavedKeyItem

private const val KEY_ID_PREVIEW_LENGTH = 8
private const val KEY_BASE64_PREVIEW_LENGTH = 32

@Composable
fun SavedKeysSection(
    savedKeys: List<SavedKeyItem>,
    onKeyClick: (String) -> Unit,
    onDeleteKey: (String) -> Unit,
    onDeleteAllClick: () -> Unit,
) {
    if (savedKeys.isEmpty()) {
        return
    }

    HorizontalDivider(
        modifier = Modifier.padding(vertical = 8.dp),
        thickness = 2.dp,
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
    )

    Text(
        text = stringResource(R.string.saved_keys),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
    )

    savedKeys.forEach { savedKey ->
        SavedKeyItem(
            savedKey = savedKey,
            onKeyClick = { onKeyClick(savedKey.id) },
            onDeleteClick = { onDeleteKey(savedKey.id) },
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    DeleteAllKeysButton(onClick = onDeleteAllClick)
}

@Composable
private fun SavedKeyItem(savedKey: SavedKeyItem, onKeyClick: () -> Unit, onDeleteClick: () -> Unit) {
    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp),
            ),
        onClick = onKeyClick,
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = savedKey.algorithm.name,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "ID: ${savedKey.id.take(KEY_ID_PREVIEW_LENGTH)}...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = savedKey.keyBase64.take(KEY_BASE64_PREVIEW_LENGTH) + "...",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    Icons.Default.Delete,
                    stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun DeleteAllKeysButton(onClick: () -> Unit) {
    androidx.compose.material3.OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error,
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.5.dp,
            color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
        ),
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.delete_all_keys),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}
