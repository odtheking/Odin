package me.odinclient.ui.hud

import me.odinclient.config.Config
import me.odinclient.features.ModuleManager.hud
import me.odinclient.ui.Screen
import me.odinclient.utils.render.gui.MouseUtils
import me.odinclient.utils.render.gui.nvg.NVG
import me.odinclient.utils.render.gui.nvg.drawNVG
import kotlin.math.sign

object ExampleHudGui : Screen() {

    var dragging: HudElement? = null

    var startX: Float = 0f
    var startY: Float = 0f

    override fun draw(nvg: NVG) {
        dragging?.let {
            it.x = MouseUtils.mouseX - startX
            it.y = MouseUtils.mouseY - startY
        }

        drawNVG {
            for (i in hud.size - 1 downTo 0) {
                hud[i].draw(this, example = true)
            }
        }
    }

    override fun onScroll(amount: Int) {
        for (i in hud.size - 1 downTo 0) {
            if (hud[i].accept()) {
                hud[i].scale += amount.sign * 0.05f
            }
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        for (i in hud.size - 1 downTo 0) {
            if (hud[i].accept()) {
                dragging = hud[i]
                hud[i].anim2.start()

                startX = MouseUtils.mouseX - hud[i].x
                startY = MouseUtils.mouseY - hud[i].y
                return
            }
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        dragging?.anim2?.start(true)
        dragging = null
        super.mouseReleased(mouseX, mouseY, state)
    }

    override fun onGuiClosed() {
        Config.saveConfig()
    }
}