package com.odtheking.odin.clickgui

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.widget.PanelWidget
import com.odtheking.odin.clickgui.widget.SearchBarWidget
import com.odtheking.odin.features.Category
import com.odtheking.odin.features.ModuleManager
import com.odtheking.odin.features.impl.render.ClickGUIModule
import com.odtheking.odin.utils.Color.Companion.hsbMax
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.roundedRectOutlined
import com.odtheking.odin.utils.ui.animations.Animations
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import net.minecraft.util.FormattedCharSequence
import org.lwjgl.glfw.GLFW

object ClickGUI : Screen(Component.literal("Click GUI")) {

    var searchString = ""
        private set

    private val panels: MutableList<PanelWidget> by lazy {
        if (Category.categories.keys.any { ClickGUIModule.panelSetting[it] == null }) ClickGUIModule.resetPositions()
        Category.categories.values.mapTo(mutableListOf()) { PanelWidget(it) }
    }

    private val searchBar by lazy {
        SearchBarWidget { text ->
            searchString = text
            panels.forEach { it.resetScroll() }
        }
    }

    private var scale: Float = 1f

    val virtualWidth get() = (mc.window.guiScaledWidth / scale).toInt()
    val virtualHeight get() = (mc.window.guiScaledHeight / scale).toInt()

    override fun children(): List<GuiEventListener> = panels.asReversed() + searchBar

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        val scaledX = (mouseX / scale).toInt()
        val scaledY = (mouseY / scale).toInt()

        graphics.pose().pushMatrix()
        graphics.pose().scale(scale, scale)

        searchBar.extractRenderState(graphics, scaledX, scaledY, partialTick)
        for (panel in panels) panel.extractRenderState(graphics, scaledX, scaledY, partialTick)
        drawDescription(graphics)

        graphics.pose().popMatrix()
        super.extractRenderState(graphics, mouseX, mouseY, partialTick)
    }

    fun setDescription(text: String, x: Int, y: Int) {
        description = text
        descriptionX = x
        descriptionY = y
    }

    private var description = ""
    private var descriptionX = 0
    private var descriptionY = 0

    private var lines: List<FormattedCharSequence> = emptyList()
    private var wrappedText = ""
    private var boxWidth = 0

    private fun drawDescription(graphics: GuiGraphicsExtractor) {
        if (description.isEmpty()) return

        if (description != wrappedText) {
            wrappedText = description
            lines = font.split(Component.literal(description), DESCRIPTION_WIDTH)
            boxWidth = (lines.maxOfOrNull { font.width(it) } ?: 0) + DESCRIPTION_PADDING * 2
        }

        val boxHeight = lines.size * font.lineHeight + DESCRIPTION_PADDING * 2
        val boxX = descriptionX.coerceIn(0, (virtualWidth - boxWidth).coerceAtLeast(0))
        val boxY = descriptionY.coerceIn(0, (virtualHeight - boxHeight).coerceAtLeast(0))

        graphics.roundedRectOutlined(
            boxX, boxY, boxX + boxWidth, boxY + boxHeight,
            GuiTheme.surface.rgba, GuiTheme.accent.hsbMax().rgba, 1.5f, GuiTheme.RADIUS
        )
        lines.forEachIndexed { index, line ->
            graphics.text(
                font, line, boxX + DESCRIPTION_PADDING,
                boxY + DESCRIPTION_PADDING + index * font.lineHeight, Colors.WHITE.rgba, false
            )
        }

        description = ""
    }

    private fun MouseButtonEvent.intoGuiSpace(): MouseButtonEvent =
        scale.let { MouseButtonEvent(x() / it, y() / it, buttonInfo()) }

    override fun mouseClicked(event: MouseButtonEvent, doubleClick: Boolean): Boolean {
        val scaled = event.intoGuiSpace()
        bringToFront(scaled)
        return super.mouseClicked(scaled, doubleClick)
    }

    override fun mouseReleased(event: MouseButtonEvent): Boolean = super.mouseReleased(event.intoGuiSpace())

    override fun mouseDragged(event: MouseButtonEvent, dragX: Double, dragY: Double): Boolean {
        val factor = scale
        return super.mouseDragged(event.intoGuiSpace(), dragX / factor, dragY / factor)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        val factor = scale
        return super.mouseScrolled(mouseX / factor, mouseY / factor, scrollX, scrollY)
    }

    override fun init() {
        Animations.settle()

        scale = ClickGUIModule.clickGuiScale.toFloat() / mc.window.guiScale
        searchBar.place(virtualWidth / 2 - SearchBarWidget.WIDTH / 2, virtualHeight - SEARCH_BAR_BOTTOM_MARGIN)
        focusSearch()
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        if (event.key == GLFW.GLFW_KEY_F && event.hasControlDownWithQuirk()) {
            focusSearch()
            return true
        }
        return focused?.keyPressed(event) == true || super.keyPressed(event)
    }

    private fun focusSearch() {
        setInitialFocus(searchBar)
        searchBar.selectAll()
    }

    override fun onClose() {
        panels.forEach { it.release() }
        ModuleManager.saveConfigurations()
        super.onClose()
    }

    override fun isPauseScreen(): Boolean = false

    private fun bringToFront(event: MouseButtonEvent) {
        val clicked = panels.lastOrNull { it.isMouseOver(event.x(), event.y()) } ?: return
        if (panels.last() === clicked) return
        panels.remove(clicked)
        panels.add(clicked)
    }

    private const val SEARCH_BAR_BOTTOM_MARGIN = 45
    private const val DESCRIPTION_WIDTH = 200
    private const val DESCRIPTION_PADDING = 8
}
