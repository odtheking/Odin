package me.odinclient.clickgui.util

import me.odinclient.clickgui.ClickGUI
import net.minecraft.util.MathHelper
import org.lwjgl.input.Mouse
import me.odinclient.OdinClient.Companion.mc

object MouseUtils {
    val scaledMouseX get() =
        MathHelper.ceiling_double_int(Mouse.getX() / ClickGUI.CLICK_GUI_SCALE)

    val scaledMouseY get() =
        MathHelper.ceiling_double_int( (mc.displayHeight - Mouse.getY()) / ClickGUI.CLICK_GUI_SCALE)

    fun isAreaHovered(x: Int, y: Int, width: Int, height: Int): Boolean =
        scaledMouseX in x..x + width && scaledMouseY in y..y + height

    fun isAreaHovered(x: Int, y: Int, width: Int): Boolean =
        scaledMouseX in x..x + width && scaledMouseY >= y

    fun isAreaHovered(x: Double, y: Double, width: Double, height: Double): Boolean =
        scaledMouseX.toDouble() in x..x + width && scaledMouseY.toDouble() in y..y + height

    fun isActualAreaHovered(x: Int, y: Int, x2: Int, y2: Int): Boolean =
        scaledMouseX in x..x2 && scaledMouseY in y..y2
}