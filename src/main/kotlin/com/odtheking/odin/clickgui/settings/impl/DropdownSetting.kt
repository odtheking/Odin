package com.odtheking.odin.clickgui.settings.impl

import com.odtheking.odin.clickgui.settings.RenderableSetting
import com.odtheking.odin.clickgui.widget.drawIcon
import com.odtheking.odin.utils.ui.animations.Easing
import com.odtheking.odin.utils.ui.animations.Fade
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.resources.Identifier
import kotlin.String
import kotlin.math.PI

/**
 * A setting intended to show or hide other settings in the GUI.
 *
 * @author Bonsai
 */
class DropdownSetting(
    name: String,
    override val default: Boolean = false,
    desc: String
) : RenderableSetting<Boolean>(name, desc) {

    override var value: Boolean = default
    private var enabled: Boolean by this::value

    private val toggleAnimation = Fade(FLIP_DURATION, Easing.EASE_IN_OUT)

    override fun render(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        drawLabel(graphics)

        val iconX = x + width - 12 - 8
        val iconY = y + (height - 12) / 2

        graphics.drawIcon(CHEVRON, iconX, iconY, 15, hover, toggleAnimation.progress(enabled) * QUARTER_TURN)
    }

    override fun onClick(event: MouseButtonEvent, doubleClick: Boolean) {
        enabled = !enabled
    }

    private companion object {
        val CHEVRON: Identifier = Identifier.fromNamespaceAndPath("odin", "textures/chevron.png")
        const val QUARTER_TURN = (PI / 2).toFloat()
        const val FLIP_DURATION = 200L
    }
}