package com.odtheking.odin.utils.render

import com.odtheking.odin.OdinMod.mc
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.navigation.ScreenRectangle
import org.joml.Matrix3x2fc
import org.joml.Vector2f
import kotlin.math.*

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

fun GuiGraphicsExtractor.pushPixelSnap(x: Int, y: Int, width: Int, height: Int): Boolean {
    val pose = pose()
    if (pose.m01() != 0f || pose.m10() != 0f || pose.m00() <= 0f || pose.m11() <= 0f) return false
    val pixels = mc.window.guiScale.toFloat()
    val devL = (pose.m00() * x + pose.m20()) * pixels
    val devT = (pose.m11() * y + pose.m21()) * pixels
    val devW = pose.m00() * width * pixels
    val devH = pose.m11() * height * pixels
    if (devW < 1e-3f || devH < 1e-3f) return false

    val left = round(devL)
    val top = round(devT)
    val right = round(devL + devW).coerceAtLeast(left + 1f)
    val bottom = round(devT + devH).coerceAtLeast(top + 1f)

    val scaleX = (right - left) / devW
    val scaleY = (bottom - top) / devH
    val shiftX = (left - devL) / (pixels * pose.m00())
    val shiftY = (top - devT) / (pixels * pose.m11())
    if (abs(shiftX) < 1e-4f && abs(shiftY) < 1e-4f && abs(scaleX - 1f) < 1e-4f && abs(scaleY - 1f) < 1e-4f) return false

    pose.pushMatrix()
    pose.translate(x + shiftX, y + shiftY)
    pose.scale(scaleX, scaleY)
    pose.translate(-x.toFloat(), -y.toFloat())
    return true
}

fun ScreenRectangle.transformOutward(pose: Matrix3x2fc): ScreenRectangle {
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