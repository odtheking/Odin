package me.odinclient.ui.clickgui

import cc.polyfrost.oneconfig.renderer.font.Fonts
import cc.polyfrost.oneconfig.utils.dsl.VG
import cc.polyfrost.oneconfig.utils.dsl.drawDropShadow
import cc.polyfrost.oneconfig.utils.dsl.drawRoundedRectVaried
import me.odinclient.ui.clickgui.elements.ModuleButton
import me.odinclient.ui.clickgui.util.ColorUtil
import me.odinclient.utils.render.gui.MouseUtils.isAreaHovered
import me.odinclient.utils.render.gui.MouseUtils.mouseX
import me.odinclient.utils.render.gui.MouseUtils.mouseY
import me.odinclient.features.Category
import me.odinclient.features.ModuleManager
import me.odinclient.features.impl.general.ClickGUIModule
import me.odinclient.utils.render.gui.GuiUtils.capitalizeOnlyFirst
import me.odinclient.utils.render.gui.GuiUtils.drawCustomCenteredText
import me.odinclient.utils.render.gui.GuiUtils.nanoVG
import me.odinclient.utils.render.gui.GuiUtils.resetScissor
import me.odinclient.utils.render.gui.GuiUtils.scissor
import kotlin.math.floor

class Panel(
    var category: Category,
) {
    var dragging = false
    val moduleButtons: ArrayList<ModuleButton> = ArrayList()

    var x = ClickGUIModule.panelX[category]!!.value
    var y = ClickGUIModule.panelY[category]!!.value

    var extended: Boolean = ClickGUIModule.panelExtended[category]!!.enabled

    var scrollOffset = 0f
    var length = 0f
    var scrollAmount = 0f

    private var x2 = 0f
    private var y2 = 0f

    init {
        for (module in ModuleManager.modules) {
            if (module.category != this.category) continue
            moduleButtons.add(ModuleButton(module, this))
        }
    }

    fun drawScreen(vg: VG) {
        if (dragging) {
            x = floor(x2 + mouseX)
            y = floor(y2 + mouseY)
        }

        if (scrollAmount != 0f) handleScroll()

        vg.nanoVG {
            val scissor = scissor(x - 2f, y + height, x + width + 1, y + height + 4000)
            var startY = height
            if (extended && moduleButtons.isNotEmpty()) {
                for (moduleButton in moduleButtons) {
                    moduleButton.y = startY
                    startY += moduleButton.draw(vg)
                }
                length = startY + 5f
            }
            resetScissor(scissor)

            drawRoundedRectVaried(x, y, width, height, ColorUtil.moduleButtonColor, 5f, 5f, 0f, 0f)
            drawDropShadow(x, y, width, startY + 10f, 12.5f, 6f, 5f)

            drawRoundedRectVaried(x, y + startY, width, 10f, ColorUtil.moduleColor(moduleButtons.last().module.enabled && extended), 0f, 0f, 5f, 5f)
            drawCustomCenteredText(category.name.capitalizeOnlyFirst(), x + width / 2, y + height / 2, 22f, Fonts.SEMIBOLD)
        }
    }

    fun mouseClicked(mouseButton: Int): Boolean {
        if (isHovered) {
            if (mouseButton == 0) {
                x2 = x - mouseX
                y2 = y - mouseY
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

        ClickGUIModule.panelX[category]!!.value = x
        ClickGUIModule.panelY[category]!!.value = y
        ClickGUIModule.panelExtended[category]!!.enabled = extended

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
            val realDiff = (scrollOffset + diff).coerceAtLeast(0f) - scrollOffset

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
        const val width = 240f
        const val height = 40f
        private const val SCROLL_DISTANCE = 16f
    }
}