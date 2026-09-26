package com.odtheking.odin.clickgui.widget

import com.odtheking.odin.clickgui.ClickGUI
import com.odtheking.odin.utils.ui.animations.Fade
import net.minecraft.client.gui.ComponentPath
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.events.ContainerEventHandler
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.navigation.FocusNavigationEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.client.input.MouseButtonInfo
import net.minecraft.client.sounds.SoundManager
import net.minecraft.network.chat.Component

abstract class OdinWidget(x: Int, y: Int, width: Int, height: Int, message: Component) : AbstractWidget(x, y, width, height, message) {
    private val hoverFade = Fade(HOVER_DURATION)
    private val descriptionDelay = Fade(DESCRIPTION_DELAY, settles = false)

    private var boundsHovered = false

    protected open val clickButtons = LEFT_ONLY
    protected open val description get() = ""

    protected val hover get() = hoverFade.progress(boundsHovered)

    final override fun isValidClickButton(buttonInfo: MouseButtonInfo): Boolean = buttonInfo.button() in clickButtons

    final override fun extractWidgetRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        boundsHovered = visible && isOver(mouseX, mouseY, x, y, width, height)

        render(graphics, mouseX, mouseY)

        val resting = wantsDescription(mouseX, mouseY)
        if (description.isNotEmpty() && resting && descriptionDelay.progress(true) >= 1f)
            ClickGUI.setDescription(description, right + DESCRIPTION_GAP, y)
        else if (!resting) descriptionDelay.progress(false)
    }

    protected open fun wantsDescription(mouseX: Int, mouseY: Int): Boolean = boundsHovered

    protected abstract fun render(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int)
    override fun playDownSound(soundManager: SoundManager) = Unit
    override fun updateWidgetNarration(output: NarrationElementOutput) = defaultButtonNarrationText(output)

    companion object {
        const val LEFT = 0
        const val RIGHT = 1

        val LEFT_ONLY = intArrayOf(LEFT)
        val BOTH_BUTTONS = intArrayOf(LEFT, RIGHT)

        private const val HOVER_DURATION = 150L
        private const val DESCRIPTION_DELAY = 600L
        private const val DESCRIPTION_GAP = 10
    }
}

abstract class OdinContainerWidget(x: Int, y: Int, width: Int, height: Int, message: Component) : OdinWidget(x, y, width, height, message), ContainerEventHandler {
    private var focusedChild: GuiEventListener? = null
    private var draggingChild = false
    private var selfFocused = false

    override val clickButtons: IntArray = BOTH_BUTTONS
    abstract override fun children(): List<GuiEventListener>
    override fun getFocused(): GuiEventListener? = focusedChild

    override fun setFocused(listener: GuiEventListener?) {
        if (focusedChild === listener) return
        focusedChild?.isFocused = false
        listener?.isFocused = true
        focusedChild = listener
    }

    override fun isDragging(): Boolean = draggingChild

    override fun setDragging(dragging: Boolean) {
        draggingChild = dragging
    }

    override fun isFocused(): Boolean = selfFocused || focusedChild != null

    override fun setFocused(focused: Boolean) {
        selfFocused = focused
        if (!focused) setFocused(null)
    }

    override fun nextFocusPath(event: FocusNavigationEvent): ComponentPath? =
        super<ContainerEventHandler>.nextFocusPath(event)

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean =
        super<ContainerEventHandler>.mouseClicked(event, doubleClick) ||
            super<OdinWidget>.mouseClicked(event, doubleClick)

    override fun mouseReleased(event: MouseButtonEvent): Boolean =
        super<ContainerEventHandler>.mouseReleased(event) || super<OdinWidget>.mouseReleased(event)

    override fun mouseDragged(event: MouseButtonEvent, dragX: Double, dragY: Double): Boolean =
        super<ContainerEventHandler>.mouseDragged(event, dragX, dragY) ||
            super<OdinWidget>.mouseDragged(event, dragX, dragY)

    override fun render(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        renderChildren(graphics, mouseX, mouseY)
    }

    protected fun renderChildren(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        for (child in children()) {
            if (child is AbstractWidget) child.extractRenderState(graphics, mouseX, mouseY, 0f)
        }
    }
}

internal fun isOver(mouseX: Int, mouseY: Int, x: Int, y: Int, width: Int, height: Int): Boolean =
    mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height