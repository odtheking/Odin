package com.odtheking.odin.clickgui.settings.impl

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.odtheking.odin.OdinMod.mc
import net.minecraft.client.gui.GuiGraphicsExtractor

open class HudElement(
    var x: Int,
    var y: Int,
    var scale: Float,
    enabled: Boolean = true,
) {
    var enabled: Boolean by mutableStateOf(enabled)

    var width: Int = 0
        private set
    var height: Int = 0
        private set

    val scaledWidth: Int get() = (width * scale).toInt()
    val scaledHeight: Int get() = (height * scale).toInt()

    fun onMeasured(measuredWidth: Int, measuredHeight: Int) {
        width = measuredWidth
        height = measuredHeight
    }

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
        enabled = !toggleable || element.get("enabled")?.asBoolean ?: enabled
    }

    companion object {
        const val MIN_SCALE = 0.5f
        const val MAX_SCALE = 5f
    }
}

fun GuiGraphicsExtractor.drawAtHud(hud: HudElement, content: GuiGraphicsExtractor.() -> Unit) {
    pose().pushMatrix()
    pose().translate(hud.x.toFloat(), hud.y.toFloat())
    pose().scale(hud.scale)
    content()
    pose().popMatrix()
}