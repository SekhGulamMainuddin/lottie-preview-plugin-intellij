package com.lottiepreview.plugin.toolwindow

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.lottiepreview.plugin.actions.PlaybackActions
import com.lottiepreview.plugin.browser.NoOpLottieBrowserManager
import com.lottiepreview.plugin.service.LottiePreviewService
import java.awt.BorderLayout
import javax.swing.JPanel

class LottiePreviewPanel(project: Project) : JPanel(BorderLayout()), Disposable {
    private val previewService = LottiePreviewService.getInstance(project)

    init {
        // Rebuild whenever the service swaps managers, including upgrades triggered by loadAnimation().
        previewService.addBrowserManagerListener(this) { rebuildContent() }
        previewService.refreshBrowserManagerIfNeeded()
        rebuildContent()

        // JCEF on Rabbit may not report supported until after full IDE startup.
        ApplicationManager.getApplication().invokeLater({
            previewService.refreshBrowserManagerIfNeeded()
        }, project.disposed)
    }

    private fun rebuildContent() {
        removeAll()
        val browserManager = previewService.browserManager
        if (browserManager !is NoOpLottieBrowserManager) {
            add(PlaybackActions.buildToolbar(browserManager), BorderLayout.NORTH)
        }
        add(browserManager.component, BorderLayout.CENTER)
        revalidate()
        repaint()
    }

    override fun dispose() = Unit
}
