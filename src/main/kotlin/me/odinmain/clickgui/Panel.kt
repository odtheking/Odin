package me.odinmain.clickgui

import me.odinmain.clickgui.ClickGUI.gray26
import me.odinmain.clickgui.settings.ModuleButton
import me.odinmain.features.Category
import me.odinmain.features.ModuleManager
import me.odinmain.features.impl.render.ClickGUIModule
import me.odinmain.features.impl.render.RandomPlayers
import me.odinmain.utils.render.Colors
import me.odinmain.utils.ui.isAreaHovered
import me.odinmain.utils.ui.rendering.NVGRenderer
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

    val moduleButtons: ArrayList<ModuleButton> = ArrayList<ModuleButton>().apply {
        ModuleManager.modules
            .filter { it.category == category && (!it.isDevModule || RandomPlayers.isRandom) }
            .sortedByDescending { NVGRenderer.textWidth(it.name, 16f, NVGRenderer.defaultFont) }
            .forEach { add(ModuleButton(it, this@Panel)) }
    }
    private val lastModuleButton by lazy { moduleButtons.lastOrNull() }

    val panelSetting = ClickGUIModule.panelSetting[category] ?: throw IllegalStateException("Panel setting for category $category is not initialized")

    private val textWidth = NVGRenderer.textWidth(category.displayName, 22f, NVGRenderer.defaultFont)
    private var previousHeight = 0f
    private var scrollOffset = 0f
    private var dragging = false
    private var deltaX = 0f
    private var deltaY = 0f

    fun draw(mouseX: Float, mouseY: Float) {
        if (dragging) {
            panelSetting.x = floor(deltaX + mouseX)
            panelSetting.y = floor(deltaY + mouseY)
        }

        NVGRenderer.dropShadow(panelSetting.x, panelSetting.y, WIDTH, (previousHeight + 10f).coerceAtLeast(HEIGHT), 10f, 3f, 5f)

        NVGRenderer.drawHalfRoundedRect(panelSetting.x, panelSetting.y, WIDTH, HEIGHT, gray26.rgba, 5f, true)
        NVGRenderer.text(category.displayName, panelSetting.x + WIDTH / 2f - textWidth / 2, panelSetting.y + HEIGHT / 2f - 11, 22f, Colors.WHITE.rgba, NVGRenderer.defaultFont)

        if (scrollOffset != 0f) NVGRenderer.pushScissor(panelSetting.x, panelSetting.y + HEIGHT, WIDTH, previousHeight - HEIGHT + 10f)

        var startY = scrollOffset + HEIGHT
        if (panelSetting.extended) {
            for (button in moduleButtons) {
                if (!button.module.name.contains(SearchBar.currentSearch, true)) continue
                startY += button.draw(panelSetting.x, startY + panelSetting.y)
            }
        }
        previousHeight = startY

        NVGRenderer.drawHalfRoundedRect(panelSetting.x, panelSetting.y + startY, WIDTH, 10f, if (lastModuleButton?.module?.enabled == true) ClickGUIModule.clickGUIColor.rgba else gray26.rgba, 5f, false)
        if (scrollOffset != 0f) NVGRenderer.popScissor()
    }

    fun handleScroll(amount: Int): Boolean {
        if (!isMouseOverExtended) return false
        scrollOffset = (scrollOffset + amount).coerceIn(-previousHeight + scrollOffset + 72f, 0f)
        return true
    }

    fun mouseClicked(mouseX: Float, mouseY: Float, button: Int): Boolean {
        if (isAreaHovered(panelSetting.x, panelSetting.y, WIDTH, HEIGHT)) {
            if (button == 0) {
                deltaX = (panelSetting.x - mouseX)
                deltaY = (panelSetting.y - mouseY)
                dragging = true
                return true
            } else if (button == 1) {
                panelSetting.extended = !panelSetting.extended
                return true
            }
        } else if (isMouseOverExtended) {
            return moduleButtons.reversed().any {
                if (!it.module.name.contains(SearchBar.currentSearch, true)) return@any false
                it.mouseClicked(mouseX, mouseY, button)
            }
        }
        return false
    }

    fun mouseReleased(state: Int) {
        if (state == 0) dragging = false

        if (panelSetting.extended)
            moduleButtons.reversed().forEach {
                if (!it.module.name.contains(SearchBar.currentSearch, true)) return@forEach
                it.mouseReleased(state)
            }
    }

    fun keyTyped(typedChar: Char): Boolean {
        if (!panelSetting.extended) return false

        return moduleButtons.reversed().any {
            if (!it.module.name.contains(SearchBar.currentSearch, true)) return@any false
            it.keyTyped(typedChar)
        }
    }

    fun keyPressed(keyCode: Int): Boolean {
        if (!panelSetting.extended) return false

        return moduleButtons.reversed().any {
            if (!it.module.name.contains(SearchBar.currentSearch, true)) return@any false
            it.keyPressed(keyCode)
        }
    }

    private inline val isMouseOverExtended get() = panelSetting.extended && isAreaHovered(panelSetting.x, panelSetting.y, WIDTH, previousHeight.coerceAtLeast(HEIGHT))

    companion object {
        const val WIDTH = 240f
        const val HEIGHT = 32f
    }
}