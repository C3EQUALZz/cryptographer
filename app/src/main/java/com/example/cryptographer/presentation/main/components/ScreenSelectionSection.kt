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

    NavigationDrawerItem(
        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
        label = { Text(stringResource(R.string.key_generation)) },
        selected = selectedScreen == AppScreen.KeyGeneration,
        onClick = { onScreenSelected(AppScreen.KeyGeneration) },
        modifier = Modifier.fillMaxWidth(),
    )

    NavigationDrawerItem(
        icon = { Icon(Icons.Default.Lock, contentDescription = null) },
        label = { Text(stringResource(R.string.encryption_decryption)) },
        selected = selectedScreen == AppScreen.Encryption,
        onClick = { onScreenSelected(AppScreen.Encryption) },
        modifier = Modifier.fillMaxWidth(),
    )

    NavigationDrawerItem(
        icon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
        label = { Text(stringResource(R.string.encoding)) },
        selected = selectedScreen == AppScreen.Encoding,
        onClick = { onScreenSelected(AppScreen.Encoding) },
        modifier = Modifier.fillMaxWidth(),
    )
}

