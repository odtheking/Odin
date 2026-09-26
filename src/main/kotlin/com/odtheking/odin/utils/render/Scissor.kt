package com.odtheking.odin.utils.render

import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.navigation.ScreenRectangle
import org.joml.Matrix3x2fc
import org.joml.Vector2f
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

fun GuiGraphicsExtractor.pushScissor(x0: Int, y0: Int, x1: Int, y1: Int) {
    scissorStack.push(ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformOutward(pose()))
}

inline fun GuiGraphicsExtractor.scissored(x0: Int, y0: Int, x1: Int, y1: Int, block: () -> Unit) {
    pushScissor(x0, y0, x1, y1)
    try {
        block()
    } finally {
        disableScissor()
    }
}

inline fun GuiGraphicsExtractor.clipped(clip: Boolean, x0: Int, y0: Int, x1: Int, y1: Int, block: () -> Unit) {
    if (clip) scissored(x0, y0, x1, y1, block) else block()
}

inline fun GuiGraphicsExtractor.scissoredReveal(x0: Int, top: Int, x1: Int, revealed: Int, block: () -> Unit) {
    if (revealed <= 0) return
    scissored(x0, top, x1, top + revealed, block)
}

internal fun ScreenRectangle.transformOutward(pose: Matrix3x2fc): ScreenRectangle {
    val corner = scratch
    val l = left().toFloat()
    val t = top().toFloat()
    val r = right().toFloat()
    val b = bottom().toFloat()

    pose.transformPosition(l, t, corner)
    var minX = corner.x
    var maxX = corner.x
    var minY = corner.y
    var maxY = corner.y

    pose.transformPosition(r, t, corner)
    minX = min(minX, corner.x); maxX = max(maxX, corner.x)
    minY = min(minY, corner.y); maxY = max(maxY, corner.y)

    pose.transformPosition(r, b, corner)
    minX = min(minX, corner.x); maxX = max(maxX, corner.x)
    minY = min(minY, corner.y); maxY = max(maxY, corner.y)

    pose.transformPosition(l, b, corner)
    minX = min(minX, corner.x); maxX = max(maxX, corner.x)
    minY = min(minY, corner.y); maxY = max(maxY, corner.y)

    val left = floor(minX).toInt()
    val top = floor(minY).toInt()
    return ScreenRectangle(left, top, ceil(maxX).toInt() - left, ceil(maxY).toInt() - top)
}

private val scratch = Vector2f()