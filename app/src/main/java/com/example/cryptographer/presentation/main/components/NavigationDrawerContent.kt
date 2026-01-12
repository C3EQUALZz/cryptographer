package com.example.cryptographer.presentation.main.components

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.cryptographer.domain.common.valueobjects.Language
import com.example.cryptographer.domain.common.valueobjects.ThemeMode
import com.example.cryptographer.presentation.main.AppScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawerContent(
    drawerState: DrawerState,
    scope: CoroutineScope,
    context: Context,
    selectedScreen: AppScreen,
    currentLanguage: Language,
    currentThemeMode: ThemeMode,
    actions: DrawerActions,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            DrawerCloseButton(
                onClose = {
                    scope.launch { drawerState.close() }
                },
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(top = 48.dp),
            ) {
                DrawerTitle()
                LanguageSelectionSection(
                    currentLanguage = currentLanguage,
                    onLanguageSelected = actions.onLanguageSelected,
                )
                DividerSpacer()
                ThemeSelectionSection(
                    currentThemeMode = currentThemeMode,
                    context = context,
                    onThemeModeChanged = actions.onThemeModeChanged,
                )
                DividerSpacer()
                ScreenSelectionSection(
                    selectedScreen = selectedScreen,
                    onScreenSelected = actions.onScreenSelected,
                )
            }
        }
    }
}
