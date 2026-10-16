package com.odtheking.odin.clickgui

import androidx.compose.runtime.*
import com.mojang.blaze3d.platform.InputConstants
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.ui.Panel
import com.odtheking.odin.clickgui.ui.TextBox
import com.odtheking.odin.features.Category
import com.odtheking.odin.features.impl.render.ClickGUIModule
import com.odtheking.odin.utils.ui.compose.FocusRequester
import com.odtheking.odin.utils.ui.compose.UiHost
import com.odtheking.odin.utils.ui.compose.offset
import com.odtheking.odin.utils.ui.compose.size
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.input.KeyEvent
import net.minecraft.network.chat.Component

object ClickGUI : OdinScreen(Component.literal("Click GUI")) {

    var searchString by mutableStateOf("")
        private set

    private val searchFocus = FocusRequester()
    private val panels = mutableStateListOf<Category>()

    override val ui by lazy {
        if (Category.categories.keys.any { ClickGUIModule.panelSetting[it] == null }) ClickGUIModule.resetPositions()
        panels.addAll(Category.categories.values)

        UiHost {
            TextBox(
                searchString, { searchString = it },
                placeholder = "Search...", maxLength = 18, centered = true, radius = 6f, focusRequester = searchFocus,
            ).size(SEARCH_WIDTH, SEARCH_HEIGHT).offset({ virtualWidth / 2 - SEARCH_WIDTH / 2 }, { virtualHeight - SEARCH_BOTTOM_MARGIN })
            for (category in panels) key(category) {
                Panel(category) { bringToFront(category) }
            }
        }
    }

    private var scale: Float = 1f
    override val guiScale: Float get() = scale

    val virtualWidth get() = (mc.window.guiScaledWidth / scale).toInt()
    val virtualHeight get() = (mc.window.guiScaledHeight / scale).toInt()

    override fun extractRenderState(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int, partialTick: Float) {
        graphics.pose().pushMatrix()
        graphics.pose().scale(scale, scale)

        ui.render(graphics, virtualWidth, virtualHeight, (mouseX / scale).toInt(), (mouseY / scale).toInt())

        graphics.pose().popMatrix()
        super.extractRenderState(graphics, mouseX, mouseY, partialTick)
    }

    override fun init() {
        super.init()
        scale = ClickGUIModule.clickGuiScale.toFloat() / mc.window.guiScale
        focusSearch()
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        if (event.key == InputConstants.KEYCODE_F && event.hasControlDownWithQuirk()) {
            focusSearch()
            return true
        }
        return super.keyPressed(event)
    }

    private fun focusSearch() = ui.focus(searchFocus)

    private fun bringToFront(category: Category) {
        if (panels.last() == category) return
        panels.remove(category)
        panels.add(category)
    }

    private const val SEARCH_WIDTH = 120
    private const val SEARCH_HEIGHT = 20
    private const val SEARCH_BOTTOM_MARGIN = 45
}