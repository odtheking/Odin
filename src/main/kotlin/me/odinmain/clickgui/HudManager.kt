package me.odinmain.clickgui

import me.odinmain.clickgui.settings.impl.HudElement
import me.odinmain.config.Config
import me.odinmain.features.ModuleManager.hudSettingsCache
import me.odinmain.utils.ui.Screen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.Display
import kotlin.math.sign
import me.odinmain.utils.ui.mouseX as odinMouseX
import me.odinmain.utils.ui.mouseY as odinMouseY

object HudManager : Screen() {

    private var dragging: HudElement? = null

    private var startX = 0f
    private var startY = 0f

    override fun draw() {
        dragging?.let {
            it.x = (odinMouseX - startX).coerceIn(0f, Display.getWidth() - (it.width * it.scale))
            it.y = (odinMouseY - startY).coerceIn(0f, Display.getHeight() - (it.height * it.scale))
        }

        GlStateManager.pushMatrix()
        val sr = ScaledResolution(mc)
        GlStateManager.scale(1f / sr.scaleFactor, 1f / sr.scaleFactor, 1f)

        for (hudSettings in hudSettingsCache) {
            if (hudSettings.isEnabled) hudSettings.value.draw(true)
        }
        GlStateManager.popMatrix()
    }

    override fun onScroll(amount: Int) {
        val actualAmount = amount.sign.toFloat() * 0.2f
        for (hud in hudSettingsCache) {
            if (hud.isEnabled && hud.value.isHovered())
                hud.value.scale = (hud.value.scale + actualAmount).coerceIn(2f, 10f)
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        for (hud in hudSettingsCache) {
            if (hud.isEnabled && hud.value.isHovered()) {
                dragging = hud.value

                startX = (odinMouseX - hud.value.x)
                startY = (odinMouseY - hud.value.y)
            }
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        dragging = null
        return super.mouseReleased(mouseX, mouseY, state)
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