package me.odinclient.clickgui

import cc.polyfrost.oneconfig.utils.dsl.nanoVG
import me.odinclient.clickgui.elements.menu.ElementColor
import me.odinclient.OdinClient.Companion.moduleConfig
import me.odinclient.features.Category
import me.odinclient.clickgui.util.MouseUtils.scaledMouseX
import me.odinclient.clickgui.util.MouseUtils.scaledMouseY
import me.odinclient.features.general.ClickGui
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11
import java.io.IOException

class ClickGUI : GuiScreen() {
    var scale = 2.0

    private var openedTime = System.currentTimeMillis()

    init {
        setUpPanels()
    }

    private fun setUpPanels() {
        panels = ArrayList()
        for (category in Category.values()) {
            panels.add(Panel(category, this))

        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val scaledResolution = ScaledResolution(mc)
        val prevScale = mc.gameSettings.guiScale

        scale = CLICK_GUI_SCALE / scaledResolution.scaleFactor
        mc.gameSettings.guiScale = 2
        GL11.glScaled(scale, scale, scale)

        nanoVG {
            for (p in panels) {
                p.drawScreen(partialTicks, this)
            }
        }

        mc.gameSettings.guiScale = prevScale
    }

    @Throws(IOException::class)
    override fun handleMouseInput() {
        super.handleMouseInput()

        var amount = Mouse.getEventDWheel().coerceIn(-1..1)
        if (isShiftKeyDown()) amount *= 7

        for (panel in panels.reversed()) {
            if (panel.initializeScroll(amount)) return
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        for (panel in panels.reversed()) {
            if (panel.mouseClicked(mouseButton)) return
        }

        try {
            super.mouseClicked(scaledMouseX, scaledMouseY, mouseButton)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        for (panel in panels.reversed()) {
            panel.mouseReleased(state)
        }

        super.mouseReleased(scaledMouseX, scaledMouseY, state)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        for (panel in panels.reversed()) {
            if (panel.keyTyped(typedChar, keyCode)) return
        }

        if (keyCode == ClickGui.keyCode && System.currentTimeMillis() - openedTime > 350) {
            mc.displayGuiScreen(null as GuiScreen?)
            if (mc.currentScreen == null) {
                mc.setIngameFocus()
            }
            return
        }

        try {
            super.keyTyped(typedChar, keyCode)
        } catch (e2: IOException) {
            e2.printStackTrace()
        }
    }

    override fun initGui() {
        openedTime = System.currentTimeMillis()

        if (OpenGlHelper.shadersSupported && mc.renderViewEntity is EntityPlayer && ClickGui.blur) {
            mc.entityRenderer.stopUseShader()
            mc.entityRenderer.loadShader(ResourceLocation("shaders/post/blur.json"))
        }

        for (panel in panels) {
            panel.x = ClickGui.panelX[panel.category]!!.value.toInt()
            panel.y = ClickGui.panelY[panel.category]!!.value.toInt()
            panel.extended = ClickGui.panelExtended[panel.category]!!.enabled
        }
    }

    override fun onGuiClosed() {
        mc.entityRenderer.stopUseShader()

        for (panel in panels.reversed()) {
            if (panel.extended) {
                if (panel.scrollAmount != 0)
                    panel.scrollOffset -= panel.scrollAmount

                for (moduleButton in panel.moduleButtons) {
                    if (moduleButton.extended) {
                        for (element in moduleButton.menuElements) {
                            if (element is ElementColor) {
                                element.dragging = null
                            }
                            element.listening = false
                        }
                    }
                }
            }
        }
        moduleConfig.saveConfig()
    }

    companion object {
        const val CLICK_GUI_SCALE = 2.0
        var panels: ArrayList<Panel> = arrayListOf()
    }

    override fun doesGuiPauseGame(): Boolean = false
}