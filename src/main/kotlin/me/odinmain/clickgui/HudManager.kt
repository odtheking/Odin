package me.odinmain.clickgui

import me.odinmain.clickgui.settings.impl.HudElement
import me.odinmain.config.Config
import me.odinmain.features.ModuleManager.hudSettingsCache
import me.odinmain.utils.render.Colors
import me.odinmain.utils.ui.Screen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.input.Keyboard
import kotlin.math.sign
import me.odinmain.utils.ui.mouseX as odinMouseX
import me.odinmain.utils.ui.mouseY as odinMouseY

object HudManager : Screen() {

    private var dragging: HudElement? = null

    private var deltaX = 0f
    private var deltaY = 0f

    override fun draw() {
        dragging?.let {
            it.x = (odinMouseX + deltaX).coerceIn(0f, mc.displayWidth - (it.width * it.scale))
            it.y = (odinMouseY + deltaY).coerceIn(0f, mc.displayHeight - (it.height * it.scale))
        }

        GlStateManager.pushMatrix()
        val sr = ScaledResolution(mc)
        GlStateManager.scale(1f / sr.scaleFactor, 1f / sr.scaleFactor, 1f)

        for (hudSetting in hudSettingsCache) {
            if (hudSetting.isEnabled) hudSetting.value.draw(true)
            if (!hudSetting.value.isHovered()) continue
            GlStateManager.pushMatrix()
            GlStateManager.translate(hudSetting.value.x + hudSetting.value.width * hudSetting.value.scale + 10f, hudSetting.value.y, 1f)
            GlStateManager.scale(1.5f, 1.5f, 1f)
            mc.fontRendererObj.drawString(hudSetting.name, 0, 0, Colors.WHITE.rgba)
            mc.fontRendererObj.drawSplitString(hudSetting.description, 0, 20, 150, Colors.WHITE.rgba)
            GlStateManager.popMatrix()
        }
        GlStateManager.popMatrix()
    }

    override fun onScroll(amount: Int) {
        val actualAmount = amount.sign.toFloat() * 0.2f
        for (hud in hudSettingsCache) {
            if (hud.isEnabled && hud.value.isHovered())
                hud.value.scale = (hud.value.scale + actualAmount).coerceIn(1f, 10f)
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        for (hud in hudSettingsCache) {
            if (hud.isEnabled && hud.value.isHovered()) {
                dragging = hud.value

                deltaX = (hud.value.x - odinMouseX)
                deltaY = (hud.value.y - odinMouseY)
            }
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        dragging = null
        return super.mouseReleased(mouseX, mouseY, state)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        when (keyCode) {
            Keyboard.KEY_EQUALS -> {
                for (hud in hudSettingsCache) {
                    if (hud.isEnabled && hud.value.isHovered()) hud.value.scale = (hud.value.scale + 0.2f).coerceIn(1f, 10f)
                }
            }
            Keyboard.KEY_MINUS -> {
                for (hud in hudSettingsCache) {
                    if (hud.isEnabled && hud.value.isHovered()) hud.value.scale = (hud.value.scale - 0.2f).coerceIn(1f, 10f)
                }
            }
            Keyboard.KEY_RIGHT -> {
                for (hud in hudSettingsCache) {
                    if (hud.isEnabled && hud.value.isHovered()) hud.value.x += 10f
                }
            }
            Keyboard.KEY_LEFT -> {
                for (hud in hudSettingsCache) {
                    if (hud.isEnabled && hud.value.isHovered()) hud.value.x -= 10f
                }
            }
            Keyboard.KEY_UP -> {
                for (hud in hudSettingsCache) {
                    if (hud.isEnabled && hud.value.isHovered()) hud.value.y -= 10f
                }
            }
            Keyboard.KEY_DOWN -> {
                for (hud in hudSettingsCache) {
                    if (hud.isEnabled && hud.value.isHovered()) hud.value.y += 10f
                }
            }
        }
        super.keyTyped(typedChar, keyCode)
    }

    override fun onGuiClosed() {
        Config.save()
        super.onGuiClosed()
    }

    fun resetHUDS() {
        hudSettingsCache.forEach {
            it.value.x = 10f
            it.value.y = 10f
            it.value.scale = 2f
        }
    }
}