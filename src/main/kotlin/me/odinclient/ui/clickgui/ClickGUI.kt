package me.odinclient.ui.clickgui

import cc.polyfrost.oneconfig.renderer.font.Fonts
import me.odinclient.OdinClient.Companion.display
import me.odinclient.config.Config
import me.odinclient.features.Category
import me.odinclient.features.impl.render.ClickGUIModule
import me.odinclient.ui.Screen
import me.odinclient.ui.clickgui.elements.menu.ElementColor
import me.odinclient.ui.clickgui.util.ColorUtil.buttonColor
import me.odinclient.ui.clickgui.util.ColorUtil.textColor
import me.odinclient.ui.clickgui.util.ColorUtil.withAlpha
import me.odinclient.ui.clickgui.util.HoverHandler
import me.odinclient.utils.clock.Executor
import me.odinclient.utils.clock.Executor.Companion.register
import me.odinclient.utils.render.gui.animations.impl.EaseInOut
import me.odinclient.utils.render.gui.nvg.*
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Mouse
import kotlin.math.floor
import kotlin.math.sign

/**
 * Renders all the modules.
 *
 * Backend made by Aton, with some changes
 * Design mostly made by Stivais
 *
 * @author Stivais, Aton
 * @see [Panel]
 */
object ClickGUI : Screen() {

    private val panels: ArrayList<Panel> = arrayListOf()

    var anim = EaseInOut(400)
    private var open = false
    private var desc: Description = Description(null, 0f, 0f, null)

    fun init() {
        for (category in Category.entries) {
            panels.add(Panel(category))
        }
    }

    override fun draw(nvg: NVG) {
        nvg {
            if (anim.isAnimating()) {
                translate(0f, floor(anim.get(-10f, 0f, !open)))
                setAlpha(anim.get(0f, 1f, !open))
            }

            for (i in 0 until panels.size) {
                panels[i].draw(this)
            }

            desc.render(this)
        }
    }

    override fun onScroll(amount: Int) {
        if (Mouse.getEventDWheel() != 0) {
            val actualAmount = amount.sign * 16
            for (i in panels.size - 1 downTo 0) {
                if (panels[i].handleScroll(actualAmount)) return
            }
        }
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        for (i in panels.size - 1 downTo 0) {
            if (panels[i].mouseClicked(mouseButton)) return
        }
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        for (i in panels.size - 1 downTo 0) {
            panels[i].mouseReleased(state)
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        for (i in panels.size - 1 downTo 0) {
            if (panels[i].keyTyped(typedChar, keyCode)) return
        }

        if (keyCode == ClickGUIModule.keyCode && !anim.isAnimating()) {
            mc.displayGuiScreen(null as GuiScreen?)
            if (mc.currentScreen == null) {
                mc.setIngameFocus()
            }
        }
        super.keyTyped(typedChar, keyCode)
    }

    override fun initGui() {
        open = true
        anim.start(true)

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

        open = false
        anim.start(true)
        mc.entityRenderer.stopUseShader()

        /** Used to render the closing animation */
        Executor(0) {
            if (!anim.isAnimating()) destroyExecutor()
            drawScreen(0, 0, 0f)
        }.register()
    }

    /**
     * Used to smooth transition between screens.
     */
    fun swapScreens(other: Screen) {
        // TODO: ACTUALLY MAKLE THIS WORK
        display = other
    }

    /** Sets the description without creating a new data class which isn't optimal */
    fun setDescription(text: String, x: Float,  y: Float, hoverHandler: HoverHandler) {
        desc.text = text
        desc.x = x
        desc.y = y
        desc.hoverHandler = hoverHandler
    }

    /**
     * Used to render Descriptions
     * @see draw
     */
    data class Description(var text: String?, var x: Float, var y: Float, var hoverHandler: HoverHandler?) {

        /** Test whether a description is active or not */
        private val shouldRender: Boolean
            get() = text != null && hoverHandler != null && text != ""

        /** Handles rendering, if it's not active then it won't render */
        fun render(nvg: NVG) {
            if (shouldRender) {
                nvg {
                    val area = wrappedTextBounds(text!!, 300f, 16f, Fonts.REGULAR)
                    rect(
                        x, y, area[2] - area[0] + 10, area[3] - area[1] + 8,
                        buttonColor.withAlpha((hoverHandler!!.percent() / 100f).coerceIn(0f, 0.8f)), 5f
                    )
                    wrappedText(text!!, x + 7f, y + 12f, 300f, 1f, textColor, 16f, Fonts.REGULAR)
                    if (hoverHandler!!.percent() == 0) {
                        text = null
                        hoverHandler = null
                    }
                }
            }
        }
    }
}