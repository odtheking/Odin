package me.odinmain.ui.hud

import me.odinmain.config.Config
import me.odinmain.features.ModuleManager.huds
import me.odinmain.ui.Screen
import me.odinmain.ui.clickgui.util.ColorUtil.textColor
import me.odinmain.ui.clickgui.util.HoverHandler
import me.odinmain.utils.clock.Executor
import me.odinmain.utils.clock.Executor.Companion.register
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.gui.GuiUtils.scaledHeight
import me.odinmain.utils.render.gui.GuiUtils.scaledWidth
import me.odinmain.utils.render.gui.MouseUtils
import me.odinmain.utils.render.gui.MouseUtils.isAreaHovered
import me.odinmain.utils.render.gui.animations.impl.EaseInOut
import me.odinmain.utils.render.gui.nvg.*
import kotlin.math.sign

/**
 * Screen that renders all your active Hud's
 *
 * @author Stivais
 */
object EditHUDGui : Screen() {

    var dragging: HudElement? = null

    private var startX: Float = 0f
    private var startY: Float = 0f

    var open = false

    private val openAnim = EaseInOut(600)
    private val resetAnim = EaseInOut(1000)

    private val hoverHandler = HoverHandler(150) // for reset button

    /** Code is horrible ngl but it looks nice */
    override fun draw(nvg: NVG) {
        mc.mcProfiler.startSection("Odin Example Hud")
        dragging?.let {
            it.x = MouseUtils.mouseX - startX
            it.y = MouseUtils.mouseY - startY
        }

        drawNVG {
            translate(scaledWidth.toFloat(), scaledHeight * 1.75f)

            if (openAnim.isAnimating()) {
                setAlpha(openAnim.get(0f, 1f, !open))
                val animVal = openAnim.get(0f, 1f, !open)
                scale(animVal, animVal)
            }

            hoverHandler.handle(scaledWidth - 100f, (scaledHeight * 1.75f) - 25f, 200f, 50f)

            dropShadow(-100f, -25f, 200f, 50f, 10f, 1f, 9f)
            rect(-100f, -25f, 200f, 50f, color, 9f)

            text("Reset", 0f, 0f, textColor, 38f, Fonts.REGULAR, TextAlign.Middle)
            //rect(-75f, -25f, 150f, 50f, Color.WHITE) // make this good
            resetTransform()

            if (!open) return@drawNVG
            for (i in 0 until huds.size) {
                huds[i].draw(this, example = true)
            }
        }
        mc.mcProfiler.endSection()
    }

    private val color = Color(0f, 0.75f, 0.75f,0.75f)
        get() {
            field.brightness = (0.75f + hoverHandler.percent() / 500f).coerceAtMost(1f)
            return field
        }

    override fun onScroll(amount: Int) {
        for (i in huds.size - 1 downTo 0) {
            if (huds[i].accept()) {
                huds[i].scale += amount.sign * 0.05f
            }
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        if (isAreaHovered(scaledWidth - 100f, (scaledHeight * 1.75f) - 25f, 200f, 50f)) {
            resetHUDs()
            return
        }

        for (i in huds.size - 1 downTo 0) {
            if (huds[i].accept()) {
                dragging = huds[i]
                huds[i].anim2.start()

                startX = MouseUtils.mouseX - huds[i].x
                startY = MouseUtils.mouseY - huds[i].y
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
        open = true
    }

    override fun onGuiClosed() {
        open = false
        openAnim.start(true)

        Executor(0) {
            if (!openAnim.isAnimating()) destroyExecutor()
            drawScreen(0, 0, 0f)
        }.register()

        for (i in 0 until huds.size) {
            huds[i].hoverHandler.reset()
        }
        Config.saveConfig()
    }

    /**
     * Creates an Executor that slowly resets hud's position
     */
    fun resetHUDs() {
        if (resetAnim.start()) {
            for (i in huds) {
                i.resetX = i.x
                i.resetY = i.y
                i.resetScale = i.scale
            }
            Executor(0) {
                if (!resetAnim.isAnimating()) {
                    Config.saveConfig()
                    destroyExecutor()
                }
                for (hud in huds) {
                    hud.x = resetAnim.get(hud.resetX, hud.xSetting.default)
                    hud.y = resetAnim.get(hud.resetY, hud.ySetting.default)
                    hud.scale = resetAnim.get(hud.resetScale, hud.scaleSetting.default)
                }
            }.register()
        }
    }
}