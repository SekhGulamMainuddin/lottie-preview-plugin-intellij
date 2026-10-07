package com.lottiepreview.plugin.ui

import com.intellij.icons.AllIcons
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.system.CpuArch
import com.intellij.openapi.util.SystemInfo
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Font
import java.awt.datatransfer.StringSelection
import javax.swing.*
import javax.swing.event.HyperlinkEvent

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
        val ideName = com.intellij.openapi.application.ApplicationInfo.getInstance().versionName
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
                    To preview Lottie and dotLottie animations, $ideName needs <b>JCEF</b> (the embedded Chromium browser).
                    Without JCEF, web-based rendering is unavailable in this IDE installation.
                </body>
                </html>
            """.trimIndent()
        }
        contentPanel.add(descPane)

        // Step 1: Plugin installation for Rabbit (2026.2+)
        val ideBuild = com.intellij.openapi.application.ApplicationInfo.getInstance().build.asStringWithoutProductCode()
        val platformSuffix = jcefPluginPlatformSuffix()
        val step1Panel = createStepCard(
            "1. Install \"Web Browser (JCEF)\" Plugin (Android Studio Rabbit 2026.2+)",
            """
                <html>
                <body style="font-family: '$fontName', sans-serif; font-size: ${fontSize}px; color: $textColor; line-height: 1.4;">
                    Starting with <b>Android Studio Rabbit (2026.2+)</b>, JCEF is provided via a separate plugin:
                    <ol style="margin-top: 4px; padding-left: 20px;">
                        <li>Open top menu: <b>Android Studio > Settings</b> (or <b>Preferences</b>).</li>
                        <li>Select <b>Plugins</b> from the left sidebar and click the <b>Marketplace</b> tab.</li>
                        <li>Search for <b>"Web Browser (JCEF)"</b> (by JetBrains).</li>
                        <li>Click <b>Install</b>, then fully quit and reopen $ideName.</li>
                    </ol>
                    Each JCEF plugin release works with <b>one exact IDE build</b>. If the Marketplace shows nothing
                    or the install fails:
                    <ol style="margin-top: 4px; padding-left: 20px;">
                        <li>Open <a href="$JCEF_PLUGIN_VERSIONS_URL">the Web Browser (JCEF) versions page</a>.</li>
                        <li>Download the version matching your IDE build
                            <code style="background-color: $codeBgColor; color: $codeTextColor;">$ideBuild</code>
                            and platform <code style="background-color: $codeBgColor; color: $codeTextColor;">$platformSuffix</code>.</li>
                        <li>In <b>Settings > Plugins</b>, click the gear icon > <b>Install Plugin from Disk...</b> and pick the downloaded zip.</li>
                        <li>Fully quit and reopen $ideName.</li>
                    </ol>
                </body>
                </html>
            """.trimIndent()
        )
        contentPanel.add(step1Panel)
        contentPanel.add(Box.createRigidArea(Dimension(0, 12)))

        // Step 2 Section: Boot Runtime
        val step2Panel = createStepCard(
            "2. Switch to a JCEF Runtime (older IDEs, or if the runtime lacks JCEF)",
            """
                <html>
                <body style="font-family: '$fontName', sans-serif; font-size: ${fontSize}px; color: $textColor; line-height: 1.4;">
                    Ensure the IDE boot runtime has JCEF native support enabled:
                    <ol style="margin-top: 4px; padding-left: 20px;">
                        <li>Press <code style="background-color: $codeBgColor; color: $codeTextColor;">Cmd+Shift+A</code> (macOS) or <code style="background-color: $codeBgColor; color: $codeTextColor;">Ctrl+Shift+A</code> (Windows/Linux) to open Action Search.</li>
                        <li>Type <b>"Choose Boot Java Runtime for the IDE"</b> and press Enter.</li>
                        <li>Select a runtime version that includes <b>"with JCEF"</b> (or <b>"-jcef"</b>).</li>
                        <li>Click <b>OK / Install</b> and <b>Restart</b> $ideName.</li>
                    </ol>
                </body>
                </html>
            """.trimIndent()
        )
        contentPanel.add(step2Panel)
        contentPanel.add(Box.createRigidArea(Dimension(0, 12)))

        // Step 3 Section: Force Enable via Registry
        val step3Panel = createStepCard(
            "3. Force-Enable via Registry",
            """
                <html>
                <body style="font-family: '$fontName', sans-serif; font-size: ${fontSize}px; color: $textColor; line-height: 1.4;">
                    If the runtime is switched but preview still fails:
                    <ol style="margin-top: 4px; padding-left: 20px;">
                        <li>Open Action Search and search for <b>"Registry..."</b>.</li>
                        <li>Locate the key <code style="background-color: $codeBgColor; color: $codeTextColor;">ide.browser.jcef.enabled</code>.</li>
                        <li>Ensure it is <b>checked</b>.</li>
                        <li>Restart $ideName.</li>
                    </ol>
                </body>
                </html>
            """.trimIndent()
        )
        contentPanel.add(step3Panel)
        contentPanel.add(Box.createRigidArea(Dimension(0, 12)))

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

        val step4Panel = createStepCard(
            "4. Enable via idea.properties (Alternative)",
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
        contentPanel.add(step4Panel)

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
            addHyperlinkListener { event ->
                if (event.eventType == HyperlinkEvent.EventType.ACTIVATED) {
                    event.url?.let { BrowserUtil.browse(it) }
                }
            }
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

    /** Platform suffix used in Web Browser (JCEF) plugin version names, e.g. `mac-arm64`. */
    private fun jcefPluginPlatformSuffix(): String {
        val os = when {
            SystemInfo.isMac -> "mac"
            SystemInfo.isWindows -> "windows"
            else -> "linux"
        }
        val arch = if (CpuArch.isArm64()) "arm64" else "x86_64"
        return "$os-$arch"
    }

    private companion object {
        const val JCEF_PLUGIN_VERSIONS_URL = "https://plugins.jetbrains.com/plugin/31360-web-browser-jcef-/versions"
    }
}
