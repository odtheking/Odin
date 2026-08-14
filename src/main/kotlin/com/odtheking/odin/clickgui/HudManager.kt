package com.odtheking.odin.clickgui

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.settings.impl.HUDSetting
import com.odtheking.odin.clickgui.settings.impl.HudElement
import com.odtheking.odin.features.ModuleManager
import com.odtheking.odin.features.ModuleManager.hudSettingsCache
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.ui.animations.Animations
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW
import kotlin.math.sign

object HudManager : Screen(Component.literal("HUD Manager")) {

    private var dragging: HudElement? = null
    private var grabX = 0
    private var grabY = 0

    override fun init() {
        Animations.settle()
        for (setting in hudSettingsCache) {
            if (setting.isEnabled) setting.hud.clampToScreen()
        }
    }

    override fun extractRenderState(guiGraphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, deltaTicks)

        dragging?.let { element ->
            element.x = mouseX - grabX
            element.y = mouseY - grabY
            element.clampToScreen()
        }

        for (setting in hudSettingsCache) {
            if (setting.isEnabled) setting.hud.draw(guiGraphics, true, mouseX, mouseY)
        }

        hovered(mouseX, mouseY)?.let { setting ->
            val element = setting.hud
            val labelX = element.x + element.scaledWidth + LABEL_GAP
            guiGraphics.text(font, setting.name, labelX, element.y, Colors.WHITE.rgba, true)
            guiGraphics.textWithWordWrap(
                font, Component.literal(setting.description),
                labelX, element.y + font.lineHeight + 1, LABEL_WIDTH, Colors.WHITE.rgba
            )
        }
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        val hovered = hovered(mouseX.toInt(), mouseY.toInt()) ?: return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
        resize(hovered.hud, verticalAmount.sign.toFloat() * SCROLL_STEP)
        return true
    }

    override fun mouseClicked(mouseButtonEvent: MouseButtonEvent, bl: Boolean): Boolean {
        val hovered = hovered(mouseButtonEvent.x().toInt(), mouseButtonEvent.y().toInt())
            ?: return super.mouseClicked(mouseButtonEvent, bl)

        dragging = hovered.hud
        grabX = mouseButtonEvent.x().toInt() - hovered.hud.x
        grabY = mouseButtonEvent.y().toInt() - hovered.hud.y
        return true
    }

    override fun mouseReleased(mouseButtonEvent: MouseButtonEvent): Boolean {
        dragging = null
        return super.mouseReleased(mouseButtonEvent)
    }

    override fun keyPressed(keyEvent: KeyEvent): Boolean {
        val (mouseX, mouseY) = mousePosition()
        hovered(mouseX, mouseY)?.let { setting ->
            val element = setting.hud
            when (keyEvent.key) {
                GLFW.GLFW_KEY_EQUAL -> resize(element, KEY_STEP)
                GLFW.GLFW_KEY_MINUS -> resize(element, -KEY_STEP)
                GLFW.GLFW_KEY_RIGHT -> element.x += NUDGE
                GLFW.GLFW_KEY_LEFT -> element.x -= NUDGE
                GLFW.GLFW_KEY_UP -> element.y -= NUDGE
                GLFW.GLFW_KEY_DOWN -> element.y += NUDGE
                else -> return super.keyPressed(keyEvent)
            }
            element.clampToScreen()
            return true
        }
        return super.keyPressed(keyEvent)
    }

    override fun onClose() {
        dragging = null
        ModuleManager.saveConfigurations()
        super.onClose()
    }

    override fun isPauseScreen(): Boolean = false

    fun resetHUDS() {
        for (setting in hudSettingsCache) {
            setting.hud.x = 10
            setting.hud.y = 10
            setting.hud.scale = 1f
        }
    }

    private fun hovered(mouseX: Int, mouseY: Int): HUDSetting? =
        hudSettingsCache.firstOrNull { it.isEnabled && it.hud.isHovered(mouseX, mouseY) }

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
    private const val SCROLL_STEP = 0.1f
    private const val KEY_STEP = 0.1f
    private const val NUDGE = 5
}