package com.example.cryptographer.presentation.aes.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm

@Composable
fun AesKeyLengthSelection(
    selectedLength: EncryptionAlgorithm,
    onLengthSelected: (EncryptionAlgorithm) -> Unit,
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
                text = stringResource(R.string.select_algorithm),
                style = MaterialTheme.typography.titleMedium,
            )

            AesKeyLengthItem(
                algorithm = EncryptionAlgorithm.AES_128,
                isSelected = selectedLength == EncryptionAlgorithm.AES_128,
                onSelected = { onLengthSelected(EncryptionAlgorithm.AES_128) },
            )

            AesKeyLengthItem(
                algorithm = EncryptionAlgorithm.AES_192,
                isSelected = selectedLength == EncryptionAlgorithm.AES_192,
                onSelected = { onLengthSelected(EncryptionAlgorithm.AES_192) },
            )

            AesKeyLengthItem(
                algorithm = EncryptionAlgorithm.AES_256,
                isSelected = selectedLength == EncryptionAlgorithm.AES_256,
                onSelected = { onLengthSelected(EncryptionAlgorithm.AES_256) },
            )
        }
    }
}

@Composable
private fun AesKeyLengthItem(
    algorithm: EncryptionAlgorithm,
    isSelected: Boolean,
    onSelected: () -> Unit,
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onSelected,
        )
        Text(
            text = when (algorithm) {
                EncryptionAlgorithm.AES_128 -> stringResource(R.string.algorithm_aes_128)
                EncryptionAlgorithm.AES_192 -> stringResource(R.string.algorithm_aes_192)
                EncryptionAlgorithm.AES_256 -> stringResource(R.string.algorithm_aes_256)
                else -> algorithm.name
            },
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

