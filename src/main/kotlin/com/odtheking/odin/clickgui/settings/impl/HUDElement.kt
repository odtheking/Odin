package com.odtheking.odin.clickgui.settings.impl

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.hollowFill
import com.odtheking.odin.utils.render.roundedOutline
import net.minecraft.client.gui.GuiGraphicsExtractor

open class HudElement(
    var x: Int,
    var y: Int,
    var scale: Float,
    var enabled: Boolean = true,
    val render: GuiGraphicsExtractor.(Boolean) -> Pair<Int, Int> = { _ -> 0 to 0 }
) {
    var width: Int = 0
        private set
    var height: Int = 0
        private set

    val scaledWidth: Int get() = (width * scale).toInt()
    val scaledHeight: Int get() = (height * scale).toInt()

    fun draw(context: GuiGraphicsExtractor, example: Boolean, mouseX: Int = -1, mouseY: Int = -1) {
        context.pose().pushMatrix()
        context.pose().translate(x.toFloat(), y.toFloat())
        context.pose().scale(scale, scale)
        val (renderedWidth, renderedHeight) = context.render(example)
        context.pose().popMatrix()

        width = renderedWidth
        height = renderedHeight

        if (example)
            context.roundedOutline(x - 1, y - 1, x + scaledWidth + 1, y + scaledHeight + 1, Colors.WHITE.rgba, if (isHovered(mouseX, mouseY)) 1.5f else 1f, 3f)
    }

    fun isHovered(mouseX: Int, mouseY: Int): Boolean =
        mouseX >= x && mouseX < x + scaledWidth && mouseY >= y && mouseY < y + scaledHeight

    fun clampToScreen() {
        x = x.coerceIn(0, (mc.window.guiScaledWidth - scaledWidth).coerceAtLeast(0))
        y = y.coerceIn(0, (mc.window.guiScaledHeight - scaledHeight).coerceAtLeast(0))
    }

    fun write(): JsonObject =
        JsonObject().apply {
            addProperty("x", x)
            addProperty("y", y)
            addProperty("scale", scale)
            addProperty("enabled", enabled)
        }

    fun read(element: JsonElement, toggleable: Boolean) {
        if (element !is JsonObject) return

        x = element.get("x")?.asInt ?: x
        y = element.get("y")?.asInt ?: y
        scale = element.get("scale")?.asFloat ?: scale
        enabled = if (toggleable) element.get("enabled")?.asBoolean ?: enabled else true
    }

    companion object {
        const val MIN_SCALE = 0.5f
        const val MAX_SCALE = 5f
    }
}