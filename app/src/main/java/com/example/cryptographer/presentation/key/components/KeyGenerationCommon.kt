package com.example.cryptographer.presentation.key.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.example.cryptographer.R
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm

@Composable
fun KeyGenerationScreenTitle() {
    Text(
        text = stringResource(R.string.key_generation_title),
        style = MaterialTheme.typography.headlineMedium,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
fun getAlgorithmDisplayName(algorithm: EncryptionAlgorithm): String {
    return stringResource(
        when (algorithm) {
            EncryptionAlgorithm.AES_128 -> R.string.algorithm_aes_128
            EncryptionAlgorithm.AES_192 -> R.string.algorithm_aes_192
            EncryptionAlgorithm.AES_256 -> R.string.algorithm_aes_256
            EncryptionAlgorithm.CHACHA20_256 -> R.string.algorithm_chacha20_256
        },
    )
}
