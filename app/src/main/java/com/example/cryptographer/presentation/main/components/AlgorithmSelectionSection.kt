package com.example.cryptographer.presentation.main.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.cryptographer.R
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm

@Composable
fun AlgorithmSelectionSection(
    selectedAlgorithm: EncryptionAlgorithm,
    onAlgorithmSelected: (EncryptionAlgorithm) -> Unit,
) {
    Text(
        text = stringResource(R.string.encryption_algorithms),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp),
        color = MaterialTheme.colorScheme.primary,
    )

    EncryptionAlgorithm.entries.forEach { algorithm ->
        NavigationDrawerItem(
            icon = { Icon(Icons.Default.Info, contentDescription = null) },
            label = { Text(algorithm.name) },
            selected = selectedAlgorithm == algorithm,
            onClick = { onAlgorithmSelected(algorithm) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
