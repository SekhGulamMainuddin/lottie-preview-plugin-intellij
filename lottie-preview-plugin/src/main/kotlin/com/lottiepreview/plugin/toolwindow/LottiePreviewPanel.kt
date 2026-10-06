package com.lottiepreview.plugin.toolwindow

import com.intellij.openapi.project.Project
import com.lottiepreview.plugin.actions.PlaybackActions
import com.lottiepreview.plugin.browser.JcefAvailability
import com.lottiepreview.plugin.service.LottiePreviewService
import java.awt.BorderLayout
import javax.swing.JPanel

class LottiePreviewPanel(project: Project) : JPanel(BorderLayout()) {
    init {
        val browserManager = LottiePreviewService.getInstance(project).browserManager

        if (JcefAvailability.isAvailable()) {
            add(PlaybackActions.buildToolbar(browserManager), BorderLayout.NORTH)
        }
        add(browserManager.component, BorderLayout.CENTER)
    }
}
