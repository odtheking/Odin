package me.odinmain.clickgui.settings.impl

import me.odinmain.utils.render.Colors
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.ui.isAreaHovered
import net.minecraft.client.renderer.GlStateManager

open class HudElement(
    var x: Float,
    var y: Float,
    var scale: Float,
    var enabled: Boolean = true,
    val render: (Boolean) -> Pair<Number, Number> = { _ -> 0f to 0f }
) {
    var width = 0f
        private set
    var height = 0f
        private set

    fun draw(example: Boolean) {
        GlStateManager.pushMatrix()
        GlStateManager.translate(x, y, 1f)
        GlStateManager.scale(scale, scale, 1f)
        val (width, height) = render(example).let { (w, h) -> w.toFloat() to h.toFloat() }

        if (example) RenderUtils.hollowRect(0f, 0f, width, height, 1 / scale + if (isHovered()) 0.5f else 0f, Colors.WHITE)

        GlStateManager.popMatrix()

        this.width = width
        this.height = height
    }

    fun isHovered(): Boolean = isAreaHovered(x, y, width * scale, height * scale)
}