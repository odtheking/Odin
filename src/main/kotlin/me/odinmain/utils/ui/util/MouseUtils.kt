package me.odinmain.utils.ui.util

import me.odinmain.OdinMain.mc
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.Display


object MouseUtils {

    val mouseX: Float
        get() = Mouse.getX().toFloat()

    val mouseY: Float
        get() = mc.displayHeight - Mouse.getY() - 1f

    fun isAreaHovered(x: Float, y: Float, w: Float, h: Float): Boolean {
        return mouseX in x..x + w && mouseY in y..y + h
    }

    fun isAreaHovered(x: Float, y: Float, w: Float): Boolean {
        return mouseX in x..x + w && mouseY >= y
    }

    fun getQuadrant(): Int {
        return when {
            mouseX >= Display.getWidth() / 2 -> if (mouseY >= Display.getHeight() / 2) 4 else 2
            else -> if (mouseY >= Display.getHeight() / 2) 3 else 1
        }
    }
}