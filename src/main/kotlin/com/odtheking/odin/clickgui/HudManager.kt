package com.odtheking.odin.clickgui

import com.mojang.blaze3d.platform.InputConstants
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.settings.impl.HudElement
import com.odtheking.odin.features.ModuleManager.hudSettingsCache
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.ui.compose.UiHost
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.input.KeyEvent
import net.minecraft.network.chat.Component

object HudManager : OdinScreen(Component.literal("HUD Manager")) {

    override val ui: UiHost get() = HudLayer.ui

    override fun init() {
        super.init()
        HudLayer.settle()
        for (setting in hudSettingsCache) if (setting.isEnabled) setting.hud.clampToScreen()
    }

    override fun extractRenderState(guiGraphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, deltaTicks)

        HudLayer.render(guiGraphics, example = true, mouseX, mouseY)

        HudLayer.hovered(mouseX, mouseY)?.let { setting ->
            val element = setting.hud
            val labelX = element.x + element.scaledWidth + LABEL_GAP
            guiGraphics.text(font, setting.name, labelX, element.y, Colors.WHITE.rgba, true)
            guiGraphics.textWithWordWrap(
                font, Component.literal(setting.description),
                labelX, element.y + font.lineHeight + 1, LABEL_WIDTH, Colors.WHITE.rgba
            )
        }
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        val (mouseX, mouseY) = mousePosition()
        HudLayer.hovered(mouseX, mouseY)?.let { setting ->
            val element = setting.hud
            when (event.key) {
                InputConstants.KEY_EQUALS -> resize(element, KEY_STEP)
                InputConstants.KEY_MINUS -> resize(element, -KEY_STEP)
                InputConstants.KEY_RIGHT -> element.x += NUDGE
                InputConstants.KEY_LEFT -> element.x -= NUDGE
                InputConstants.KEY_UP -> element.y -= NUDGE
                InputConstants.KEY_DOWN -> element.y += NUDGE
                else -> return super.keyPressed(event)
            }
            element.clampToScreen()
            return true
        }
        return super.keyPressed(event)
    }

    fun resetHUDS() {
        for (setting in hudSettingsCache) {
            setting.hud.x = 10
            setting.hud.y = 10
            setting.hud.scale = 1f
        }
    }

    private fun resize(element: HudElement, amount: Float) {
        element.scale = (element.scale + amount).coerceIn(HudElement.MIN_SCALE, HudElement.MAX_SCALE)
        element.clampToScreen()
    }

    private fun mousePosition(): Pair<Int, Int> {
        val scale = mc.window.guiScale.coerceAtLeast(1)
        return (mc.mouseHandler.xpos() / scale).toInt() to (mc.mouseHandler.ypos() / scale).toInt()
    }

    private const val LABEL_GAP = 10
    private const val LABEL_WIDTH = 150
    private const val KEY_STEP = 0.1f
    private const val NUDGE = 5
}