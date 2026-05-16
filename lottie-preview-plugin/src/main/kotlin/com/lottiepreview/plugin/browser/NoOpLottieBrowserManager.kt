package com.lottiepreview.plugin.browser

import com.intellij.ui.components.JBLabel
import java.io.File
import javax.swing.JComponent

class NoOpLottieBrowserManager : LottieBrowserManager {
    override val component: JComponent = JBLabel("JCEF is not supported in this IDE installation.")
    override val currentFile: File? = null

    override fun loadAnimation(file: File) = Unit
    override fun clear() = Unit
    override fun play() = Unit
    override fun pause() = Unit
    override fun stop() = Unit
    override fun setLoop(loop: Boolean) = Unit
    override fun setSpeed(speed: Float) = Unit
    override fun setBackgroundColor(hexColor: String) = Unit
    override fun setShowBoundary(show: Boolean) = Unit
    override fun dispose() = Unit
}
