package me.odinclient.features.impl.general

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
import me.odinclient.utils.render.gui.nvg.*

object CPSDisplay : Module(
    "CPS Display",
    category = Category.GENERAL
) {
    private val hud: HudElement by HudSetting("Display", 10f, 10f, 1f, false) {
        leftClicks.removeAll { System.currentTimeMillis() - it > 1000 }
        rightClicks.removeAll { System.currentTimeMillis() - it > 1000 }

        val value = if (button == 0) "${leftClicks.size}" else "${rightClicks.size}"
        val anim = if (button == 0) leftAnim else rightAnim

        rect(0f, 0f, 50f, 36f, color.brighter(anim.get(1f, 1.5f, anim.getPercent() >= 50)), 9f)
        dropShadow(0f, 0f, 50f, 36f, 10f, 1f, 9f)

        if (mouseText) {
            val text = if (button == 0) "LMB" else "RMB"
            text(text, 25f, 8.5f, textColor, 10f, Fonts.MEDIUM, TextAlign.Middle)
            text(value, 25f, 24.5f, textColor, 18.5f, Fonts.MEDIUM, TextAlign.Middle)
        } else {
            text(value, 25f, 19f, textColor, 24f, Fonts.MEDIUM, TextAlign.Middle)
        }
        50f to 38f
    }

    private val advanced: Boolean by BooleanSetting("Settings", false)

    private val button: Int by SelectorSetting("Button", "Both", arrayListOf("Left", "Right"))
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