package com.example.cryptographer.presentation.main.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.cryptographer.R
import com.example.cryptographer.presentation.main.AppScreen

@Composable
fun DividerSpacer() {
    Spacer(modifier = Modifier.height(24.dp))
    androidx.compose.material3.HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))
}

@Composable
fun getScreenTitle(screen: AppScreen): String {
    return when (screen) {
        AppScreen.KeyGeneration -> stringResource(R.string.screen_key_generation)
        AppScreen.AesEncryption -> stringResource(R.string.screen_aes_encryption)
        AppScreen.ChaCha20Encryption -> stringResource(R.string.screen_chacha20_encryption)
        AppScreen.Encoding -> stringResource(R.string.screen_encoding)
    }
}
