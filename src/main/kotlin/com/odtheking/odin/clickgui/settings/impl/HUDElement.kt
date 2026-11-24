package com.odtheking.odin.clickgui.settings.impl

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.odtheking.odin.OdinMod.mc
import net.minecraft.client.gui.GuiGraphicsExtractor
import kotlin.math.abs
import kotlin.math.roundToInt

open class HudElement(
    relX: Float,
    relY: Float,
    var scale: Float,
    enabled: Boolean = true,
) {
    var enabled: Boolean by mutableStateOf(enabled)

    var relX: Float = relX.unit()
        set(value) { field = value.unit() }
    var relY: Float = relY.unit()
        set(value) { field = value.unit() }

    var width: Int = 0
        private set
    var height: Int = 0
        private set

    val scaledWidth: Int get() = (width * scale).toInt()
    val scaledHeight: Int get() = (height * scale).toInt()

    private val freeWidth get() = (mc.window.guiScaledWidth - scaledWidth).coerceAtLeast(0)
    private val freeHeight get() = (mc.window.guiScaledHeight - scaledHeight).coerceAtLeast(0)

    private var legacy: Pair<Int, Int>? = null

    var x: Int
        get() = legacy?.first?.coerceIn(0, freeWidth) ?: (relX * freeWidth).roundToInt()
        set(value) { relX = value.toFloat() / freeWidth }

    var y: Int
        get() = legacy?.second?.coerceIn(0, freeHeight) ?: (relY * freeHeight).roundToInt()
        set(value) { relY = value.toFloat() / freeHeight }

    fun measure(width: Int, height: Int) {
        if (width <= 0 || height <= 0) return
        this.width = width
        this.height = height
        legacy?.let { (lx, ly) ->
            legacy = null
            x = lx
            y = ly
        }
    }

    fun snap(others: List<HudElement>): Pair<Int?, Int?> {
        val huds = others.filter { it !== this }
        val lineX = snapAxis(x, scaledWidth, lines(0, mc.window.guiScaledWidth) + huds.flatMap { lines(it.x, it.scaledWidth) }) { x = it }
        val lineY = snapAxis(y, scaledHeight, lines(0, mc.window.guiScaledHeight) + huds.flatMap { lines(it.y, it.scaledHeight) }) { y = it }
        return lineX to lineY
    }

    private inline fun snapAxis(pos: Int, size: Int, targets: List<Int>, set: (Int) -> Unit): Int? {
        val (line, offset) = targets.flatMap { line -> lines(0, size).map { line to it } }
            .minBy { (line, offset) -> abs(pos + offset - line) }
            .takeIf { (line, offset) -> abs(pos + offset - line) <= SNAP_RANGE } ?: return null
        set(line - offset)
        return line
    }

    private fun lines(start: Int, size: Int) = listOf(start, start + size / 2, start + size)

    fun reset() {
        legacy = null
        relX = DEFAULT_POS
        relY = DEFAULT_POS
        scale = 1f
    }

    fun write(): JsonObject =
        JsonObject().apply {
            val pending = legacy
            if (pending != null) {
                addProperty("x", pending.first)
                addProperty("y", pending.second)
            } else {
                addProperty("relX", relX)
                addProperty("relY", relY)
            }
            addProperty("width", width)
            addProperty("height", height)
            addProperty("scale", scale)
            addProperty("enabled", enabled)
        }

    fun read(element: JsonElement, toggleable: Boolean) {
        if (element !is JsonObject) return

        if (element.has("relX")) {
            legacy = null
            relX = element.get("relX").asFloat
            relY = element.get("relY")?.asFloat ?: relY
        } else if (element.has("x")) {
            legacy = element.get("x").asInt to (element.get("y")?.asInt ?: 0)
        }
        width = element.get("width")?.asInt ?: width
        height = element.get("height")?.asInt ?: height
        scale = element.get("scale")?.asFloat ?: scale
        enabled = !toggleable || element.get("enabled")?.asBoolean ?: enabled
    }

    companion object {
        const val MIN_SCALE = 0.5f
        const val MAX_SCALE = 5f
        const val DEFAULT_POS = 0.01f
        const val SNAP_RANGE = 4

        private fun Float.unit(): Float = if (isNaN()) 0f else coerceIn(0f, 1f)
    }
}

inline fun <T> GuiGraphicsExtractor.drawAtHud(hud: HudElement, content: GuiGraphicsExtractor.() -> T): T {
    pose().pushMatrix()
    pose().translate(hud.x.toFloat(), hud.y.toFloat())
    pose().scale(hud.scale)
    return content().also { pose().popMatrix() }
}