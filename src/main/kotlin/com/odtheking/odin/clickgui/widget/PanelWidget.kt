package com.odtheking.odin.clickgui.widget

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.ClickGUI
import com.odtheking.odin.clickgui.GuiTheme
import com.odtheking.odin.features.Category
import com.odtheking.odin.features.ModuleManager
import com.odtheking.odin.features.impl.render.ClickGUIModule
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.Corners
import com.odtheking.odin.utils.render.roundedRect
import com.odtheking.odin.utils.render.roundedShadow
import com.odtheking.odin.utils.render.scissored
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.chat.Component
import kotlin.math.sign

class PanelWidget(category: Category) : OdinContainerWidget(
    0, 0, GuiTheme.ROW_WIDTH, GuiTheme.ROW_HEIGHT + GuiTheme.CAP, Component.literal(category.name)
) {
    private val panelData = ClickGUIModule.panelSetting.getOrPut(category.name) { ClickGUIModule.PanelData(0, 0) }
    private val boldTitle = "§l${category.name}"

    private val modules = ModuleManager.modulesByCategory[category]
        ?.sortedByDescending { mc.font.width(it.name) }
        ?.map { ModuleWidget(it) }.orEmpty()

    override fun children(): List<GuiEventListener> = modules

    private var contentHeight = 0
    private var scroll = 0

    private var headerGrabbed = false
    private var grabX = 0
    private var grabY = 0

    var extended: Boolean
        get() = panelData.extended
        set(value) {
            panelData.extended = value
            if (!value) {
                scroll = 0
                modules.forEach { it.visible = false }
            }
        }

    fun moveTo(newX: Int, newY: Int) {
        panelData.x = newX.coerceIn(0, maxX)
        panelData.y = newY.coerceIn(0, maxY)
    }

    private val maxX get() = (ClickGUI.virtualWidth - GuiTheme.ROW_WIDTH).coerceAtLeast(0)
    private val maxY get() = (ClickGUI.virtualHeight - GuiTheme.ROW_HEIGHT).coerceAtLeast(0)

    fun release() = modules.forEach { it.release() }

    fun resetScroll() {
        scroll = 0
    }

    override fun render(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        layout()
        graphics.roundedShadow(x, y, x + width, y + height, GuiTheme.shadow.rgba, GuiTheme.PANEL_BLUR, CORNERS)

        graphics.roundedRect(x, y, x + width, y + GuiTheme.ROW_HEIGHT, GuiTheme.background.rgba, CORNERS_TOP)
        graphics.centeredText(mc.font, boldTitle, x + width / 2, GuiTheme.textY(y, GuiTheme.ROW_HEIGHT), Colors.WHITE.rgba)

        val capY = y + height - GuiTheme.CAP
        if (extended) {
            val top = y + GuiTheme.ROW_HEIGHT
            graphics.scissored(x, top, x + width, capY) {
                for (module in modules) {
                    if (!module.visible || module.bottom <= top || module.y >= capY) continue
                    module.extractRenderState(graphics, mouseX, mouseY, 0f)
                }
            }
        }

        graphics.roundedRect(
            x, capY, x + width, capY + GuiTheme.CAP,
            (if (modules.lastOrNull()?.module?.enabled == true && extended) GuiTheme.accent else GuiTheme.background).rgba,
            CORNERS_BOTTOM
        )
    }

    override fun onClick(event: MouseButtonEvent, doubleClick: Boolean) {
        headerGrabbed = event.y().toInt() < y + GuiTheme.ROW_HEIGHT
        if (!headerGrabbed) return

        if (event.button() == RIGHT) {
            extended = !extended
            return
        }
        grabX = event.x().toInt() - x
        grabY = event.y().toInt() - y
    }

    override fun onDrag(event: MouseButtonEvent, dragX: Double, dragY: Double) {
        if (headerGrabbed) moveTo(event.x().toInt() - grabX, event.y().toInt() - grabY)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        if (!extended || !isMouseOver(mouseX, mouseY)) return false
        val limit = (contentHeight - MIN_VISIBLE).coerceAtLeast(0)
        scroll = (scroll + (scrollY.sign * SCROLL_STEP).toInt()).coerceIn(-limit, 0)
        return true
    }

    private fun layout() {
        x = panelData.x.coerceIn(0, maxX)
        y = panelData.y.coerceIn(0, maxY)

        if (!extended) {
            height = GuiTheme.ROW_HEIGHT + GuiTheme.CAP
            return
        }

        contentHeight = 0
        var filtering = false
        for (module in modules) {
            module.visible = module.matches(ClickGUI.searchString)
            filtering = filtering || module.isFiltering
            if (!module.visible) continue
            module.measure()
            contentHeight += module.height
        }

        if (!filtering) scroll = scroll.coerceIn(-(contentHeight - MIN_VISIBLE).coerceAtLeast(0), 0)

        var offset = GuiTheme.ROW_HEIGHT + scroll
        for (module in modules) {
            if (!module.visible) continue
            module.x = x
            module.y = y + offset
            module.place()
            offset += module.height
        }

        height = offset + GuiTheme.CAP
    }

    private companion object {
        const val MIN_VISIBLE = 72
        const val SCROLL_STEP = 16

        val CORNERS = Corners(GuiTheme.RADIUS)
        val CORNERS_TOP = Corners.top(GuiTheme.RADIUS)
        val CORNERS_BOTTOM = Corners.bottom(GuiTheme.RADIUS)
    }
}