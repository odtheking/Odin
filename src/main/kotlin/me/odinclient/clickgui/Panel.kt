package me.odinclient.clickgui

import cc.polyfrost.oneconfig.renderer.font.Fonts
import cc.polyfrost.oneconfig.utils.dsl.*
import me.odinclient.features.Category
import me.odinclient.features.ModuleManager
import me.odinclient.clickgui.elements.ModuleButton
import me.odinclient.clickgui.util.ColorUtil
import me.odinclient.clickgui.util.FontUtil.capitalizeOnlyFirst
import me.odinclient.clickgui.util.FontUtil.drawCustomCenteredText
import me.odinclient.clickgui.util.MouseUtils.isAreaHovered
import me.odinclient.clickgui.util.MouseUtils.scaledMouseX
import me.odinclient.clickgui.util.MouseUtils.scaledMouseY
import me.odinclient.features.general.ClickGui
import me.odinclient.utils.render.HUDRenderUtils.startDraw

class Panel(
    var category: Category,
    var clickgui: ClickGUI
) {
    var dragging = false
    val moduleButtons: ArrayList<ModuleButton> = ArrayList()

    var x = ClickGui.panelX[category]!!.value.toInt()
    var y = ClickGui.panelY[category]!!.value.toInt()

    val width = ClickGui.PANEL_WIDTH
    val height = ClickGui.PANEL_HEIGHT

    var extended: Boolean = ClickGui.panelExtended[category]!!.enabled

    var scrollOffset = 0
    var length = 0
    var scrollAmount = 0

    private var x2 = 0
    private var y2 = 0

    init {
        for (module in ModuleManager.modules) {
            if (module.category != this.category) continue
            moduleButtons.add(ModuleButton(module, this))
        }
        moduleButtons.sortBy { it.module.name.length }
        moduleButtons.reverse()
    }

    fun drawScreen(partialTicks: Float, vg: VG) {
        if (dragging) {
            x = x2 + scaledMouseX
            y = y2 + scaledMouseY
        }

        if (scrollAmount != 0)
            handleScroll()

        vg.startDraw() {
            setUpScissor(x - 2, y + height, x + width + 1, y + height + 4000)

            var startY = height
            if (extended && moduleButtons.isNotEmpty()) {
                startY -= scrollOffset
                for (moduleButton in moduleButtons) {
                    moduleButton.y = startY
                    startY += moduleButton.drawScreen(partialTicks, vg)
                }
                length = startY + 5
            }

            endScissor()

            roundedRect(x, y, width, height, ColorUtil.moduleButtonColor, 5f, 0f)
            drawShadow(x, y, width, startY + 5, 12.5f, 6f, 5f)
            roundedRect(x, y + startY, width, 5, ColorUtil.moduleButtonColor, 0f, 5f)
            // This version also colors the bottom of the panel blue or grey if the last module is enabled/disabled
            // roundedRect(x, y + startY, width, 5, ColorUtil.moduleColor(moduleButtons.last().module.enabled && extended), 0f, 5f)
            vg.drawCustomCenteredText(category.name.capitalizeOnlyFirst(), x + width / 2, y + height + 2, 24f, Fonts.SEMIBOLD)
        }
    }

    fun mouseClicked(mouseButton: Int): Boolean {
        if (isHovered) {
            if (mouseButton == 0) {
                x2 = x - scaledMouseX
                y2 = y - scaledMouseY
                dragging = true
                return true
            } else if (mouseButton == 1) {
                extended = !extended
                return true
            }
        } else if (isMouseOverExtended) {
            for (moduleButton in moduleButtons.reversed()) {
                if (moduleButton.mouseClicked(mouseButton)) {
                    return true
                }
            }
        }
        return false
    }

    fun mouseReleased(state: Int) {
        if (state == 0) dragging = false

        ClickGui.panelX[category]!!.value = x.toDouble()
        ClickGui.panelY[category]!!.value = y.toDouble()
        ClickGui.panelExtended[category]!!.enabled = extended

        if (extended) {
            for (moduleButton in moduleButtons.reversed()) {
                moduleButton.mouseReleased(state)
            }
        }
    }

    fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        if (extended) {
            for (moduleButton in moduleButtons.reversed()) {
                if (moduleButton.keyTyped(typedChar, keyCode)) return true
            }
        }
        return false
    }

    fun initializeScroll(amount: Int): Boolean {
        if (isMouseOverExtended) {
            val diff = (-amount * SCROLL_DISTANCE).coerceAtMost(length - height - 16)
            val realDiff = (scrollOffset + diff).coerceAtLeast(0) - scrollOffset

            scrollAmount = realDiff
            return true
        }
        return false
    }

    private fun handleScroll() {
        if (scrollAmount > 0) {
            scrollAmount--
            scrollOffset++
        } else {
            scrollAmount++
            scrollOffset--
        }
    }

    private val isHovered
        get() = isAreaHovered(x, y, width, height)

    private val isMouseOverExtended
        get() = extended && isAreaHovered(x, y, width, length)

    companion object {
        private const val SCROLL_DISTANCE = 16
    }
}