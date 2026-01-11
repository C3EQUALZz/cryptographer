package com.example.cryptographer.presentation.main

import com.example.cryptographer.application.commands.language.update.SaveLanguageCommand
import com.example.cryptographer.application.commands.language.update.SaveLanguageCommandHandler
import com.example.cryptographer.application.commands.theme.update.SaveThemeCommand
import com.example.cryptographer.application.commands.theme.update.SaveThemeCommandHandler
import com.example.cryptographer.application.queries.language.read.LoadLanguageQuery
import com.example.cryptographer.application.queries.language.read.LoadLanguageQueryHandler
import com.example.cryptographer.application.queries.theme.read.LoadThemeQuery
import com.example.cryptographer.application.queries.theme.read.LoadThemeQueryHandler
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * Presenter for main screen (navigation and settings).
 * Coordinates command/query handlers for settings (theme, language).
 *
 * This layer separates business logic from ViewModel, making it easier to test.
 * Following CQRS pattern - uses CommandHandlers and QueryHandlers from Application layer.
 */
class MainPresenter(
    private val saveThemeHandler: SaveThemeCommandHandler,
    private val loadThemeHandler: LoadThemeQueryHandler,
    private val saveLanguageHandler: SaveLanguageCommandHandler,
    private val loadLanguageHandler: LoadLanguageQueryHandler,
) {
    private val logger = KotlinLogging.logger {}

    /**
     * Loads the saved theme mode.
     *
     * @return Result with theme mode ("system", "light", or "dark") or error
     */
    suspend fun loadThemeMode(): Result<String> {
        return try {
            logger.debug { "Presenter: Loading theme mode" }
            val query = LoadThemeQuery
            val themeViewResult = loadThemeHandler(query)

            if (themeViewResult.isFailure) {
                val error = themeViewResult.exceptionOrNull() ?: Exception("Failed to load theme mode")
                logger.error(error) { "Presenter: Failed to load theme mode: ${error.message}" }
                return Result.failure(error)
            }

            val themeView = themeViewResult.getOrThrow()
            logger.debug { "Presenter: Theme mode loaded: ${themeView.themeMode}" }
            Result.success(themeView.themeMode)
        } catch (e: Exception) {
            logger.error(e) { "Presenter: Error loading theme mode: ${e.message}" }
            Result.failure(e)
        }
    }

    /**
     * Saves the theme mode.
     *
     * @param themeMode Theme mode to save ("system", "light", or "dark")
     * @return Result indicating success or failure
     */
    suspend fun saveThemeMode(themeMode: String): Result<Unit> {
        return try {
            logger.debug { "Presenter: Saving theme mode: $themeMode" }
            val command = SaveThemeCommand(themeMode)
            val result = saveThemeHandler(command)

            if (result.isSuccess) {
                logger.info { "Presenter: Theme mode saved successfully: $themeMode" }
            } else {
                logger.warn { "Presenter: Failed to save theme mode: $themeMode" }
            }

            result
        } catch (e: Exception) {
            logger.error(e) { "Presenter: Error saving theme mode: ${e.message}" }
            Result.failure(e)
        }
    }

    /**
     * Loads the saved language preference.
     *
     * @return Result with language code (e.g., "en", "ru") or error
     */
    suspend fun loadLanguage(): Result<String> {
        return try {
            logger.debug { "Presenter: Loading language" }
            val query = LoadLanguageQuery
            val languageViewResult = loadLanguageHandler(query)

            if (languageViewResult.isFailure) {
                val error = languageViewResult.exceptionOrNull() ?: Exception("Failed to load language")
                logger.error(error) { "Presenter: Failed to load language: ${error.message}" }
                return Result.failure(error)
            }

            val languageView = languageViewResult.getOrThrow()
            logger.debug { "Presenter: Language loaded: ${languageView.languageCode}" }
            Result.success(languageView.languageCode)
        } catch (e: Exception) {
            logger.error(e) { "Presenter: Error loading language: ${e.message}" }
            Result.failure(e)
        }
    }

    /**
     * Saves the language preference.
     *
     * @param languageCode Language code to save (e.g., "en", "ru")
     * @return Result indicating success or failure
     */
    suspend fun saveLanguage(languageCode: String): Result<Unit> {
        return try {
            logger.debug { "Presenter: Saving language: $languageCode" }
            val command = SaveLanguageCommand(languageCode)
            val result = saveLanguageHandler(command)

            if (result.isSuccess) {
                logger.info { "Presenter: Language saved successfully: $languageCode" }
            } else {
                logger.warn { "Presenter: Failed to save language: $languageCode" }
            }

            result
        } catch (e: Exception) {
            logger.error(e) { "Presenter: Error saving language: ${e.message}" }
            Result.failure(e)
        }
    }
}
