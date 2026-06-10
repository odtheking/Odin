package com.odtheking.odin.clickgui.vanilla.settings.impl

import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.hollowFill
import com.odtheking.odin.utils.ui.isAreaHovered
import net.minecraft.client.gui.GuiGraphics

open class HudElement(
    var x: Int,
    var y: Int,
    var scale: Float,
    var enabled: Boolean = true,
    val render: GuiGraphics.(Boolean) -> Pair<Int, Int> = { _ -> 0 to 0 }
) {
    var width: Int = 0
        private set
    var height: Int = 0
        private set

    fun draw(context: GuiGraphics, example: Boolean) {
        context.pose().pushMatrix()
        context.pose().translate(x.toFloat(), y.toFloat())

        context.pose().scale(scale, scale)
        val (width, height) = context.render(example).let { (w, h) -> w to h }

        context.pose().popMatrix()
        if (example) context.hollowFill(x - 1, y - 1, (width * scale).toInt(), (height * scale).toInt(), if (isHovered()) 2 else 1, Colors.WHITE)

        this.width = width
        this.height = height
    }

    fun isHovered(): Boolean = isAreaHovered(x.toFloat(), y.toFloat(), width * scale, height * scale)
}