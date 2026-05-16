package com.lottiepreview.plugin.actions

import com.lottiepreview.plugin.browser.LottieBrowserManager
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JToggleButton
import javax.swing.JToolBar

object PlaybackActions {
    fun buildToolbar(manager: LottieBrowserManager): JToolBar {
        return JToolBar().apply {
            isFloatable = false

            add(JButton("Play").apply {
                toolTipText = "Play"
                addActionListener { manager.play() }
            })

            add(JButton("Pause").apply {
                toolTipText = "Pause"
                addActionListener { manager.pause() }
            })

            add(JButton("Stop").apply {
                toolTipText = "Stop"
                addActionListener { manager.stop() }
            })

            addSeparator()

            add(JToggleButton("Loop").apply {
                toolTipText = "Loop"
                isSelected = true
                addActionListener { manager.setLoop(isSelected) }
            })

            addSeparator()
            add(JLabel(" Speed: "))

            add(JComboBox(arrayOf("0.5x", "1x", "1.5x", "2x")).apply {
                selectedItem = "1x"
                addActionListener {
                    val speed = (selectedItem as String).removeSuffix("x").toFloat()
                    manager.setSpeed(speed)
                }
            })
        }
    }
}
