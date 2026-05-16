package com.lottiepreview.plugin.file

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.wm.ToolWindowManager
import com.lottiepreview.plugin.toolwindow.LottiePreviewPanel
import java.nio.file.Path

/**
 * Watches the virtual file system for changes to the currently previewed Lottie file.
 *
 * - **Content change** → reloads the animation with updated data.
 * - **Deletion** → clears the preview back to the placeholder.
 */
class LottieVfsListener(private val project: Project) : BulkFileListener {

    override fun after(events: List<VFileEvent>) {
        val panel = findPanel() ?: return
        val currentFile = panel.browserManager.currentFile ?: return
        val currentPath = currentFile.toPath().toAbsolutePath().normalize()

        for (event in events) {
            val eventPath = eventPath(event) ?: continue
            if (eventPath != currentPath) continue

            when (event) {
                is VFileContentChangeEvent -> panel.browserManager.loadAnimation(currentFile)
                is VFileDeleteEvent -> panel.browserManager.clear()
            }
            return
        }
    }

    private fun eventPath(event: VFileEvent): Path? {
        return try {
            Path.of(event.path).toAbsolutePath().normalize()
        } catch (_: Exception) {
            null
        }
    }

    private fun findPanel(): LottiePreviewPanel? {
        if (project.isDisposed) return null
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow(TOOL_WINDOW_ID)
            ?: return null
        return toolWindow.contentManager
            .contents
            .asSequence()
            .mapNotNull { it.component as? LottiePreviewPanel }
            .firstOrNull()
    }

    private companion object {
        const val TOOL_WINDOW_ID = "Lottie Preview"
    }
}
