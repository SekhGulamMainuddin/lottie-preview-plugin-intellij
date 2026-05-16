package com.lottiepreview.plugin.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.wm.ToolWindowManager
import com.lottiepreview.plugin.file.LottieFileValidator
import com.lottiepreview.plugin.toolwindow.LottiePreviewPanel

class OpenLottieAction : AnAction() {
    override fun update(event: AnActionEvent) {
        val file = event.getData(CommonDataKeys.VIRTUAL_FILE)
        event.presentation.isEnabledAndVisible = file != null && LottieFileValidator.isLottieJsonFile(file)
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val file = event.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        if (!LottieFileValidator.isLottieJsonFile(file)) return

        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow(TOOL_WINDOW_ID) ?: return
        toolWindow.show {
            val panel = toolWindow.contentManager
                .contents
                .asSequence()
                .mapNotNull { it.component as? LottiePreviewPanel }
                .firstOrNull()

            panel?.browserManager?.loadAnimation(file.toNioPath().toFile())
        }
    }

    private companion object {
        const val TOOL_WINDOW_ID = "Lottie Preview"
    }
}
