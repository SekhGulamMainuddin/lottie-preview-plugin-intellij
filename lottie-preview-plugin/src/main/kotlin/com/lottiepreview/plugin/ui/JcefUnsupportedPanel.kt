package com.lottiepreview.plugin.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.datatransfer.StringSelection
import javax.swing.*

class JcefUnsupportedPanel : JBScrollPane() {
    init {
        // Outer panel that holds the content
        val contentPanel = JBPanel<JBPanel<*>>().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = JBUI.Borders.empty(16, 20)
        }

        // Header panel: Icon + Title
        val headerPanel = JPanel(BorderLayout()).apply {
            isOpaque = false
            alignmentX = JComponent.LEFT_ALIGNMENT
            border = JBUI.Borders.emptyBottom(12)
        }

        val iconLabel = JBLabel(AllIcons.General.Warning).apply {
            border = JBUI.Borders.emptyRight(10)
        }
        val titleLabel = JBLabel("JCEF Support Required").apply {
            font = font.deriveFont(Font.BOLD, 15f)
        }
        headerPanel.add(iconLabel, BorderLayout.WEST)
        headerPanel.add(titleLabel, BorderLayout.CENTER)
        contentPanel.add(headerPanel)

        // Description text pane (HTML with custom styling)
        val isDark = !com.intellij.ui.JBColor.isBright()
        val textColor = if (isDark) "#BBBBBB" else "#4E4E4E"
        val codeBgColor = if (isDark) "#3C3F41" else "#E5E5E5"
        val codeTextColor = if (isDark) "#6BA2E0" else "#2E5CA2"
        val fontName = UIUtil.getLabelFont().fontName
        val fontSize = UIUtil.getLabelFont().size

        val descPane = JTextPane().apply {
            contentType = "text/html"
            isEditable = false
            isOpaque = false
            editorKit = javax.swing.text.html.HTMLEditorKit()
            alignmentX = JComponent.LEFT_ALIGNMENT
            border = JBUI.Borders.emptyBottom(16)
            text = """
                <html>
                <body style="font-family: '$fontName', sans-serif; font-size: ${fontSize}px; color: $textColor; line-height: 1.4;">
                    To preview Lottie and dotLottie animations, Android Studio requires a <b>JCEF-enabled</b> boot runtime. 
                    Without JCEF, web-based rendering is unavailable in this IDE installation.
                </body>
                </html>
            """.trimIndent()
        }
        contentPanel.add(descPane)

        // Step 1 Section
        val step1Panel = createStepCard(
            "1. Switch to a JCEF Runtime (Recommended)",
            """
                <html>
                <body style="font-family: '$fontName', sans-serif; font-size: ${fontSize}px; color: $textColor; line-height: 1.4;">
                    This is the most reliable fix for Android Studio:
                    <ol style="margin-top: 4px; padding-left: 20px;">
                        <li>Press <code style="background-color: $codeBgColor; color: $codeTextColor;">Cmd+Shift+A</code> (macOS) or <code style="background-color: $codeBgColor; color: $codeTextColor;">Ctrl+Shift+A</code> (Windows/Linux) to open the Action Search.</li>
                        <li>Type <b>"Choose Boot Java Runtime for the IDE"</b> and press Enter.</li>
                        <li>In the dropdown, select a runtime version that says <b>"with JCEF"</b>.</li>
                        <li>Click <b>OK / Install</b> and <b>Restart</b> Android Studio.</li>
                    </ol>
                </body>
                </html>
            """.trimIndent()
        )
        contentPanel.add(step1Panel)
        contentPanel.add(Box.createRigidArea(Dimension(0, 12)))

        // Step 2 Section
        val step2Panel = createStepCard(
            "2. Force-Enable via Registry",
            """
                <html>
                <body style="font-family: '$fontName', sans-serif; font-size: ${fontSize}px; color: $textColor; line-height: 1.4;">
                    If the runtime is switched but preview still fails:
                    <ol style="margin-top: 4px; padding-left: 20px;">
                        <li>Open Action Search and search for <b>"Registry..."</b>.</li>
                        <li>Locate the key <code style="background-color: $codeBgColor; color: $codeTextColor;">ide.browser.jcef.enabled</code>.</li>
                        <li>Ensure it is <b>checked</b>.</li>
                        <li>Restart Android Studio.</li>
                    </ol>
                </body>
                </html>
            """.trimIndent()
        )
        contentPanel.add(step2Panel)
        contentPanel.add(Box.createRigidArea(Dimension(0, 12)))

        // Step 3 Section
        val copyButton = JButton("Copy Property Line").apply {
            toolTipText = "Copy 'ide.browser.jcef.enabled=true' to clipboard"
            addActionListener {
                CopyPasteManager.getInstance().setContents(StringSelection("ide.browser.jcef.enabled=true"))
                text = "Copied!"
                isEnabled = false
                Timer(1500) {
                    text = "Copy Property Line"
                    isEnabled = true
                }.apply {
                    isRepeats = false
                    start()
                }
            }
        }

        val step3Panel = createStepCard(
            "3. Enable via idea.properties (Alternative)",
            """
                <html>
                <body style="font-family: '$fontName', sans-serif; font-size: ${fontSize}px; color: $textColor; line-height: 1.4;">
                    Add the configuration line directly to custom properties:
                    <ol style="margin-top: 4px; padding-left: 20px; margin-bottom: 8px;">
                        <li>Go to top menu: <b>Help > Edit Custom Properties...</b></li>
                        <li>Add the following line to the file:</li>
                    </ol>
                    <code style="background-color: $codeBgColor; color: $textColor; padding: 4px 6px; display: block; border-radius: 4px;">ide.browser.jcef.enabled=true</code>
                </body>
                </html>
            """.trimIndent(),
            copyButton
        )
        contentPanel.add(step3Panel)

        // Set the scroll pane viewport
        setViewportView(contentPanel)
        border = BorderFactory.createEmptyBorder()
        horizontalScrollBarPolicy = JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        verticalScrollBarPolicy = JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
    }

    private fun createStepCard(title: String, htmlContent: String, actionComponent: JComponent? = null): JPanel {
        val card = JBPanel<JBPanel<*>>().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtil.getBoundsColor(), 1, true),
                JBUI.Borders.empty(12)
            )
            alignmentX = JComponent.LEFT_ALIGNMENT
        }

        val titleLabel = JBLabel(title).apply {
            font = font.deriveFont(Font.BOLD)
            alignmentX = JComponent.LEFT_ALIGNMENT
            border = JBUI.Borders.emptyBottom(6)
        }
        card.add(titleLabel)

        val contentPane = JTextPane().apply {
            contentType = "text/html"
            isEditable = false
            isOpaque = false
            editorKit = javax.swing.text.html.HTMLEditorKit()
            alignmentX = JComponent.LEFT_ALIGNMENT
            text = htmlContent
        }
        card.add(contentPane)

        if (actionComponent != null) {
            val wrapper = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0)).apply {
                isOpaque = false
                alignmentX = JComponent.LEFT_ALIGNMENT
                border = JBUI.Borders.emptyTop(8)
                add(actionComponent)
            }
            card.add(wrapper)
        }

        return card
    }
}
