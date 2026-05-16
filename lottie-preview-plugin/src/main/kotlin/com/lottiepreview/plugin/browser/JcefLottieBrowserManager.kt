package com.lottiepreview.plugin.browser

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
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
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.concurrent.ConcurrentLinkedQueue
import javax.swing.JComponent

class JcefLottieBrowserManager(
    private val project: Project,
    parentDisposable: Disposable
) : LottieBrowserManager {
    private val log = Logger.getInstance(JcefLottieBrowserManager::class.java)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val pendingScripts = ConcurrentLinkedQueue<String>()

    @Volatile
    private var playerReady = false

    private val browser = JBCefBrowser()

    override val component: JComponent
        get() = browser.component

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
        val htmlUrl = javaClass.classLoader
            .getResource("lottiepreview/player.html")
            ?.toExternalForm()

        if (htmlUrl == null) {
            log.error("player.html not found in plugin resources")
            return
        }

        browser.loadURL(htmlUrl)
    }

    override fun loadAnimation(file: File) {
        if (!file.isFile) return

        scope.launch {
            runCatching {
                val json = file.readText(StandardCharsets.UTF_8)
                Base64.getEncoder().encodeToString(json.toByteArray(StandardCharsets.UTF_8))
            }.onSuccess { base64 ->
                executeOrQueue("window.loadLottieBase64('$base64')")
            }.onFailure { error ->
                log.warn("Unable to load Lottie file: ${file.absolutePath}", error)
                executeOrQueue("window.showLottieError(${error.message.orEmpty().jsStringLiteral()})")
            }
        }
    }

    override fun play() = executeOrQueue("window.lottiePlay()")

    override fun pause() = executeOrQueue("window.lottiePause()")

    override fun stop() = executeOrQueue("window.lottieStop()")

    override fun setLoop(loop: Boolean) = executeOrQueue("window.lottieSetLoop($loop)")

    override fun setSpeed(speed: Float) = executeOrQueue("window.lottieSetSpeed($speed)")

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
