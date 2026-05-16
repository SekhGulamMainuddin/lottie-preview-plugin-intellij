package com.lottiepreview.plugin.service

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.jcef.JBCefApp
import com.lottiepreview.plugin.browser.JcefLottieBrowserManager
import com.lottiepreview.plugin.browser.LottieBrowserManager
import com.lottiepreview.plugin.browser.NoOpLottieBrowserManager
import com.lottiepreview.plugin.file.LottieFileValidator

/**
 * Project-level service that owns the Lottie browser manager and acts as
 * the single source of truth for preview state.
 *
 * Actions and listeners should call this service instead of digging
 * into tool window content to find the preview panel.
 */
@Service(Service.Level.PROJECT)
class LottiePreviewService(private val project: Project) : Disposable {

    val browserManager: LottieBrowserManager

    init {
        browserManager = if (JBCefApp.isSupported()) {
            JcefLottieBrowserManager(this)
        } else {
            NoOpLottieBrowserManager()
        }
        Disposer.register(this, browserManager)
    }

    /**
     * Validates the file as a Lottie JSON and loads it into the preview.
     */
    fun loadAnimation(file: VirtualFile) {
        if (!LottieFileValidator.isLottieJsonFile(file)) return
        browserManager.loadAnimation(file.toNioPath().toFile())
    }

    override fun dispose() = Unit

    companion object {
        @JvmStatic
        fun getInstance(project: Project): LottiePreviewService =
            project.getService(LottiePreviewService::class.java)
    }
}
