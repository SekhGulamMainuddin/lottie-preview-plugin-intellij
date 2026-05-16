package com.lottiepreview.plugin.browser

import com.intellij.openapi.Disposable
import java.io.File
import javax.swing.JComponent

interface LottieBrowserManager : Disposable {
    val component: JComponent
    val currentFile: File?

    fun loadAnimation(file: File)
    fun clear()
    fun play()
    fun pause()
    fun stop()
    fun setLoop(loop: Boolean)
    fun setSpeed(speed: Float)
    fun setBackgroundColor(hexColor: String)
    fun setShowBoundary(show: Boolean)
}
