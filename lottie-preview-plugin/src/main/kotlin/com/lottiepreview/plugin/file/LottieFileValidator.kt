package com.lottiepreview.plugin.file

import com.intellij.openapi.vfs.VirtualFile
import java.nio.charset.StandardCharsets

object LottieFileValidator {
    private const val MAX_PREVIEW_BYTES = 8192
    private val requiredKeys = listOf("\"v\"", "\"fr\"", "\"ip\"", "\"op\"", "\"layers\"")

    fun isLottieJsonFile(file: VirtualFile): Boolean {
        val ext = file.extension?.lowercase()
        if (ext == "lottie") return true
        if (ext != "json") return false

        return runCatching {
            val bytes = file.inputStream.use { input ->
                input.readNBytes(MAX_PREVIEW_BYTES)
            }
            val preview = String(bytes, StandardCharsets.UTF_8)
            requiredKeys.count { key -> preview.contains(key) } >= 4
        }.getOrDefault(false)
    }
}
