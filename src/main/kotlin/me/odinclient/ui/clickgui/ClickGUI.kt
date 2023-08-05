package me.odinclient.ui.clickgui

import me.odinclient.config.Config
import me.odinclient.features.Category
import me.odinclient.features.impl.general.ClickGUIModule
import me.odinclient.ui.clickgui.elements.menu.ElementColor
import me.odinclient.utils.render.gui.animations.impl.EaseInOut
import me.odinclient.utils.render.gui.nvg.NVG
import me.odinclient.utils.render.gui.nvg.drawNVG
import me.odinclient.utils.render.gui.nvg.setAlpha
import me.odinclient.utils.render.gui.nvg.translate
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Mouse
import java.io.IOException
import kotlin.math.floor
import kotlin.math.sign

object ClickGUI : GuiScreen() {
    var anim = EaseInOut(400)
    private var openedTime = System.currentTimeMillis()

    private var panels: ArrayList<Panel> = arrayListOf()
    var descriptionToRender: (NVG.() -> Unit)? = null

    fun init() {
        for (category in Category.entries) {
            panels.add(Panel(category))
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawNVG {
            if (anim.isAnimating()) {
                translate(0f, floor(anim.get(-10f, 0f)))
                setAlpha(anim.get(0f, 1f))
            }

            for (i in 0 until panels.size) {
                panels[i].draw(this)
            }

            if (descriptionToRender != null) descriptionToRender?.invoke(this)
            descriptionToRender = null
        }
    }

    override fun handleMouseInput() {
        super.handleMouseInput()
        if (Mouse.getEventDWheel() != 0) {
            val amount = Mouse.getEventDWheel().sign * 16
            panels.forEach {
                if (it.handleScroll(amount)) return@forEach
            }
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        for (i in panels.size - 1 downTo 0) {
            if (panels[i].mouseClicked(mouseButton)) return
        }

        try {
            super.mouseClicked(mouseX, mouseY, mouseButton)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        for (i in panels.size - 1 downTo 0) {
            panels[i].mouseReleased(state)
        }
        super.mouseReleased(mouseX, mouseY, state)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        for (i in panels.size - 1 downTo 0) {
            if (panels[i].keyTyped(typedChar, keyCode)) return
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
        anim.start(true)

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
        Config.saveConfig()
    }

    override fun doesGuiPauseGame(): Boolean = false
}