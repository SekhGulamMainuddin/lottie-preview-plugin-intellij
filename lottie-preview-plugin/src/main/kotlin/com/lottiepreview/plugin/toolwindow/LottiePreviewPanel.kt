package com.lottiepreview.plugin.toolwindow

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.ui.jcef.JBCefApp
import com.lottiepreview.plugin.actions.PlaybackActions
import com.lottiepreview.plugin.service.LottiePreviewService
import java.awt.BorderLayout
import javax.swing.JPanel

class LottiePreviewPanel(project: Project) : JPanel(BorderLayout()) {
    init {
        val browserManager = LottiePreviewService.getInstance(project).browserManager

        if (JBCefApp.isSupported()) {
            add(PlaybackActions.buildToolbar(browserManager), BorderLayout.NORTH)
            add(browserManager.component, BorderLayout.CENTER)
        } else {
            add(JBLabel("JCEF is not supported in this IDE installation."), BorderLayout.CENTER)
        }
    }
}
