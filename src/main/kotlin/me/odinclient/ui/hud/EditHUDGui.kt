package me.odinclient.ui.hud

import me.odinclient.config.Config
import me.odinclient.features.ModuleManager.hud
import me.odinclient.ui.Screen
import me.odinclient.utils.render.Color
import me.odinclient.utils.render.gui.GuiUtils.scaleFactor
import me.odinclient.utils.render.gui.GuiUtils.scaledHeight
import me.odinclient.utils.render.gui.GuiUtils.scaledWidth
import me.odinclient.utils.render.gui.MouseUtils
import me.odinclient.utils.render.gui.animations.Animation
import me.odinclient.utils.render.gui.animations.impl.EaseInOut
import me.odinclient.utils.render.gui.nvg.*
import kotlin.math.sign

object EditHUDGui : Screen() {

    var dragging: HudElement? = null

    private var startX: Float = 0f
    private var startY: Float = 0f

    private val openAnim = EaseInOut(600)
    private val resetAnim = EaseInOut(750)

    override fun draw(nvg: NVG) {
        dragging?.let {
            it.x = MouseUtils.mouseX - startX
            it.y = MouseUtils.mouseY - startY
        }

        drawNVG {
            translate(scaledWidth.toFloat(), scaledHeight * 2f - 100f)
            if (openAnim.isAnimating()) {
                setAlpha(openAnim.get(0f, 1f))
                val animVal = openAnim.get(0f, 1f)
                scale(animVal, animVal)
            }

            rect(-75f, 0f, 150f, 50f, Color.WHITE) // make this good
            resetTransform()

            for (i in 0 until hud.size) {
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

    override fun initGui() {
        openAnim.start()
    }

    override fun onGuiClosed() {
        for (i in 0 until hud.size) {
            hud[i].hoverHandler.reset()
        }
        Config.saveConfig()
    }
}