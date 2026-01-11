package com.example.cryptographer.presentation.main.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.cryptographer.presentation.aes.AesScreen
import com.example.cryptographer.presentation.aes.AesViewModel
import com.example.cryptographer.presentation.chacha20.ChaCha20Screen
import com.example.cryptographer.presentation.chacha20.ChaCha20ViewModel
import com.example.cryptographer.presentation.encoding.EncodingScreen
import com.example.cryptographer.presentation.encoding.EncodingViewModel
import com.example.cryptographer.presentation.key.KeyGenerationScreen
import com.example.cryptographer.presentation.key.KeyGenerationViewModel
import com.example.cryptographer.presentation.main.AppScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun MainScreenContent(selectedScreen: AppScreen, drawerState: DrawerState, scope: CoroutineScope) {
    Scaffold(
        topBar = {
            MainScreenTopBar(
                screen = selectedScreen,
                onMenuClick = {
                    scope.launch { drawerState.open() }
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            ScreenContent(selectedScreen = selectedScreen)
        }
    }
}

@Composable
private fun ScreenContent(selectedScreen: AppScreen) {
    when (selectedScreen) {
        AppScreen.KeyGeneration -> {
            val keyViewModel: KeyGenerationViewModel = viewModel()
            KeyGenerationScreen(viewModel = keyViewModel)
        }
        AppScreen.AesEncryption -> {
            val aesViewModel: AesViewModel = viewModel()
            AesScreen(viewModel = aesViewModel)
        }
        AppScreen.ChaCha20Encryption -> {
            val chaCha20ViewModel: ChaCha20ViewModel = viewModel()
            ChaCha20Screen(viewModel = chaCha20ViewModel)
        }
        AppScreen.Encoding -> {
            val encodingViewModel: EncodingViewModel = viewModel()
            EncodingScreen(viewModel = encodingViewModel)
        }
    }
}
