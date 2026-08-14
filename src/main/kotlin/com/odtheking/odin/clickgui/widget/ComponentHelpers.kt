package com.odtheking.odin.clickgui.widget

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.GuiTheme
import com.odtheking.odin.clickgui.hoverTint
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.Corners
import com.odtheking.odin.utils.render.circle
import com.odtheking.odin.utils.render.roundedRectClipped
import com.odtheking.odin.utils.render.roundedRectOutlined
import com.odtheking.odin.utils.render.roundedTexture
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.EditBox
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import kotlin.math.roundToInt

object Toggle {
    const val WIDTH = 24
    const val HEIGHT = 16

    private const val KNOB_INSET = 4

    internal const val RADIUS = 9f
    internal const val KNOB_RADIUS = (HEIGHT / 2 - KNOB_INSET).toFloat()

    internal const val KNOB_HOME = HEIGHT / 2

    internal const val TRAVEL = WIDTH - HEIGHT

    internal val CORNERS = Corners(RADIUS)
}

fun GuiGraphicsExtractor.drawToggle(x: Int, centerY: Int, progress: Float, hover: Float) {
    val top = centerY - Toggle.HEIGHT / 2
    val bottom = top + Toggle.HEIGHT
    val right = x + Toggle.WIDTH
    val corners = Toggle.CORNERS

    roundedRectOutlined(x, top, right, bottom, GuiTheme.surface.hoverTint(hover), GuiTheme.accent.rgba, 1.5f, corners)

    val fillRight = x + (Toggle.WIDTH * progress).roundToInt()

    if (progress > 0f) roundedRectClipped(x, top, right, bottom, x, top, fillRight, bottom, GuiTheme.accent.hoverTint(hover), corners)

    circle(x + Toggle.KNOB_HOME + (Toggle.TRAVEL * progress).roundToInt(), centerY, Toggle.KNOB_RADIUS, Colors.WHITE.rgba)
}

private const val FIELD_HEIGHT = 8

fun rowTextField(width: Int, name: String, centered: Boolean): EditBox =
    EditBox(mc.font, 0, 0, width, FIELD_HEIGHT, Component.literal(name)).stripChrome(centered)

fun <T : EditBox> T.stripChrome(centered: Boolean): T = apply {
    isBordered = false
    setCentered(centered)
    setTextShadow(false)
    setTextColor(Colors.WHITE.rgba)
}

private const val HOVER_GROWTH = 0.15f

fun GuiGraphicsExtractor.drawIcon(
    texture: Identifier, x: Int, y: Int, size: Int, hover: Float, rotation: Float = 0f
) {
    val centerX = x + size / 2f
    val centerY = y + size / 2f
    val scale = 1f + HOVER_GROWTH * hover

    pose().pushMatrix()
    pose().translate(centerX, centerY)
    pose().scale(scale, scale)
    pose().rotate(rotation)
    pose().translate(-centerX, -centerY)
    roundedTexture(x, y, x + size, y + size, texture)
    pose().popMatrix()
}
