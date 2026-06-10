package com.odtheking.odin.clickgui.vanilla

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.vanilla.VanillaGUI.currentMousePosition
import com.odtheking.odin.features.Category
import com.odtheking.odin.features.ModuleManager
import com.odtheking.odin.features.impl.render.ClickGUIModule
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.roundedFill
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent

class Panel(private val category: Category) {

    val panelSetting = ClickGUIModule.panelSetting[category.name]
        ?: throw IllegalStateException("Panel setting for category ${category.name} is not initialized")

    val moduleButtons = ModuleManager.modulesByCategory[category]
        ?.sortedByDescending { mc.font.width(it.name) }
        ?.map { ModuleButton(it, this) }
        ?: listOf()

    private var previousHeight = 0
    private var scrollOffset = 0
    var dragging = false
        private set
    private var deltaX = 0
    private var deltaY = 0

    fun draw(context: GuiGraphics, mouseX: Int, mouseY: Int) {
        if (dragging) {
            panelSetting.x = deltaX + mouseX
            panelSetting.y = deltaY + mouseY
        }

        val startX = panelSetting.x
        val startY = panelSetting.y

        context.roundedFill(startX, startY, startX + WIDTH, startY + HEIGHT, VanillaGUI.gray26.rgba, 5, 5, 0, 0)
        context.drawCenteredString(mc.font, "§l${category.name}", startX + WIDTH / 2, startY + HEIGHT / 2 - 4, Colors.WHITE.rgba)

        var offsetY = scrollOffset + HEIGHT
        if (panelSetting.extended) {
            for (button in moduleButtons) {
                if (!button.module.name.contains(SearchBar.currentSearch, true)) continue
                offsetY += button.draw(context, startX, startY + offsetY, mouseX, mouseY)
            }
        }

        previousHeight = offsetY
        val color = if (moduleButtons.lastOrNull()?.module?.enabled == true) ClickGUIModule.clickGUIColor.rgba else VanillaGUI.gray26.rgba
        context.roundedFill(startX, startY + offsetY, startX + WIDTH, startY + offsetY + 5, color, 0, 0, 5, 5)
    }

    fun handleScroll(amount: Int): Boolean {
        if (!isMouseOverExtended()) return false
        scrollOffset = (scrollOffset + amount).coerceIn((-previousHeight + scrollOffset + 72).coerceAtMost(0), 0)
        return true
    }

    fun mouseClicked(mouseX: Int, mouseY: Int, click: MouseButtonEvent): Boolean {
        val panelX = panelSetting.x
        val panelY = panelSetting.y
        if (isHovered(panelX, panelY, WIDTH, HEIGHT, mouseX, mouseY)) {
            if (click.button() == 0) {
                deltaX = panelX - mouseX
                deltaY = panelY - mouseY
                dragging = true
                return true
            }
            if (click.button() == 1) {
                panelSetting.extended = !panelSetting.extended
                return true
            }
        } else if (isMouseOverExtended()) {
            return moduleButtons.reversed().any {
                if (!it.module.name.contains(SearchBar.currentSearch, true)) return@any false
                it.mouseClicked(mouseX, mouseY, click)
            }
        }
        return false
    }

    fun mouseReleased(mouseX: Int, mouseY: Int, click: MouseButtonEvent) {
        dragging = false
        if (panelSetting.extended) {
            moduleButtons.reversed().forEach {
                if (!it.module.name.contains(SearchBar.currentSearch, true)) return@forEach
                it.mouseReleased(mouseX, mouseY, click)
            }
        }
    }

    fun keyTyped(input: CharacterEvent): Boolean {
        if (!panelSetting.extended) return false
        return moduleButtons.reversed().any {
            if (!it.module.name.contains(SearchBar.currentSearch, true)) return@any false
            it.keyTyped(input)
        }
    }

    fun keyPressed(input: KeyEvent): Boolean {
        if (!panelSetting.extended) return false
        return moduleButtons.reversed().any {
            if (!it.module.name.contains(SearchBar.currentSearch, true)) return@any false
            it.keyPressed(input)
        }
    }

    private fun isMouseOverExtended(): Boolean {
        val (mx, my) = currentMousePosition()
        return panelSetting.extended && isHovered(panelSetting.x, panelSetting.y, WIDTH, previousHeight.coerceAtLeast(HEIGHT), mx, my)
    }

    companion object {
        const val WIDTH = 160
        const val HEIGHT = 21
    }
}

internal fun isHovered(x: Int, y: Int, width: Int, height: Int, mouseX: Int, mouseY: Int): Boolean {
    return mouseX in x..(x + width) && mouseY in y..(y + height)
}