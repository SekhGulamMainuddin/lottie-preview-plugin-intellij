package com.lottiepreview.plugin.actions

import com.lottiepreview.plugin.browser.LottieBrowserManager
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JToggleButton
import javax.swing.JToolBar

import java.awt.FlowLayout
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.JComponent
import javax.swing.Box

object PlaybackActions {
    fun buildToolbar(manager: LottieBrowserManager): JComponent {
        val panel = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
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

            add(Box.createHorizontalStrut(10))

            add(JToggleButton("Loop").apply {
                toolTipText = "Loop"
                isSelected = true
                addActionListener { manager.setLoop(isSelected) }
            })

            add(Box.createHorizontalStrut(10))
            add(JLabel(" Speed: "))

            add(JComboBox(arrayOf("0.5x", "1x", "1.5x", "2x")).apply {
                selectedItem = "1x"
                addActionListener {
                    val speed = (selectedItem as String).removeSuffix("x").toFloat()
                    manager.setSpeed(speed)
                }
            })

            add(Box.createHorizontalStrut(10))
            add(JLabel(" BG Color: "))
            add(com.intellij.ui.components.JBTextField(7).apply {
                toolTipText = "Enter hex color (e.g., #FFFFFF)"
                val applyColor = { manager.setBackgroundColor(text) }
                addActionListener { applyColor() }
                document.addDocumentListener(object : javax.swing.event.DocumentListener {
                    override fun insertUpdate(e: javax.swing.event.DocumentEvent?) = applyColor()
                    override fun removeUpdate(e: javax.swing.event.DocumentEvent?) = applyColor()
                    override fun changedUpdate(e: javax.swing.event.DocumentEvent?) = applyColor()
                })
            })

            add(JCheckBox("Boundary").apply {
                toolTipText = "Show animation boundary"
                addActionListener { manager.setShowBoundary(isSelected) }
            })
        }
        
        return panel
    }
}
