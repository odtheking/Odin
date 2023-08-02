package me.odinclient.ui.hud

import cc.polyfrost.oneconfig.renderer.font.Fonts
import cc.polyfrost.oneconfig.utils.dsl.drawText
import cc.polyfrost.oneconfig.utils.dsl.nanoVG
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.utils.render.RenderUtils2d
import me.odinclient.utils.render.gui.MouseUtils
import net.minecraft.client.renderer.GlStateManager
import java.awt.Color
import java.util.*
import kotlin.math.floor

class ExampleHud(val hud: BaseHud) {
    private var dragging = false

    private var x2 = 0f
    private var y2 = 0f

    fun draw() {
        if (dragging) {
            hud.x = floor(x2 + MouseUtils.mouseX / 2)
            hud.y = floor(y2 + MouseUtils.mouseY / 2)
        }

        RenderUtils2d.drawRect(
            hud.x - 2f, hud.y - 3f, hud.width + 4f, hud.height + 2f,
            if (isHovered) Color(0, 0, 0, 100) else Color(0, 0, 0, 40)
        )

        hud.render(example = true)
    }

    private val isHovered get() = MouseUtils.isAreaHovered((hud.x - 2f) * 2, (hud.y - 3f) * 2, (hud.width + 4f) * 2, (hud.height + 2f) * 2)

    fun mouseReleased(state: Int) {
        if (state == 0) dragging = false
    }

    fun mouseClicked(mouseButton: Int) {
        if (isHovered) {
            if (mouseButton == 0) {
                x2 = hud.x - MouseUtils.mouseX / 2
                y2 = hud.y - MouseUtils.mouseY / 2
                dragging = true
            }
        }
    }

    fun onClose() {
        dragging = false
    }
}