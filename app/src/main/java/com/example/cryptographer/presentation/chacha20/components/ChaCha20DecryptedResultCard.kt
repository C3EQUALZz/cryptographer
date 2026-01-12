package com.example.cryptographer.presentation.chacha20.components

import android.content.ClipData
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.stringResource
import com.example.cryptographer.R
import kotlinx.coroutines.launch

@Composable
fun ChaCha20DecryptedResultCard(decryptedText: String, clipboard: Clipboard) {
    val scope = rememberCoroutineScope()

    ChaCha20ResultCard(
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
