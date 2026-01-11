package com.example.cryptographer.presentation.main.components

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.cryptographer.R
import com.example.cryptographer.domain.common.valueobjects.ThemeMode

@Composable
fun ThemeSelectionSection(currentThemeMode: ThemeMode, context: Context, onThemeModeChanged: (ThemeMode) -> Unit) {
    Text(
        text = stringResource(R.string.theme_selection),
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp),
        color = MaterialTheme.colorScheme.primary,
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.dark_theme),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = when (currentThemeMode) {
                ThemeMode.SYSTEM -> {
                    (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                        Configuration.UI_MODE_NIGHT_YES
                }
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
            },
            onCheckedChange = { isDark ->
                val newTheme = if (isDark) ThemeMode.DARK else ThemeMode.LIGHT
                onThemeModeChanged(newTheme)
            },
        )
    }
}
