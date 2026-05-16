package com.lottiepreview.plugin.ui

import java.awt.Container
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.JScrollPane
import javax.swing.SwingUtilities

/**
 * FlowLayout subclass that fully supports wrapping of components.
 */
class WrapLayout : FlowLayout {
    constructor() : super()
    constructor(align: Int) : super(align)
    constructor(align: Int, hgap: Int, vgap: Int) : super(align, hgap, vgap)

    override fun preferredLayoutSize(target: Container): Dimension {
        return layoutSize(target, true)
    }

    override fun minimumLayoutSize(target: Container): Dimension {
        val minimum = layoutSize(target, false)
        minimum.width -= hgap * 2
        return minimum
    }

    private fun layoutSize(target: Container, preferred: Boolean): Dimension {
        synchronized(target.treeLock) {
            var targetWidth = target.size.width

            if (targetWidth == 0) {
                targetWidth = Integer.MAX_VALUE
            }

            val insets = target.insets
            val horizontalInsetsAndGap = insets.left + insets.right + hgap * 2
            val maxWidth = targetWidth - horizontalInsetsAndGap

            val dim = Dimension(0, 0)
            var rowWidth = 0
            var rowHeight = 0

            for (i in 0 until target.componentCount) {
                val m = target.getComponent(i)
                if (m.isVisible) {
                    val d = if (preferred) m.preferredSize else m.minimumSize

                    if (rowWidth + d.width > maxWidth) {
                        addRow(dim, rowWidth, rowHeight)
                        rowWidth = 0
                        rowHeight = 0
                    }

                    if (rowWidth != 0) {
                        rowWidth += hgap
                    }

                    rowWidth += d.width
                    rowHeight = Math.max(rowHeight, d.height)
                }
            }

            addRow(dim, rowWidth, rowHeight)

            dim.width += horizontalInsetsAndGap
            dim.height += insets.top + insets.bottom + vgap * 2

            val scrollPane = SwingUtilities.getAncestorOfClass(JScrollPane::class.java, target)
            if (scrollPane != null && target.parent is javax.swing.JViewport) {
                dim.width -= (scrollPane as JScrollPane).verticalScrollBar.preferredSize.width
            }

            return dim
        }
    }

    private fun addRow(dim: Dimension, rowWidth: Int, rowHeight: Int) {
        dim.width = Math.max(dim.width, rowWidth)
        if (dim.height > 0) {
            dim.height += vgap
        }
        dim.height += rowHeight
    }
}
