package com.example.cryptographer.presentation.aes.components

import android.content.ClipData
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.stringResource
import com.example.cryptographer.R
import kotlinx.coroutines.launch

@Composable
fun AesEncryptedResultCard(encryptedText: String, ivText: String, clipboard: Clipboard) {
    val scope = rememberCoroutineScope()
    AesResultCard(
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
        AesResultCard(
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
