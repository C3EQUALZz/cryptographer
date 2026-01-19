package com.example.cryptographer.presentation.main.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.cryptographer.R
import com.example.cryptographer.presentation.main.AppScreen

@Composable
fun ScreenSelectionSection(selectedScreen: AppScreen, onScreenSelected: (AppScreen) -> Unit) {
    Text(
        text = stringResource(R.string.screens),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp),
        color = MaterialTheme.colorScheme.primary,
    )

    screenItems().forEach { item ->
        ScreenSelectionItem(
            item = item,
            selectedScreen = selectedScreen,
            onScreenSelected = onScreenSelected,
        )
    }
}

private data class ScreenItem(
    val screen: AppScreen,
    val icon: ImageVector,
    val labelRes: Int,
)

private fun screenItems(): List<ScreenItem> {
    return listOf(
        ScreenItem(AppScreen.KeyGeneration, Icons.Default.Settings, R.string.key_generation),
        ScreenItem(AppScreen.AesEncryption, Icons.Default.Lock, R.string.aes_encryption),
        ScreenItem(AppScreen.AesFile, Icons.Default.Lock, R.string.aes_file_processing),
        ScreenItem(AppScreen.ChaCha20Encryption, Icons.Default.Lock, R.string.chacha20_encryption),
        ScreenItem(AppScreen.ChaCha20File, Icons.Default.Lock, R.string.chacha20_file_processing),
        ScreenItem(AppScreen.TripleDesEncryption, Icons.Default.Lock, R.string.tdes_encryption),
        ScreenItem(AppScreen.TripleDesFile, Icons.Default.Lock, R.string.tdes_file_processing),
        ScreenItem(AppScreen.Encoding, Icons.Default.ArrowDropDown, R.string.encoding),
    )
}

@Composable
private fun ScreenSelectionItem(item: ScreenItem, selectedScreen: AppScreen, onScreenSelected: (AppScreen) -> Unit) {
    NavigationDrawerItem(
        icon = { Icon(item.icon, contentDescription = null) },
        label = { Text(stringResource(item.labelRes)) },
        selected = selectedScreen == item.screen,
        onClick = { onScreenSelected(item.screen) },
        modifier = Modifier.fillMaxWidth(),
    )
}
