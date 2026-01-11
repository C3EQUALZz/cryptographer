package com.example.cryptographer.presentation.main.components

import com.example.cryptographer.domain.common.valueobjects.Language
import com.example.cryptographer.domain.common.valueobjects.ThemeMode
import com.example.cryptographer.domain.text.valueobjects.EncryptionAlgorithm
import com.example.cryptographer.presentation.main.AppScreen

data class DrawerActions(
    val onScreenSelected: (AppScreen) -> Unit,
    val onAlgorithmSelected: (EncryptionAlgorithm) -> Unit,
    val onLanguageSelected: (Language) -> Unit,
    val onThemeModeChanged: (ThemeMode) -> Unit,
)

