package me.odinmain.utils.ui

import me.odinmain.OdinMain.mc
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.Display

const val VIRTUAL_WIDTH = 1920f
const val VIRTUAL_HEIGHT = 1080f

inline val mouseX: Float get() =
    Mouse.getX().toFloat() * (VIRTUAL_WIDTH / Display.getWidth())

inline val mouseY: Float get() =
    (mc.displayHeight - Mouse.getY() - 1f) * (VIRTUAL_HEIGHT / Display.getHeight())

fun isAreaHovered(x: Float, y: Float, w: Float, h: Float): Boolean =
    mouseX in x..x + w && mouseY in y..y + h

fun isAreaHovered(x: Float, y: Float, w: Float): Boolean =
    mouseX in x..x + w && mouseY >= y

fun getQuadrant(): Int =
    when {
        mouseX >= VIRTUAL_WIDTH / 2 -> if (mouseY >= VIRTUAL_HEIGHT / 2) 4 else 2
        else -> if (mouseY >= VIRTUAL_HEIGHT / 2) 3 else 1
    }