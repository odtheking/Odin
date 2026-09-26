package com.odtheking.odin.clickgui.settings

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.GuiTheme
import com.odtheking.odin.clickgui.widget.OdinContainerWidget
import com.odtheking.odin.clickgui.widget.isOver
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.scissoredReveal
import com.odtheking.odin.utils.ui.animations.Easing
import com.odtheking.odin.utils.ui.animations.Fade
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.network.chat.Component

abstract class RenderableSetting<T>(
    override val name: String,
    override var description: String,
    height: Int = GuiTheme.ROW_HEIGHT
) : OdinContainerWidget(0, 0, GuiTheme.ROW_WIDTH, height, Component.literal(name)), Setting<T> {

    override var hidden: Boolean = false

    override var visibilityDependency: (() -> Boolean)? = null

    override val clickButtons: IntArray = LEFT_ONLY

    private val reveal = Fade(REVEAL_DURATION, Easing.EASE_IN_OUT)

    val revealFraction: Float get() = reveal.progress(isVisible)

    override fun children(): List<GuiEventListener> = emptyList()

    open fun measure() = Unit

    open fun place() = Unit

    open fun release() = Unit

    final override fun wantsDescription(mouseX: Int, mouseY: Int): Boolean =
        isOver(mouseX, mouseY, x, y, width, GuiTheme.ROW_HEIGHT)

    protected fun drawLabel(graphics: GuiGraphicsExtractor, rowY: Int = y, rowHeight: Int = GuiTheme.ROW_HEIGHT) {
        graphics.text(mc.font, name, x + GuiTheme.PADDING, GuiTheme.textY(rowY, rowHeight), Colors.WHITE.rgba, false)
    }

    protected inline fun renderExpanded(graphics: GuiGraphicsExtractor, content: () -> Unit) {
        graphics.scissoredReveal(x, y + GuiTheme.ROW_HEIGHT, x + width, height - GuiTheme.ROW_HEIGHT, content)
    }

    private companion object {
        const val REVEAL_DURATION = 200L
    }
}