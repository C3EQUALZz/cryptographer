package com.example.cryptographer.presentation.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import java.net.URLDecoder

private const val DEFAULT_ENCODING = "UTF-8"

fun persistReadPermission(context: Context, uri: Uri) {
    persistUriPermission(context, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
}

fun persistWritePermission(context: Context, uri: Uri) {
    persistUriPermission(context, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
}

fun defaultOutputFileName(inputPath: String, suffix: String, fallback: String): String {
    if (inputPath.isBlank()) {
        return fallback
    }

    val uri = inputPath.toUri()
    val lastSegment = uri.lastPathSegment ?: inputPath.substringAfterLast('/').substringAfterLast('\\')
    val decodedSegment = runCatching { URLDecoder.decode(lastSegment, DEFAULT_ENCODING) }
        .getOrDefault(lastSegment)
    val trimmedSegment = decodedSegment.substringAfterLast(':')
    val baseName = trimmedSegment.substringBeforeLast('.', trimmedSegment)

    return if (baseName.isBlank()) {
        fallback
    } else {
        "$baseName$suffix"
    }
}

private fun persistUriPermission(context: Context, uri: Uri, flags: Int) {
    runCatching { context.contentResolver.takePersistableUriPermission(uri, flags) }
}
