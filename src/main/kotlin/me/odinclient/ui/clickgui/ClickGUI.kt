package me.odinclient.ui.clickgui

import cc.polyfrost.oneconfig.utils.dsl.nanoVG
import cc.polyfrost.oneconfig.utils.dsl.setAlpha
import cc.polyfrost.oneconfig.utils.dsl.translate
import me.odinclient.OdinClient.Companion.moduleConfig
import me.odinclient.ui.clickgui.elements.menu.ElementColor
import me.odinclient.features.Category
import me.odinclient.features.impl.general.ClickGUIModule
import me.odinclient.utils.render.gui.animations.impl.EaseInOut
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Mouse
import java.io.IOException
import kotlin.math.floor

object ClickGUI : GuiScreen() {
    private val openingAnimation = EaseInOut(200)
    private var openedTime = System.currentTimeMillis()

    var panels: ArrayList<Panel> = arrayListOf()

    fun init() {
        for (category in Category.values()) {
            panels.add(Panel(category))
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        nanoVG {
            if (openingAnimation.isAnimating()) {
                translate(0f, floor(openingAnimation.get(-10f, 0f)))
                setAlpha(openingAnimation.get(0f, 1f))
            }

            for (p in panels) {
                p.drawScreen(this)
            }
        }
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
            super.mouseClicked(mouseX, mouseY, mouseButton)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        for (panel in panels.reversed()) {
            panel.mouseReleased(state)
        }

        super.mouseReleased(mouseX, mouseY, state)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        for (panel in panels.reversed()) {
            if (panel.keyTyped(typedChar, keyCode)) return
        }

        if (keyCode == ClickGUIModule.keyCode && System.currentTimeMillis() - openedTime > 350) {
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
        openingAnimation.start(true)

        // TODO: use a different blur
        if (OpenGlHelper.shadersSupported && mc.renderViewEntity is EntityPlayer && ClickGUIModule.blur) {
            mc.entityRenderer.stopUseShader()
            mc.entityRenderer.loadShader(ResourceLocation("shaders/post/blur.json"))
        }

        for (panel in panels) {
            panel.x = ClickGUIModule.panelX[panel.category]!!.value
            panel.y = ClickGUIModule.panelY[panel.category]!!.value
            panel.extended = ClickGUIModule.panelExtended[panel.category]!!.enabled
        }
    }

    override fun onGuiClosed() {
        mc.entityRenderer.stopUseShader()

        for (panel in panels.reversed()) {
            if (panel.extended) {
                if (panel.scrollAmount != 0f)
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

    override fun doesGuiPauseGame(): Boolean = false
}