package me.odinmain.utils.render.gui

import me.odinmain.OdinMain.mc
import org.lwjgl.input.Mouse


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

    fun getQuadrant(mouseX: Int, mouseY: Int): Int {
        var guiSize = mc.gameSettings.guiScale * 2
        if (mc.gameSettings.guiScale == 0) guiSize = 10

        val screenY = mc.displayHeight / guiSize
        val screenX = mc.displayWidth / guiSize

        return when {
            mouseX >= screenX -> if (mouseY >= screenY) 4 else 2
            else -> if (mouseY >= screenY) 3 else 1
        }
    }
}