package com.odtheking.odin.clickgui

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.features.Category
import com.odtheking.odin.features.ModuleManager
import com.odtheking.odin.features.impl.render.ClickGUIModule
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.ui.HoverHandler
import com.odtheking.odin.utils.ui.animations.EaseOutAnimation
import com.odtheking.odin.utils.ui.rendering.NVGPIPRenderer
import com.odtheking.odin.utils.ui.rendering.NVGRenderer
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import kotlin.math.sign
import com.odtheking.odin.utils.ui.mouseX as odinMouseX
import com.odtheking.odin.utils.ui.mouseY as odinMouseY

/**
 * Renders all the modules.
 */
object ClickGUI : Screen(Component.literal("Click GUI")) {

    private val panels: ArrayList<Panel> = arrayListOf<Panel>().apply {
        if (Category.categories.any { (category, _) -> ClickGUIModule.panelSetting[category] == null }) ClickGUIModule.resetPositions()
        for ((_, category) in Category.categories) add(Panel(category))
    }

    private var openAnim = EaseOutAnimation(500)
    val gray38 = Color(38, 38, 38)
    val gray26 = Color(26, 26, 26)

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        NVGPIPRenderer.draw(context, 0, 0, context.guiWidth(), context.guiHeight()) {
            val scaledMouseX = odinMouseX / ClickGUIModule.getStandardGuiScale()
            val scaledMouseY = odinMouseY / ClickGUIModule.getStandardGuiScale()

            NVGRenderer.scale(ClickGUIModule.getStandardGuiScale(), ClickGUIModule.getStandardGuiScale())

            SearchBar.draw(
                mc.window.screenWidth / (2f * ClickGUIModule.getStandardGuiScale()) - 175f,
                (mc.window.screenHeight - 110f) / ClickGUIModule.getStandardGuiScale() - 20f,
                scaledMouseX,
                scaledMouseY
            )

            if (openAnim.isAnimating()) {
                val scale = openAnim.get(0f, 1f)

                val centerX = context.guiWidth().toFloat()
                val centerY = context.guiHeight().toFloat()
                NVGRenderer.translate(centerX, centerY)
                NVGRenderer.scale(scale, scale)
                NVGRenderer.translate(-centerX, -centerY)
            }

            val draggedPanel = panels.firstOrNull { it.dragging }
            for (panel in panels) {
                if (panel != draggedPanel) panel.draw(scaledMouseX, scaledMouseY)
            }

            draggedPanel?.draw(scaledMouseX, scaledMouseY)

            desc.render()
        }
        super.render(context, mouseX, mouseY, deltaTicks)
    }

    override fun mouseScrolled(
        mouseX: Double,
        mouseY: Double,
        horizontalAmount: Double,
        verticalAmount: Double
    ): Boolean {
        val actualAmount = (verticalAmount.sign * 16).toInt()
        for (i in panels.size - 1 downTo 0) {
            if (panels[i].handleScroll(actualAmount)) return true
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    override fun mouseClicked(
        mouseButtonEvent: MouseButtonEvent,
        bl: Boolean
    ): Boolean {
        val scaledMouseX = odinMouseX / ClickGUIModule.getStandardGuiScale()
        val scaledMouseY = odinMouseY / ClickGUIModule.getStandardGuiScale()
        SearchBar.mouseClicked(scaledMouseX, scaledMouseY, mouseButtonEvent)
        for (i in panels.size - 1 downTo 0) {
            if (panels[i].mouseClicked(scaledMouseX, scaledMouseY, mouseButtonEvent)) return true
        }
        return super.mouseClicked(mouseButtonEvent, bl)
    }

    override fun mouseReleased(mouseButtonEvent: MouseButtonEvent): Boolean {
        SearchBar.mouseReleased()
        for (i in panels.size - 1 downTo 0) {
            panels[i].mouseReleased(mouseButtonEvent)
        }
        return super.mouseReleased(mouseButtonEvent)
    }

    override fun charTyped(characterEvent: CharacterEvent): Boolean {
        SearchBar.keyTyped(characterEvent)
        for (i in panels.size - 1 downTo 0) {
            if (panels[i].keyTyped(characterEvent)) return true
        }
        return super.charTyped(characterEvent)
    }

    override fun keyPressed(keyEvent: KeyEvent): Boolean {
        SearchBar.keyPressed(keyEvent)
        for (i in panels.size - 1 downTo 0) {
            if (panels[i].keyPressed(keyEvent)) return true
        }
        return super.keyPressed(keyEvent)
    }

    override fun init() {
        openAnim.start()
        super.init()
    }

    override fun onClose() {
        for (panel in panels.filter { it.panelSetting.extended }.reversed()) {
            for (moduleButton in panel.moduleButtons.filter { it.extended }) {
                for (setting in moduleButton.representableSettings) {
                    if (setting is ColorSetting) setting.section = null
                    setting.listening = false
                }
            }
        }

        ModuleManager.saveConfigurations()
        super.onClose()
    }

    override fun isPauseScreen(): Boolean = false

    private var desc = Description("", 0f, 0f, HoverHandler(150))

    /** Sets the description without creating a new data class which isn't optimal */
    fun setDescription(text: String, x: Float, y: Float, hoverHandler: HoverHandler) {
        desc.text = text
        desc.x = x
        desc.y = y
        desc.hoverHandler = hoverHandler
    }

    data class Description(var text: String, var x: Float, var y: Float, var hoverHandler: HoverHandler) {

        fun render() {
            if (text.isEmpty() || hoverHandler.percent() < 100) return
            val area = NVGRenderer.wrappedTextBounds(text, 300f, 16f, NVGRenderer.defaultFont)
            NVGRenderer.rect(x, y, area[2] - area[0] + 16f, area[3] - area[1] + 16f, gray38.rgba, 5f)
            NVGRenderer.hollowRect(
                x,
                y,
                area[2] - area[0] + 16f,
                area[3] - area[1] + 16f,
                1.5f,
                ClickGUIModule.clickGUIColor.rgba,
                5f
            )
            NVGRenderer.drawWrappedString(text, x + 8f, y + 8f, 300f, 16f, Colors.WHITE.rgba, NVGRenderer.defaultFont)
        }
    }

    val movementImage = NVGRenderer.createImage("/assets/odin/MovementIcon.svg")
    val hueImage = NVGRenderer.createImage("/assets/odin/HueGradient.png")
    val chevronImage = NVGRenderer.createImage("/assets/odin/chevron.svg")
}