package me.odinclient.ui.hud

import cc.polyfrost.oneconfig.renderer.font.Fonts
import cc.polyfrost.oneconfig.utils.dsl.drawText
import cc.polyfrost.oneconfig.utils.dsl.nanoVG
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.utils.render.RenderUtils2d
import me.odinclient.utils.render.gui.MouseUtils
import me.odinclient.utils.skyblock.ChatUtils.modMessage
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

        GlStateManager.translate(hud.x, hud.y, 0f)
        GlStateManager.scale(hud.scale, hud.scale, 1f)

        RenderUtils2d.drawRect(
            -2f, -3f, hud.width + 4f, hud.height + 2f,
            if (isHovered) Color(0, 0, 0, 110) else Color(0, 0, 0, 40)
        )

        hud.render(example = true)

        GlStateManager.scale(1f / hud.scale, 1f / hud.scale, 1f)
        GlStateManager.translate(-hud.x, -hud.y, 0f)
    }

    private val isHovered get() = MouseUtils.isAreaHovered((hud.x - 2f) * 2, (hud.y - 3f) * 2, (hud.width + 4f) * 2 * hud.scale, (hud.height + 2f) * 2 * hud.scale)

    fun handleScroll(amount: Int): Boolean {
        if (isHovered) {
            hud.scale += amount / 100f
            hud.scale = hud.scale.coerceIn(0.6f, 6f)
            return true
        }
        return false
    }

    fun mouseReleased(state: Int) {
        if (state == 0) dragging = false
    }

    fun mouseClicked(mouseButton: Int): Boolean {
        if (isHovered) {
            if (mouseButton == 0) {
                x2 = hud.x - MouseUtils.mouseX / 2
                y2 = hud.y - MouseUtils.mouseY / 2
                dragging = true
                return true
            }
        }
        return false
    }

    fun onClose() {
        dragging = false
    }
}