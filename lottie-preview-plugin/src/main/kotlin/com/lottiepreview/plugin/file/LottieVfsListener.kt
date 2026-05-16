package com.lottiepreview.plugin.file

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.lottiepreview.plugin.service.LottiePreviewService
import java.nio.file.Path

/**
 * Watches the virtual file system for changes to the currently previewed Lottie file.
 *
 * - **Content change** → reloads the animation with updated data.
 * - **Deletion** → clears the preview back to the placeholder.
 */
class LottieVfsListener(private val project: Project) : BulkFileListener {

    override fun after(events: List<VFileEvent>) {
        if (project.isDisposed) return
        val service = LottiePreviewService.getInstance(project)
        val currentFile = service.browserManager.currentFile ?: return
        val currentPath = currentFile.toPath().toAbsolutePath().normalize()

        for (event in events) {
            val eventPath = eventPath(event) ?: continue
            if (eventPath != currentPath) continue

            when (event) {
                is VFileContentChangeEvent -> service.browserManager.loadAnimation(currentFile)
                is VFileDeleteEvent -> service.browserManager.clear()
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
}
