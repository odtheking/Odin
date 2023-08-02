package me.odinclient.ui.hud

import me.odinclient.config.Config
import me.odinclient.features.ModuleManager.hud
import me.odinclient.utils.render.gui.MouseUtils
import me.odinclient.utils.render.gui.nvg.drawNVG
import net.minecraft.client.gui.GuiScreen
import org.lwjgl.input.Mouse
import java.io.IOException
import kotlin.math.sign

object ExampleHudGui : GuiScreen() {

    var dragging: HudElement? = null
    var startX: Float = 0f
    var startY: Float = 0f

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawNVG {
            for (i in 0 until hud.size) {
                hud[i].draw(this, example = true)
            }
        }

        dragging?.let {
            it.x = MouseUtils.mouseX - startX
            it.y = MouseUtils.mouseY - startY
        }
    }

    @Throws(IOException::class)
    override fun handleMouseInput() {
        super.handleMouseInput()
        if (Mouse.getEventDWheel() != 0) {
            for (i in hud.size - 1 downTo 0) {
                if (hud[i].accept()) hud[i].scale += Mouse.getEventDWheel().sign * 0.05f
            }
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        for (i in hud.size - 1 downTo 0) {
            if (hud[i].accept()) {
                dragging = hud[i]
                startX = MouseUtils.mouseX - hud[i].x
                startY = MouseUtils.mouseY - hud[i].y
                return
            }
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        dragging = null
        super.mouseReleased(mouseX, mouseY, state)
    }

    override fun onGuiClosed() {
        Config.saveConfig()
    }

    override fun doesGuiPauseGame(): Boolean = false
}