package com.example.cryptographer.presentation.key

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.example.cryptographer.domain.text.entity.EncryptionAlgorithm

/**
 * Screen for generating and viewing encryption keys.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeyGenerationScreen(
    viewModel: KeyGenerationViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val savedKeys by viewModel.savedKeys.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Text(
            text = "Encryption Key Generator",
            style = MaterialTheme.typography.headlineMedium
        )

        // Algorithm selection
        var selectedAlgorithm by remember { mutableStateOf(EncryptionAlgorithm.AES_256) }
        
        Text(
            text = "Select Algorithm:",
            style = MaterialTheme.typography.titleMedium
        )
        
        EncryptionAlgorithm.entries.forEach { algorithm ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedAlgorithm == algorithm,
                    onClick = { selectedAlgorithm = algorithm }
                )
                Text(
                    text = algorithm.name,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        // Generate button
        Button(
            onClick = { viewModel.generateKey(selectedAlgorithm) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Generate Key")
        }

        // Generated key display
        uiState.generatedKey?.let { key ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Generated Key",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Text(
                        text = "Algorithm: ${key.algorithm.name}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Text(
                        text = "Key ID: ${uiState.keyId?.take(8)}...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Key value
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.keyBase64 ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f),
                            maxLines = 3
                        )
                        TextButton(
                            onClick = {
                                uiState.keyBase64?.let {
                                    clipboardManager.setText(AnnotatedString(it))
                                }
                            }
                        ) {
                            Text("Copy")
                        }
                    }
                }
            }
        }

        // Error message
        uiState.error?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        // Saved keys section
        if (savedKeys.isNotEmpty()) {
            HorizontalDivider()
            
            Text(
                text = "Saved Keys",
                style = MaterialTheme.typography.titleMedium
            )

            savedKeys.forEach { savedKey ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { viewModel.loadKey(savedKey.id) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = savedKey.algorithm.name,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "ID: ${savedKey.id.take(8)}...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = savedKey.keyBase64.take(32) + "...",
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1
                            )
                        }
                        IconButton(
                            onClick = { viewModel.deleteKey(savedKey.id) }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}

