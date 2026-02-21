package com.odtheking.odin.clickgui

import com.odtheking.odin.clickgui.ClickGUI.gray26
import com.odtheking.odin.clickgui.settings.ModuleButton
import com.odtheking.odin.features.Category
import com.odtheking.odin.features.ModuleManager
import com.odtheking.odin.features.impl.render.ClickGUIModule
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.ui.isAreaHovered
import com.odtheking.odin.utils.ui.rendering.NVGRenderer
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import kotlin.math.floor

/**
 * Renders all the panels.
 *
 * Backend made by Aton, with some changes
 * Design mostly made by Stivais
 *
 * @author Stivais, Aton
 * @see [ModuleButton]
 */
class Panel(private val category: Category) {

    val panelSetting = ClickGUIModule.panelSetting[category.name] ?: throw IllegalStateException("Panel setting for category $category is not initialized")
    val moduleButtons = ModuleManager.modulesByCategory[category]
        ?.sortedByDescending { NVGRenderer.textWidth(it.name, 16f, NVGRenderer.defaultFont) }
        ?.map { ModuleButton(it, this@Panel) } ?: listOf()
    private val lastModuleButton by lazy { moduleButtons.lastOrNull() }

    private val textWidth = NVGRenderer.textWidth(category.name, 22f, NVGRenderer.defaultFont)
    private var previousHeight = 0f
    private var scrollOffset = 0f
    var dragging = false
        private set
    private var deltaX = 0f
    private var deltaY = 0f

    fun draw(mouseX: Float, mouseY: Float) {
        if (dragging) {
            panelSetting.x = floor(deltaX + mouseX)
            panelSetting.y = floor(deltaY + mouseY)
        }

        NVGRenderer.dropShadow(
            panelSetting.x,
            panelSetting.y,
            WIDTH,
            (previousHeight + if (ClickGUIModule.roundedPanelBottom) 10f else 0f).coerceAtLeast(HEIGHT),
            10f,
            3f,
            5f
        )

        NVGRenderer.drawHalfRoundedRect(panelSetting.x, panelSetting.y, WIDTH, HEIGHT, gray26.rgba, 5f, true)
        NVGRenderer.text(
            category.name,
            panelSetting.x + WIDTH / 2f - textWidth / 2,
            panelSetting.y + HEIGHT / 2f - 11,
            22f,
            Colors.WHITE.rgba,
            NVGRenderer.defaultFont
        )

        if (scrollOffset != 0f) NVGRenderer.pushScissor(
            panelSetting.x,
            panelSetting.y + HEIGHT,
            WIDTH,
            previousHeight - HEIGHT + 10f
        )

        var startY = scrollOffset + HEIGHT
        if (panelSetting.extended) {
            for (button in moduleButtons) {
                if (!button.module.name.contains(SearchBar.currentSearch, true)) continue
                startY += button.draw(panelSetting.x, startY + panelSetting.y, button == lastModuleButton)
            }
        }
        previousHeight = startY

        if (ClickGUIModule.roundedPanelBottom) {
            NVGRenderer.drawHalfRoundedRect(
                panelSetting.x,
                panelSetting.y + startY,
                WIDTH,
                10f,
                if (lastModuleButton?.module?.enabled == true) ClickGUIModule.clickGUIColor.rgba else gray26.rgba,
                5f,
                false
            )
        }
        if (scrollOffset != 0f) NVGRenderer.popScissor()
    }

    fun handleScroll(amount: Int): Boolean {
        if (!isMouseOverExtended) return false
        scrollOffset = (scrollOffset + amount).coerceIn((-previousHeight + scrollOffset + 72f).coerceAtMost(0f), 0f)
        return true
    }

    fun mouseClicked(mouseX: Float, mouseY: Float, click: MouseButtonEvent): Boolean {
        if (isAreaHovered(panelSetting.x, panelSetting.y, WIDTH, HEIGHT, true)) {
            if (click.button() == 0) {
                deltaX = (panelSetting.x - mouseX)
                deltaY = (panelSetting.y - mouseY)
                dragging = true
                return true
            } else if (click.button() == 1) {
                panelSetting.extended = !panelSetting.extended
                return true
            }
        } else if (isMouseOverExtended) {
            return moduleButtons.reversed().any {
                if (!it.module.name.contains(SearchBar.currentSearch, true)) return@any false
                it.mouseClicked(mouseX, mouseY, click)
            }
        }
        return false
    }

    fun mouseReleased(click: MouseButtonEvent) {
        dragging = false

        if (panelSetting.extended)
            moduleButtons.reversed().forEach {
                if (!it.module.name.contains(SearchBar.currentSearch, true)) return@forEach
                it.mouseReleased(click)
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

    private inline val isMouseOverExtended
        get() = panelSetting.extended && isAreaHovered(
            panelSetting.x,
            panelSetting.y,
            WIDTH,
            previousHeight.coerceAtLeast(HEIGHT),
            true
        )

    companion object {
        const val WIDTH = 240f
        const val HEIGHT = 32f
    }
}