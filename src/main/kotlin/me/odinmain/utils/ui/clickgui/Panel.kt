package me.odinmain.utils.ui.clickgui

import me.odinmain.features.Category
import me.odinmain.features.ModuleManager.modules
import me.odinmain.features.impl.render.ClickGUIModule
import me.odinmain.font.OdinFont
import me.odinmain.utils.render.*
import me.odinmain.utils.round
import me.odinmain.utils.ui.clickgui.SearchBar.currentSearch
import me.odinmain.utils.ui.clickgui.animations.impl.LinearAnimation
import me.odinmain.utils.ui.clickgui.elements.ModuleButton
import me.odinmain.utils.ui.clickgui.util.ColorUtil
import me.odinmain.utils.ui.util.MouseUtils.isAreaHovered
import me.odinmain.utils.ui.util.MouseUtils.mouseX
import me.odinmain.utils.ui.util.MouseUtils.mouseY
import net.minecraft.client.renderer.GlStateManager
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
        for (module in modules.sortedByDescending { getTextWidth(it.name, 18f) }) {
            if (module.category != this@Panel.category) continue
            moduleButtons.add(ModuleButton(module, this@Panel))
        }
    }

    fun draw() {
        if (dragging) {
            x = floor(x2 + mouseX)
            y = floor(y2 + mouseY)
        }

        scrollOffset = scrollAnimation.get(scrollOffset, scrollTarget).round(0).toFloat()
        var startY = scrollOffset + HEIGHT
        GlStateManager.scale(1f / scaleFactor, 1f / scaleFactor, 1f)
        dropShadow(x, y, WIDTH, if (extended) (length + 5f).coerceAtLeast(HEIGHT) else 40f, ColorUtil.moduleButtonColor, 15f, 10f, 10f, 10f, 10f)
        roundedRectangle(x, y, WIDTH, HEIGHT, ColorUtil.moduleButtonColor, ColorUtil.moduleButtonColor, ColorUtil.moduleButtonColor, 0f, 15f, 15f, 0f, 0f, 0f)

        text(category.displayName, x + WIDTH / 2f, y + HEIGHT / 2f, ColorUtil.textColor, 20f, type = OdinFont.BOLD, TextAlign.Middle)

        val s = scissor(x, y + HEIGHT, WIDTH, 5000f)
        if (extended && moduleButtons.isNotEmpty()) {
            for (button in moduleButtons.filter { it.module.name.contains(currentSearch, true) }) {
                button.y = startY
                startY += button.draw()
            }
            length = startY + 5f
        }

        val lastColor =
            if (extended) moduleButtons.lastOrNull()?.color ?: ColorUtil.moduleButtonColor
            else ColorUtil.moduleButtonColor
        roundedRectangle(x, y + startY, WIDTH, 10f, lastColor, lastColor, lastColor, 0f, 0f, 0f, 10f, 10f, 4f)
        resetScissor(s)
        GlStateManager.scale(scaleFactor, scaleFactor, 1f)
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
            return moduleButtons.filter { it.module.name.contains(currentSearch, true) }.reversed().any {
                it.mouseClicked(mouseButton)
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
            moduleButtons.filter { it.module.name.contains(currentSearch, true) }.reversed().forEach {
                it.mouseReleased(state)
            }
        }
    }

    fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        return if (extended) {
            moduleButtons.filter { it.module.name.contains(currentSearch, true) }.reversed().any {
                it.keyTyped(typedChar, keyCode)
            }
        } else false
    }

    private val isHovered
        get() = isAreaHovered(x, y, WIDTH, HEIGHT)

    private val isMouseOverExtended
        get() = extended && isAreaHovered(x, y, WIDTH, length.coerceAtLeast(HEIGHT))

    companion object {
        const val WIDTH = 240f
        const val HEIGHT = 40f
    }
}