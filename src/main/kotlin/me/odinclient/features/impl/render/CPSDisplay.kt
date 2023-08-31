package me.odinclient.features.impl.render

import cc.polyfrost.oneconfig.renderer.font.Fonts
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.Setting.Companion.withDependency
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.features.settings.impl.ColorSetting
import me.odinclient.features.settings.impl.HudSetting
import me.odinclient.features.settings.impl.SelectorSetting
import me.odinclient.ui.clickgui.util.ColorUtil.brighter
import me.odinclient.ui.hud.HudElement
import me.odinclient.utils.render.Color
import me.odinclient.utils.render.gui.animations.impl.EaseInOut
import me.odinclient.utils.render.gui.nvg.TextAlign
import me.odinclient.utils.render.gui.nvg.dropShadow
import me.odinclient.utils.render.gui.nvg.rect
import me.odinclient.utils.render.gui.nvg.text

object CPSDisplay : Module(
    "CPS Display",
    description = "Displays your CPS",
    category = Category.RENDER
) {
    private val hud: HudElement by HudSetting("Display", 10f, 10f, 2f, false) {
        leftClicks.removeAll { System.currentTimeMillis() - it > 1000 }
        rightClicks.removeAll { System.currentTimeMillis() - it > 1000 }

        val value = if (button == 0) "${leftClicks.size}" else "${rightClicks.size}"
        val anim = if (button == 0) leftAnim else rightAnim

        if (button == 2) {
            rect(0f, 0f, 50f, 36f, color.brighter(leftAnim.get(1f, 1.5f, leftAnim.getPercent() >= 50)), 9f, 0f, 9f, 0f)
            rect(50f, 0f, 50f, 36f, color.brighter(rightAnim.get(1f, 1.5f, rightAnim.getPercent() >= 50)), 0f, 9f, 0f, 9f)

            dropShadow(0f, 0f, 100f, 36f, 10f, 1f, 9f)
        } else {
            rect(0f, 0f, 50f, 36f, color.brighter(anim.get(1f, 1.5f, anim.getPercent() >= 50)), 9f)
            dropShadow(0f, 0f, 50f, 36f, 10f, 1f, 9f)
        }

        if (mouseText) {
            if (button == 2) {
                text("LMB", 25f, 8.5f, textColor, 10f, Fonts.MEDIUM, TextAlign.Middle)
                text(leftClicks.size.toString(), 25f, 24.5f, textColor, 18.5f, Fonts.MEDIUM, TextAlign.Middle)

                text("RMB", 75f, 8.5f, textColor, 10f, Fonts.MEDIUM, TextAlign.Middle)
                text(rightClicks.size.toString(), 75f, 24.5f, textColor, 18.5f, Fonts.MEDIUM, TextAlign.Middle)
            } else {
                val text = if (button == 0) "LMB" else "RMB"
                text(text, 25f, 8.5f, textColor, 10f, Fonts.MEDIUM, TextAlign.Middle)
                text(value, 25f, 24.5f, textColor, 18.5f, Fonts.MEDIUM, TextAlign.Middle)
            }
        } else {
            if (button == 2) {
                text(leftClicks.size.toString(), 25f, 19f, textColor, 24f, Fonts.MEDIUM, TextAlign.Middle)
                text(rightClicks.size.toString(), 75f, 19f, textColor, 24f, Fonts.MEDIUM, TextAlign.Middle)
            } else text(value, 25f, 19f, textColor, 24f, Fonts.MEDIUM, TextAlign.Middle)
        }
        if (button == 2) 100f to 38f else 50f to 38f
    }

    private val advanced: Boolean by BooleanSetting("Settings", false)

    private val button: Int by SelectorSetting("Button", "Both", arrayListOf("Left", "Right", "Both"))
        .withDependency { advanced }

    private val mouseText: Boolean by BooleanSetting("Show Button", true)
        .withDependency { advanced }

    private val color: Color by ColorSetting("Color", Color(21, 22, 23, 0.25f), allowAlpha = true)
        .withDependency { advanced }

    private val textColor: Color by ColorSetting("Text Color", Color(239, 239, 239, 1f), allowAlpha = true)
        .withDependency { advanced }

    private val leftAnim = EaseInOut(300)
    private val rightAnim = EaseInOut(300)

    private val leftClicks = mutableListOf<Long>()
    private val rightClicks = mutableListOf<Long>()

    fun onLeftClick() {
        leftClicks.add(System.currentTimeMillis())
        leftAnim.start(true)
    }

    fun onRightClick() {
        rightClicks.add(System.currentTimeMillis())
        rightAnim.start(true)
    }
}