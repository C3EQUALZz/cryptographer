package com.example.cryptographer.domain.text.usecase

import com.example.cryptographer.domain.text.entity.Text
import com.example.cryptographer.domain.text.entity.TextEncoding
import java.util.Base64

/**
 * Use Case for converting text between different encodings.
 */
class ConvertTextEncodingUseCase {
    operator fun invoke(
        text: Text,
        targetEncoding: TextEncoding
    ): Result<Text> {
        return try {
            if (text.encoding == targetEncoding) {
                return Result.success(text)
            }

            val convertedContent = when {
                text.encoding == TextEncoding.UTF8 && targetEncoding == TextEncoding.BASE64 -> {
                    Base64.getEncoder().encodeToString(text.content.toByteArray(Charsets.UTF_8))
                }
                text.encoding == TextEncoding.BASE64 && targetEncoding == TextEncoding.UTF8 -> {
                    String(Base64.getDecoder().decode(text.content), Charsets.UTF_8)
                }
                text.encoding == TextEncoding.UTF8 && targetEncoding == TextEncoding.ASCII -> {
                    convertToAscii(text.content)
                }
                text.encoding == TextEncoding.ASCII && targetEncoding == TextEncoding.UTF8 -> {
                    text.content // ASCII is a subset of UTF8
                }
                else -> {
                    return Result.failure(
                        IllegalArgumentException(
                            "Conversion from ${text.encoding} to $targetEncoding is not supported"
                        )
                    )
                }
            }

            Result.success(
                Text(
                    content = convertedContent,
                    encoding = targetEncoding
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun convertToAscii(content: String): String {
        return content.map { char ->
            if (char.code < 128) char else '?'
        }.joinToString("")
    }
}

