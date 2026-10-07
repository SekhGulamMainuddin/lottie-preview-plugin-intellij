package com.lottiepreview.plugin.service

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.VirtualFile
import com.lottiepreview.plugin.browser.JcefAvailability
import com.lottiepreview.plugin.browser.JcefLottieBrowserManager
import com.lottiepreview.plugin.browser.LottieBrowserManager
import com.lottiepreview.plugin.browser.NoOpLottieBrowserManager
import com.lottiepreview.plugin.file.LottieFileValidator
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Project-level service that owns the Lottie browser manager and acts as
 * the single source of truth for preview state.
 *
 * Actions and listeners should call this service instead of digging
 * into tool window content to find the preview panel.
 */
@Service(Service.Level.PROJECT)
class LottiePreviewService(private val project: Project) : Disposable {

    @Volatile
    var browserManager: LottieBrowserManager = createBrowserManager()
        private set

    private val managerLock = Any()
    private val managerListeners = CopyOnWriteArrayList<() -> Unit>()

    init {
        Disposer.register(this, browserManager)
    }

    /**
     * Registers [listener] to be called on the EDT whenever [browserManager] is replaced.
     * The listener is removed when [parentDisposable] is disposed.
     */
    fun addBrowserManagerListener(parentDisposable: Disposable, listener: () -> Unit) {
        managerListeners.add(listener)
        Disposer.register(parentDisposable) { managerListeners.remove(listener) }
    }

    /**
     * On Android Studio Rabbit, JCEF can become usable shortly after project services
     * start (or after JBCefApp.isSupported first returns false during IDE bootstrap).
     * Re-probe when opening the tool window so we do not stick on [NoOpLottieBrowserManager].
     *
     * @return true if the manager was upgraded from the no-op fallback to JCEF.
     */
    fun refreshBrowserManagerIfNeeded(): Boolean {
        if (browserManager !is NoOpLottieBrowserManager) return false

        synchronized(managerLock) {
            if (browserManager !is NoOpLottieBrowserManager) return false
            if (!JcefAvailability.isAvailable()) return false

            val previous = browserManager
            val next = JcefLottieBrowserManager(this)
            browserManager = next
            Disposer.register(this, next)
            Disposer.dispose(previous)
        }

        notifyBrowserManagerChanged()
        return true
    }

    private fun notifyBrowserManagerChanged() {
        ApplicationManager.getApplication().invokeLater({
            managerListeners.forEach { it() }
        }, project.disposed)
    }

    private fun createBrowserManager(): LottieBrowserManager {
        return if (JcefAvailability.isAvailable()) {
            JcefLottieBrowserManager(this)
        } else {
            NoOpLottieBrowserManager()
        }
    }

    /**
     * Validates the file as a Lottie JSON and loads it into the preview.
     */
    fun loadAnimation(file: VirtualFile) {
        refreshBrowserManagerIfNeeded()
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
