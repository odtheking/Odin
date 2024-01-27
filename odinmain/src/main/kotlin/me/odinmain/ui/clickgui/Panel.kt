package me.odinmain.ui.clickgui

import me.odinmain.features.Category
import me.odinmain.features.ModuleManager.modules
import me.odinmain.features.impl.render.ClickGUIModule
import me.odinmain.ui.clickgui.SearchBar.currentSearch
import me.odinmain.ui.clickgui.elements.ModuleButton
import me.odinmain.ui.clickgui.util.ColorUtil
import me.odinmain.utils.capitalizeFirst
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.gui.*
import me.odinmain.utils.render.gui.MouseUtils.isAreaHovered
import me.odinmain.utils.render.gui.MouseUtils.mouseX
import me.odinmain.utils.render.gui.MouseUtils.mouseY
import me.odinmain.utils.render.gui.animations.impl.LinearAnimation
import me.odinmain.utils.round
import me.odinmain.utils.skyblock.modMessage
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
class Panel(
    var category: Category,
) {
    val displayName = category.name.capitalizeFirst()

    private var dragging = false
    val moduleButtons: ArrayList<ModuleButton> = ArrayList()

    var x = ClickGUIModule.panelX[category]!!.value
    var y = ClickGUIModule.panelY[category]!!.value

    var extended: Boolean = ClickGUIModule.panelExtended[category]!!.enabled

    private var length = 0f

    private var x2 = 0f
    private var y2 = 0f

    private var scrollTarget = 0f
    private var scrollOffset = 0f
    private val scrollAnimation = LinearAnimation<Float>(200)

    init {
        for (module in modules.sortedByDescending { getTextWidth(it.name, 18f, Fonts.REGULAR) }) {
            if (module.category != this@Panel.category) continue
            moduleButtons.add(ModuleButton(module, this@Panel))
        }
    }

    fun draw() {
        if (dragging) {
            x = floor(x2 + mouseX)
            y = floor(y2 + mouseY)
        }

        scrollOffset = scrollAnimation.get(scrollOffset, scrollTarget).round(0)
        var startY = scrollOffset + height

        roundedRectangle(x, y, width, height, ColorUtil.moduleButtonColor, ColorUtil.moduleButtonColor, ColorUtil.moduleButtonColor, 0f, 15f, 15f, 0f, 0f, 0f)
        text(if (displayName == "Floor7") "Floor 7" else "§l${displayName}", x + width / 2f, y + height / 2f, ColorUtil.textColor, 22f, Fonts.SEMIBOLD, TextAlign.Middle)



        val s = scissor(x, y + height, width, 5000f)
        if (extended && moduleButtons.isNotEmpty()) {
                for (button in moduleButtons.filter { it.module.name.contains(currentSearch, true) }) {
                    button.y = startY
                    startY += button.draw()
                }
                length = startY + 5f
            }

        moduleButtons.lastOrNull()?.color?.let { roundedRectangle(x, y + startY, width, 10f, it, it, it, 0f, 0f, 0f, 10f, 10f, 4f) }
        resetScissor(s)
        dropShadow(x, y, width, (startY + 10f).coerceAtLeast(height), ColorUtil.moduleButtonColor, 15f, 10f, 10f, 10f, 10f)
    }

    fun handleScroll(amount: Int): Boolean {
        if (isMouseOverExtended && currentSearch.isBlank()) {
            scrollTarget = (scrollTarget + amount).coerceIn(-length + scrollOffset + 72f, 0f)
            scrollAnimation.start(true)
            return true
        }
        return false
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
            for (i in moduleButtons.size - 1 downTo 0) {
                if (moduleButtons[i].mouseClicked(mouseButton)) {
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
            for (i in moduleButtons.size - 1 downTo 0) {
                moduleButtons[i].mouseReleased(state)
            }
        }
    }

    fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        if (extended) {
            for (i in moduleButtons.size - 1 downTo 0) {
                if (moduleButtons[i].keyTyped(typedChar, keyCode)) return true
            }
        }
        return false
    }

    private val isHovered
        get() = isAreaHovered(x, y, width, height)

    private val isMouseOverExtended
        get() = extended && isAreaHovered(x, y, width, length.coerceAtLeast(height))

    companion object {
        const val width = 240f
        const val height = 40f
    }
}