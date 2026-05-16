package com.lottiepreview.plugin.browser

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.Disposer
import com.intellij.ui.jcef.JBCefBrowser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.cef.browser.CefBrowser
import org.cef.handler.CefLoadHandlerAdapter
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.zip.ZipInputStream
import javax.swing.JComponent

class JcefLottieBrowserManager(
    parentDisposable: Disposable
) : LottieBrowserManager {
    private val log = Logger.getInstance(JcefLottieBrowserManager::class.java)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val pendingScripts = ConcurrentLinkedQueue<String>()

    @Volatile
    private var playerReady = false

    @Volatile
    private var _currentFile: File? = null

    private val browser = JBCefBrowser()

    override val component: JComponent
        get() = browser.component

    override val currentFile: File?
        get() = _currentFile

    init {
        Disposer.register(parentDisposable, browser)
        browser.jbCefClient.addLoadHandler(object : CefLoadHandlerAdapter() {
            override fun onLoadEnd(cefBrowser: CefBrowser, frame: org.cef.browser.CefFrame, httpStatusCode: Int) {
                if (frame.isMain) {
                    playerReady = true
                    flushPendingScripts()
                }
            }
        }, browser.cefBrowser)
        loadPlayerHtml()
    }

    private fun loadPlayerHtml() {
        val htmlUrl = runCatching {
            extractPlayerResources().toUri().toASCIIString()
        }.onFailure { error ->
            log.error("Unable to prepare Lottie player resources", error)
        }.getOrNull()

        if (htmlUrl == null) {
            return
        }

        playerReady = false
        browser.loadURL(htmlUrl)
    }

    private fun extractPlayerResources(): Path {
        val playerDirectory = Path.of(PathManager.getTempPath(), "lottie-preview-plugin", "player")
        Files.createDirectories(playerDirectory)

        copyResource("lottiepreview/player.html", playerDirectory.resolve("player.html"))
        copyResource("lottiepreview/lottie.min.js", playerDirectory.resolve("lottie.min.js"))
        copyResource("lottiepreview/dotlottie.js", playerDirectory.resolve("dotlottie.js"))
        copyResource("lottiepreview/dotlottie-player.wasm", playerDirectory.resolve("dotlottie-player.wasm"))

        return playerDirectory.resolve("player.html")
    }

    private fun copyResource(resourcePath: String, target: Path) {
        val input = javaClass.classLoader.getResourceAsStream(resourcePath)
            ?: error("$resourcePath not found in plugin resources")

        input.use {
            Files.copy(it, target, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    override fun loadAnimation(file: File) {
        if (!file.isFile) return
        _currentFile = file

        scope.launch {
            runCatching {
                val bytes = file.readBytes()
                Base64.getEncoder().encodeToString(bytes)
            }.onSuccess { base64 ->
                executeOrQueue("window.loadLottieBase64('$base64', '${file.extension.lowercase()}')")
                executeOrQueue("window.setFilenameOverlay(${file.name.jsStringLiteral()})")
            }.onFailure { error ->
                log.warn("Unable to load Lottie file: ${file.absolutePath}", error)
                executeOrQueue("window.showLottieError(${error.message.orEmpty().jsStringLiteral()})")
            }
        }
    }

    override fun clear() {
        _currentFile = null
        executeOrQueue("window.clearAnimation()")
        executeOrQueue("window.setFilenameOverlay('')")
    }

    override fun play() = executeOrQueue("window.lottiePlay()")

    override fun pause() = executeOrQueue("window.lottiePause()")

    override fun stop() = executeOrQueue("window.lottieStop()")

    override fun setLoop(loop: Boolean) = executeOrQueue("window.lottieSetLoop($loop)")

    override fun setSpeed(speed: Float) = executeOrQueue("window.lottieSetSpeed($speed)")

    override fun setBackgroundColor(hexColor: String) = executeOrQueue("window.setBackgroundColor(${hexColor.jsStringLiteral()})")

    override fun setShowBoundary(show: Boolean) = executeOrQueue("window.lottieSetBoundary($show)")

    private fun executeOrQueue(script: String) {
        if (!playerReady) {
            pendingScripts.add(script)
            return
        }

        browser.cefBrowser.executeJavaScript(script, browser.cefBrowser.url ?: "", 0)
    }

    private fun flushPendingScripts() {
        while (true) {
            val script = pendingScripts.poll() ?: return
            executeOrQueue(script)
        }
    }

    override fun dispose() {
        scope.cancel()
    }

    private fun String.jsStringLiteral(): String {
        return buildString(length + 2) {
            append('\'')
            this@jsStringLiteral.forEach { char ->
                when (char) {
                    '\\' -> append("\\\\")
                    '\'' -> append("\\'")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> append(char)
                }
            }
            append('\'')
        }
    }
}
