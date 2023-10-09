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
}