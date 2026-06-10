package com.odtheking.odin.clickgui.vanilla

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.features.Category
import com.odtheking.odin.features.ModuleManager
import com.odtheking.odin.features.impl.render.ClickGUIModule
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.roundedFill
import com.odtheking.odin.utils.render.roundedOutline
import com.odtheking.odin.utils.ui.HoverHandler
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import kotlin.math.sign

object VanillaGUI : Screen(Component.literal("Click GUI")) {

    val gray38 = Color(38, 38, 38)
    val gray26 = Color(26, 26, 26)
    val white = Colors.WHITE

    private val panels: ArrayList<Panel> = arrayListOf<Panel>().apply {
        if (Category.categories.any { (category, _) -> ClickGUIModule.panelSetting[category] == null }) ClickGUIModule.resetPositions()
        for ((_, category) in Category.categories) add(Panel(category))
    }

    private var description = Description("", 0, 0, HoverHandler(150))

    override fun init() {
        panels.forEach {
            it.panelSetting.x = it.panelSetting.x.coerceIn(0, mc.window.width)
            it.panelSetting.y = it.panelSetting.y.coerceIn(0, mc.window.height)
        }
        super.init()
    }

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, deltaTicks: Float) {
        val (mx, my) = currentMousePosition()

        SearchBar.draw(context)

        val draggedPanel = panels.firstOrNull { it.dragging }
        for (panel in panels) {
            if (panel !== draggedPanel) panel.draw(context, mx, my)
        }
        draggedPanel?.draw(context, mx, my)

        drawDescription(context)
        super.render(context, mouseX, mouseY, deltaTicks)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, horizontalAmount: Double, verticalAmount: Double): Boolean {
        val amount = (verticalAmount.sign * 16).toInt()
        for (i in panels.size - 1 downTo 0) {
            if (panels[i].handleScroll(amount)) return true
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)
    }

    override fun mouseClicked(mouseButtonEvent: MouseButtonEvent, bl: Boolean): Boolean {
        val (mouseX, mouseY) = currentMousePosition()
        if (SearchBar.mouseClicked(mouseX, mouseY, mouseButtonEvent)) return true
        for (i in panels.size - 1 downTo 0) {
            if (panels[i].mouseClicked(mouseX, mouseY, mouseButtonEvent)) return true
        }
        return super.mouseClicked(mouseButtonEvent, bl)
    }

    override fun mouseReleased(mouseButtonEvent: MouseButtonEvent): Boolean {
        val (mouseX, mouseY) = currentMousePosition()
        SearchBar.mouseReleased()
        for (i in panels.size - 1 downTo 0) {
            panels[i].mouseReleased(mouseX, mouseY, mouseButtonEvent)
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

    override fun onClose() {
        for (panel in panels.filter { it.panelSetting.extended }.reversed()) {
            for (moduleButton in panel.moduleButtons.filter { it.extended }) {
                for (setting in moduleButton.representableSettings) {
                //    if (setting is ColorSetting) setting.section = null
                    setting.listening = false
                }
            }
        }

        ModuleManager.saveConfigurations()
        super.onClose()
    }

    override fun isPauseScreen(): Boolean = false

    fun setDescription(text: String, x: Int, y: Int, hoverHandler: HoverHandler) {
        description.text = text
        description.x = x
        description.y = y
        description.hoverHandler = hoverHandler
    }

    private fun drawDescription(context: GuiGraphics) {
        if (description.text.isEmpty() || description.hoverHandler.percent() < 100) return
        val text = Component.literal(description.text)

        val lines = font.split(text, DESCRIPTION_MAX_WIDTH)
        val width = lines.maxOfOrNull { mc.font.width(it) } ?: 0
        val height = lines.size * (mc.font.lineHeight + 1)

        val x = description.x
        val y = description.y
        val boxW = width + 16
        val boxH = height + 16

        context.roundedFill(x, y, x + boxW, y + boxH, gray38.rgba, 5)
        context.roundedOutline(x, y, x + boxW, y + boxH, ClickGUIModule.clickGUIColor.rgba, 1.5f, 5)
        context.drawWordWrap(font, Component.literal(description.text), x + 8, y + 8, DESCRIPTION_MAX_WIDTH, white.rgba, false)
    }

    data class Description(var text: String, var x: Int, var y: Int, var hoverHandler: HoverHandler)

    fun currentMousePosition(): Pair<Int, Int> {
        val guiScale = mc.window.guiScale.toDouble()
        return (mc.mouseHandler.xpos() / guiScale).toInt() to (mc.mouseHandler.ypos() / guiScale).toInt()
    }

    private const val DESCRIPTION_MAX_WIDTH = 300
}