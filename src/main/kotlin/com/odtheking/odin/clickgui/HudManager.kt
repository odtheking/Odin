package com.odtheking.odin.clickgui

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.settings.impl.HudElement
import com.odtheking.odin.features.ModuleManager
import com.odtheking.odin.features.ModuleManager.hudSettingsCache
import com.odtheking.odin.utils.Colors
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW
import kotlin.math.roundToInt
import kotlin.math.sign
import com.odtheking.odin.utils.ui.mouseX as odinMouseX
import com.odtheking.odin.utils.ui.mouseY as odinMouseY

object HudManager : Screen(Component.literal("HUD Manager")) {

    private var dragging: HudElement? = null
    private var deltaX = 0f
    private var deltaY = 0f

    var gridEnabled = false
    var gridSize = 15
        private set

    override fun init() {
        for (hud in hudSettingsCache) {
            if (hud.isEnabled) {
                hud.value.x = hud.value.x.coerceIn(0, (mc.window.screenWidth - (hud.value.width * hud.value.scale)).toInt())
                hud.value.y = hud.value.y.coerceIn(0, (mc.window.screenHeight - (hud.value.height * hud.value.scale)).toInt())
            }
        }
        super.init()
    }

    private fun snapToGrid(value: Float): Int = (value / gridSize).roundToInt() * gridSize

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        super.render(guiGraphics, mouseX, mouseY, deltaTicks)

        dragging?.let {
            val rawX = (odinMouseX + deltaX).coerceIn(0f, (mc.window.screenWidth - (it.width * it.scale)))
            val rawY = (odinMouseY + deltaY).coerceIn(0f, (mc.window.screenHeight - (it.height * it.scale)))
            it.x = if (gridEnabled) snapToGrid(rawX) else rawX.toInt()
            it.y = if (gridEnabled) snapToGrid(rawY) else rawY.toInt()
        }

        guiGraphics.pose().pushMatrix()
        val sf = mc.window.guiScale
        guiGraphics.pose().scale(1f / sf, 1f / sf)

        if (gridEnabled) {
            val sw = mc.window.screenWidth
            val sh = mc.window.screenHeight
            val gridColor = 0x22FFFFFF.toInt()
            var gx = 0
            while (gx <= sw) {
                guiGraphics.fill(gx, 0, gx + 1, sh, gridColor)
                gx += gridSize
            }
            var gy = 0
            while (gy <= sh) {
                guiGraphics.fill(0, gy, sw, gy + 1, gridColor)
                gy += gridSize
            }
        }

        for (hud in hudSettingsCache) {
            if (hud.isEnabled) hud.value.draw(guiGraphics, true)
        }

        hudSettingsCache.firstOrNull { it.isEnabled && it.value.isHovered() }?.let { hoveredHud ->
            guiGraphics.pose().pushMatrix()
            guiGraphics.pose().translate(
                (hoveredHud.value.x + hoveredHud.value.width * hoveredHud.value.scale + 10f),
                hoveredHud.value.y.toFloat(),
            )
            guiGraphics.pose().scale(2f, 2f)
            guiGraphics.drawString(mc.font, hoveredHud.name, 0, 0, Colors.WHITE.rgba)
            guiGraphics.drawWordWrap(mc.font, Component.literal(hoveredHud.description), 0, 10, 150, Colors.WHITE.rgba)
            guiGraphics.pose().popMatrix()
        }

        guiGraphics.pose().popMatrix()
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        val actualAmount = verticalAmount.sign.toFloat() * 0.2f
        hudSettingsCache.firstOrNull { it.isEnabled && it.value.isHovered() }?.let { hovered ->
            hovered.value.scale = (hovered.value.scale + actualAmount).coerceIn(1f, 10f)
            return true
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    override fun mouseClicked(mouseButtonEvent: MouseButtonEvent, bl: Boolean): Boolean {
        hudSettingsCache.firstOrNull { it.isEnabled && it.value.isHovered() }?.let { hovered ->
            dragging = hovered.value
            deltaX = (hovered.value.x - odinMouseX)
            deltaY = (hovered.value.y - odinMouseY)
            return true
        }
        return super.mouseClicked(mouseButtonEvent, bl)
    }

    override fun mouseReleased(mouseButtonEvent: MouseButtonEvent): Boolean {
        dragging = null
        return super.mouseReleased(mouseButtonEvent)
    }

    override fun keyPressed(keyEvent: KeyEvent): Boolean {
        when (keyEvent.key) {
            GLFW.GLFW_KEY_G     -> gridEnabled = !gridEnabled
            GLFW.GLFW_KEY_EQUAL -> gridSize = (gridSize + 1).coerceIn(1, 50)
            GLFW.GLFW_KEY_MINUS -> gridSize = (gridSize - 1).coerceIn(1, 50)
        }

        hudSettingsCache.firstOrNull { it.isEnabled && it.value.isHovered() }?.let { hovered ->
            val el = hovered.value
            val sw = mc.window.screenWidth
            val sh = mc.window.screenHeight
            when (keyEvent.key) {
                GLFW.GLFW_KEY_RIGHT -> el.x += 1
                GLFW.GLFW_KEY_LEFT  -> el.x -= 1
                GLFW.GLFW_KEY_UP    -> el.y -= 1
                GLFW.GLFW_KEY_DOWN  -> el.y += 1
                GLFW.GLFW_KEY_H     -> el.x = ((sw - el.width * el.scale) / 2).toInt()
                GLFW.GLFW_KEY_V     -> el.y = ((sh - el.height * el.scale) / 2).toInt()
            }
        }

        return super.keyPressed(keyEvent)
    }

    override fun onClose() {
        ModuleManager.saveConfigurations()
        super.onClose()
    }

    fun resetHUDS() {
        hudSettingsCache.forEach {
            it.value.x = 10
            it.value.y = 10
            it.value.scale = 2f
        }
    }

    override fun isPauseScreen(): Boolean = false
}
