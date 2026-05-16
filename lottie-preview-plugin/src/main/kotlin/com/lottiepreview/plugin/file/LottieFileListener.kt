package com.lottiepreview.plugin.file

import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.wm.ToolWindowManager
import com.lottiepreview.plugin.toolwindow.LottiePreviewPanel

class LottieFileListener : FileEditorManagerListener {
    override fun selectionChanged(event: FileEditorManagerEvent) {
        val file = event.newFile ?: return
        if (!LottieFileValidator.isLottieJsonFile(file)) return

        val project = event.manager.project
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
