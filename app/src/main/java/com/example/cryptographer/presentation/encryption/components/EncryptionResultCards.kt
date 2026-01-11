package com.example.cryptographer.presentation.encryption.components

import android.content.ClipData
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.cryptographer.R
import kotlinx.coroutines.launch

@Composable
fun EncryptedResultCard(encryptedText: String, ivText: String, clipboard: Clipboard) {
    val scope = rememberCoroutineScope()
    ResultCard(
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
        ResultCard(
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

@Composable
fun DecryptedResultCard(decryptedText: String, clipboard: Clipboard) {
    val scope = rememberCoroutineScope()
    ResultCard(
        title = stringResource(R.string.decrypted_text),
        content = decryptedText,
        onCopyClick = {
            scope.launch {
                clipboard.setClipEntry(
                    ClipData.newPlainText("", decryptedText).toClipEntry(),
                )
            }
        },
        copyButtonText = stringResource(R.string.copy),
    )
}

@Composable
private fun ResultCard(
    title: String,
    content: String,
    onCopyClick: () -> Unit,
    copyButtonText: String,
    containerColor: Color = MaterialTheme.colorScheme.surface,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
        ),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                OutlinedButton(
                    onClick = onCopyClick,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(copyButtonText)
                }
            }
        }
    }
}
