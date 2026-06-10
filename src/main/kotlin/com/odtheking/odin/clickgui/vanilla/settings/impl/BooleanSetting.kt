package com.odtheking.odin.clickgui.vanilla.settings.impl

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.settings.ClickGUI.gray38
import com.odtheking.odin.clickgui.settings.Saving
import com.odtheking.odin.clickgui.vanilla.VanillaRenderableSetting
import com.odtheking.odin.features.impl.render.ClickGUIModule
import com.odtheking.odin.utils.Color.Companion.brighter
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.DrawContextRenderer.drawCircle
import com.odtheking.odin.utils.render.roundedFill
import com.odtheking.odin.utils.ui.animations.LinearAnimation
import com.odtheking.odin.utils.ui.isAreaHovered
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.input.MouseButtonEvent

class BooleanSetting(
    name: String,
    override val default: Boolean = false,
    desc: String,
) : VanillaRenderableSetting<Boolean>(name, desc), Saving {

    override var value: Boolean = default
    var enabled: Boolean by this::value

    private val toggleAnimation = LinearAnimation<Float>(200)

    private val TRACK_W    = 22
    private val TRACK_H    = 14
    private val TRACK_R    = 6
    private val RIGHT_PAD  = 4
    private val KNOB_R     = 4

    override fun render(graphics: GuiGraphics, x: Int, y: Int, mouseX: Int, mouseY: Int): Int {
        super.render(graphics, x, y, mouseX, mouseY)
        val height = getHeight()
        val hovered = isHovered

        val trackX = x + width - RIGHT_PAD - TRACK_W
        val trackY = y + height / 2 - TRACK_H / 2

        graphics.drawString(mc.font, name, x + 6, y + height / 2 - 4, Colors.WHITE.rgba, false)

        val bgColor = if (hovered) gray38.brighter().rgba else gray38.rgba
        graphics.roundedFill(trackX, trackY, trackX + TRACK_W, trackY + TRACK_H, bgColor, TRACK_R, ClickGUIModule.clickGUIColor.rgba, 2f)

        if (enabled || toggleAnimation.isAnimating()) {
            val fillW = toggleAnimation.get(TRACK_W.toFloat(), 6f, enabled).toInt()
            graphics.roundedFill(trackX + 1, trackY + 1, trackX + fillW, trackY + TRACK_H - 1, ClickGUIModule.clickGUIColor.rgba, 6)
        }

        val knobCx = x + width - toggleAnimation.get(19f, 11f, !enabled).toInt()
        val knobCy = y + height / 2
        graphics.drawCircle(knobCx, knobCy, KNOB_R, Colors.WHITE.rgba)

        return height
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, click: MouseButtonEvent): Boolean {
        return if (click.button() != 0 || !isHovered) false
        else {
            toggleAnimation.start()
            enabled = !enabled
            true
        }
    }

    override val isHovered get() = isAreaHovered(lastX + width - RIGHT_PAD - TRACK_W - 2, lastY + getHeight() / 2 - TRACK_H / 2 - 2, TRACK_W + 4, TRACK_H + 4)

    override fun write(gson: Gson): JsonElement = JsonPrimitive(enabled)

    override fun read(element: JsonElement, gson: Gson) {
        enabled = element.asBoolean
    }
}