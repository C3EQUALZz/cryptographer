package com.example.cryptographer.presentation.main

import com.example.cryptographer.application.commands.language.update.SaveLanguageCommand
import com.example.cryptographer.application.commands.language.update.SaveLanguageCommandHandler
import com.example.cryptographer.application.commands.theme.update.SaveThemeCommand
import com.example.cryptographer.application.commands.theme.update.SaveThemeCommandHandler
import com.example.cryptographer.application.common.views.LanguageView
import com.example.cryptographer.application.common.views.ThemeView
import com.example.cryptographer.application.queries.language.read.LoadLanguageQuery
import com.example.cryptographer.application.queries.language.read.LoadLanguageQueryHandler
import com.example.cryptographer.application.queries.theme.read.LoadThemeQuery
import com.example.cryptographer.application.queries.theme.read.LoadThemeQueryHandler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for MainPresenter.
 */
class MainPresenterTest {

    private lateinit var saveThemeHandler: SaveThemeCommandHandler
    private lateinit var loadThemeHandler: LoadThemeQueryHandler
    private lateinit var saveLanguageHandler: SaveLanguageCommandHandler
    private lateinit var loadLanguageHandler: LoadLanguageQueryHandler
    private lateinit var presenter: MainPresenter

    @Before
    fun setUp() {
        saveThemeHandler = mockk()
        loadThemeHandler = mockk()
        saveLanguageHandler = mockk()
        loadLanguageHandler = mockk()
        presenter = MainPresenter(
            saveThemeHandler = saveThemeHandler,
            loadThemeHandler = loadThemeHandler,
            saveLanguageHandler = saveLanguageHandler,
            loadLanguageHandler = loadLanguageHandler,
        )
    }

    @Test
    fun `loadThemeMode should return success when theme is loaded`() = runTest {
        // Given
        val themeMode = "dark"
        val themeView = ThemeView(themeMode = themeMode)

        coEvery { loadThemeHandler(LoadThemeQuery) } returns Result.success(themeView)

        // When
        val result = presenter.loadThemeMode()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(themeMode, result.getOrThrow())

        coVerify(exactly = 1) { loadThemeHandler(LoadThemeQuery) }
    }

    @Test
    fun `loadThemeMode should return failure when loading fails`() = runTest {
        // Given
        val error = Exception("Failed to load theme mode")

        coEvery { loadThemeHandler(LoadThemeQuery) } returns Result.failure(error)

        // When
        val result = presenter.loadThemeMode()

        // Then
        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())

        coVerify(exactly = 1) { loadThemeHandler(LoadThemeQuery) }
    }

    @Test
    fun `loadThemeMode should handle all theme modes`() = runTest {
        // Given
        val themeModes = listOf("system", "light", "dark")

        themeModes.forEach { themeMode ->
            val themeView = ThemeView(themeMode = themeMode)
            coEvery { loadThemeHandler(LoadThemeQuery) } returns Result.success(themeView)

            // When
            val result = presenter.loadThemeMode()

            // Then
            assertTrue(result.isSuccess)
            assertEquals(themeMode, result.getOrThrow())
        }
    }

    @Test
    fun `saveThemeMode should return success when theme is saved`() = runTest {
        // Given
        val themeMode = "light"

        coEvery { saveThemeHandler(SaveThemeCommand(themeMode)) } returns Result.success(Unit)

        // When
        val result = presenter.saveThemeMode(themeMode)

        // Then
        assertTrue(result.isSuccess)

        coVerify(exactly = 1) { saveThemeHandler(SaveThemeCommand(themeMode)) }
    }

    @Test
    fun `saveThemeMode should return failure when saving fails`() = runTest {
        // Given
        val themeMode = "dark"
        val error = Exception("Failed to save theme mode")

        coEvery { saveThemeHandler(SaveThemeCommand(themeMode)) } returns Result.failure(error)

        // When
        val result = presenter.saveThemeMode(themeMode)

        // Then
        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())

        coVerify(exactly = 1) { saveThemeHandler(SaveThemeCommand(themeMode)) }
    }

    @Test
    fun `loadLanguage should return success when language is loaded`() = runTest {
        // Given
        val languageCode = "en"
        val languageView = LanguageView(languageCode = languageCode)

        coEvery { loadLanguageHandler(LoadLanguageQuery) } returns Result.success(languageView)

        // When
        val result = presenter.loadLanguage()

        // Then
        assertTrue(result.isSuccess)
        assertEquals(languageCode, result.getOrThrow())

        coVerify(exactly = 1) { loadLanguageHandler(LoadLanguageQuery) }
    }

    @Test
    fun `loadLanguage should return failure when loading fails`() = runTest {
        // Given
        val error = Exception("Failed to load language")

        coEvery { loadLanguageHandler(LoadLanguageQuery) } returns Result.failure(error)

        // When
        val result = presenter.loadLanguage()

        // Then
        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())

        coVerify(exactly = 1) { loadLanguageHandler(LoadLanguageQuery) }
    }

    @Test
    fun `loadLanguage should handle all supported languages`() = runTest {
        // Given
        val languageCodes = listOf("en", "ru")

        languageCodes.forEach { languageCode ->
            val languageView = LanguageView(languageCode = languageCode)
            coEvery { loadLanguageHandler(LoadLanguageQuery) } returns Result.success(languageView)

            // When
            val result = presenter.loadLanguage()

            // Then
            assertTrue(result.isSuccess)
            assertEquals(languageCode, result.getOrThrow())
        }
    }

    @Test
    fun `saveLanguage should return success when language is saved`() = runTest {
        // Given
        val languageCode = "ru"

        coEvery { saveLanguageHandler(SaveLanguageCommand(languageCode)) } returns Result.success(Unit)

        // When
        val result = presenter.saveLanguage(languageCode)

        // Then
        assertTrue(result.isSuccess)

        coVerify(exactly = 1) { saveLanguageHandler(SaveLanguageCommand(languageCode)) }
    }

    @Test
    fun `saveLanguage should return failure when saving fails`() = runTest {
        // Given
        val languageCode = "en"
        val error = Exception("Failed to save language")

        coEvery { saveLanguageHandler(SaveLanguageCommand(languageCode)) } returns Result.failure(error)

        // When
        val result = presenter.saveLanguage(languageCode)

        // Then
        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())

        coVerify(exactly = 1) { saveLanguageHandler(SaveLanguageCommand(languageCode)) }
    }

    @Test
    fun `loadThemeMode should handle exception during loading`() = runTest {
        // Given
        val exception = RuntimeException("Unexpected error")

        coEvery { loadThemeHandler(LoadThemeQuery) } throws exception

        // When
        val result = presenter.loadThemeMode()

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())

        coVerify(exactly = 1) { loadThemeHandler(LoadThemeQuery) }
    }

    @Test
    fun `saveThemeMode should handle exception during saving`() = runTest {
        // Given
        val themeMode = "system"
        val exception = RuntimeException("Unexpected error")

        coEvery { saveThemeHandler(SaveThemeCommand(themeMode)) } throws exception

        // When
        val result = presenter.saveThemeMode(themeMode)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())

        coVerify(exactly = 1) { saveThemeHandler(SaveThemeCommand(themeMode)) }
    }

    @Test
    fun `loadLanguage should handle exception during loading`() = runTest {
        // Given
        val exception = RuntimeException("Unexpected error")

        coEvery { loadLanguageHandler(LoadLanguageQuery) } throws exception

        // When
        val result = presenter.loadLanguage()

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())

        coVerify(exactly = 1) { loadLanguageHandler(LoadLanguageQuery) }
    }

    @Test
    fun `saveLanguage should handle exception during saving`() = runTest {
        // Given
        val languageCode = "en"
        val exception = RuntimeException("Unexpected error")

        coEvery { saveLanguageHandler(SaveLanguageCommand(languageCode)) } throws exception

        // When
        val result = presenter.saveLanguage(languageCode)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())

        coVerify(exactly = 1) { saveLanguageHandler(SaveLanguageCommand(languageCode)) }
    }
}
