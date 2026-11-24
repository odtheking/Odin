package com.odtheking.odin.clickgui

import androidx.compose.runtime.*
import com.mojang.blaze3d.platform.InputConstants
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.ui.Panel
import com.odtheking.odin.clickgui.ui.components.TextBox
import com.odtheking.odin.features.impl.render.ClickGUIModule
import com.odtheking.odin.utils.ui.compose.FocusRequester
import com.odtheking.odin.utils.ui.compose.UiHost
import com.odtheking.odin.utils.ui.compose.offset
import com.odtheking.odin.utils.ui.compose.size
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.input.KeyEvent
import net.minecraft.network.chat.Component

class ClickGUI(panels: List<Panel>) : OdinScreen(Component.literal("Click GUI")) {

    private var searchString by mutableStateOf("")

    private val searchFocus = FocusRequester()
    private val panels = panels.toMutableStateList()

    override val ui: UiHost by lazy {
        UiHost {
            TextBox(
                searchString, { searchString = it },
                placeholder = "Search...", maxLength = 18, centered = true, radius = 6f, focusRequester = searchFocus,
            ).size(SEARCH_WIDTH, SEARCH_HEIGHT).offset({ ui.width / 2 - SEARCH_WIDTH / 2 }, { ui.height - SEARCH_BOTTOM_MARGIN })
            for (panel in panels) key(panel) {
                panel.Content({ searchString }) { bringToFront(panel) }
            }
        }
    }

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        ui.render(graphics, width, height, mouseX, mouseY)
        super.extractRenderState(graphics, mouseX, mouseY, partialTick)
    }

    override fun init() {
        super.init()
        ui.scale = ClickGUIModule.clickGuiScale / mc.window.guiScale
        focusSearch()
    }

    override fun removed() {
        super.removed()
        panels.flatMap { it.modules }.mapNotNullTo(hashSetOf()) { it.config }.forEach { it.save() }
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        if (event.key == InputConstants.KEYCODE_F && event.hasControlDownWithQuirk()) {
            focusSearch()
            return true
        }
        return super.keyPressed(event)
    }

    private fun focusSearch() = ui.focus(searchFocus)

    private fun bringToFront(panel: Panel) {
        if (panels.last() == panel) return
        panels.remove(panel)
        panels.add(panel)
    }
}

private const val SEARCH_WIDTH = 120
private const val SEARCH_HEIGHT = 20
private const val SEARCH_BOTTOM_MARGIN = 45