package com.lottiepreview.plugin.toolwindow

import com.intellij.openapi.Disposable
import com.intellij.openapi.util.Disposer
import com.intellij.ui.components.JBLabel
import com.intellij.ui.jcef.JBCefApp
import com.lottiepreview.plugin.actions.PlaybackActions
import com.lottiepreview.plugin.browser.JcefLottieBrowserManager
import com.lottiepreview.plugin.browser.LottieBrowserManager
import com.lottiepreview.plugin.browser.NoOpLottieBrowserManager
import java.awt.BorderLayout
import javax.swing.JPanel

class LottiePreviewPanel : JPanel(BorderLayout()), Disposable {
    val browserManager: LottieBrowserManager

    init {
        if (JBCefApp.isSupported()) {
            browserManager = JcefLottieBrowserManager(this)
            add(PlaybackActions.buildToolbar(browserManager), BorderLayout.NORTH)
            add(browserManager.component, BorderLayout.CENTER)
            Disposer.register(this, browserManager)
        } else {
            browserManager = NoOpLottieBrowserManager()
            add(JBLabel("JCEF is not supported in this IDE installation."), BorderLayout.CENTER)
        }
    }

    override fun dispose() = Unit
}
