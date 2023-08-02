package me.odinclient.ui.clickgui.elements

import cc.polyfrost.oneconfig.renderer.font.Fonts
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.*
import me.odinclient.ui.clickgui.Panel
import me.odinclient.ui.clickgui.elements.menu.*
import me.odinclient.ui.clickgui.util.ColorUtil.brighter
import me.odinclient.ui.clickgui.util.ColorUtil.clickGUIColor
import me.odinclient.ui.clickgui.util.ColorUtil.moduleButtonColor
import me.odinclient.ui.clickgui.util.ColorUtil.textColor
import me.odinclient.ui.clickgui.util.HoverHandler
import me.odinclient.utils.render.Color
import me.odinclient.utils.render.gui.MouseUtils.isAreaHovered
import me.odinclient.utils.render.gui.animations.impl.ColorAnimation
import me.odinclient.utils.render.gui.animations.impl.EaseInOut
import me.odinclient.utils.render.gui.nvg.*
import kotlin.math.floor

class ModuleButton(val module: Module, val panel: Panel) {

    val menuElements: ArrayList<Element<*>> = ArrayList()

    val x: Float
        inline get() = panel.x

    var y: Float = 0f
        get() = field + panel.y

    private val colorAnim = ColorAnimation(300)

    val color: Color
        get() = colorAnim.get(clickGUIColor, moduleButtonColor, module.enabled).brighter(1 + hoverHandler.percent() / 300f)

    val width = Panel.width
    val height = 32f

    var extended = false

    private val extendAnim = EaseInOut(250)
    private val hoverHandler = HoverHandler(150)

    init {
        updateElements()
        if (module.keyCode != -999) menuElements.add(ElementKeyBind(this, module))
        //menuElements.add(ElementDescription(this, module.description))
    }

    private fun updateElements() {
        var position = -1 // This looks weird, but it starts at -1 because it gets incremented before being used.
        for (setting in module.settings) {
            /** Don't show hidden settings */
            if (setting.shouldBeVisible) run addElement@{
                position++
                if (menuElements.any { it.setting === setting }) return@addElement
                val newElement = when (setting) {
                    is BooleanSetting -> ElementCheckBox(this, setting)
                    is NumberSetting -> ElementSlider(this, setting)
                    is SelectorSetting -> ElementSelector(this, setting)
                    is StringSetting -> ElementTextField(this, setting)
                    is ColorSetting -> ElementColor(this, setting)
                    is ActionSetting -> ElementAction(this, setting)
                    is DualSetting -> ElementDual(this, setting)
                    is HudSetting -> ElementHud(this, setting)
                    else -> return@addElement
                }
                menuElements.add(position, newElement)
            } else {
                menuElements.removeAll {
                    it.setting === setting
                }
            }
        }
    }

    fun draw(nvg: NVG): Float {
        var offs = height

        hoverHandler.handle(x, y, width, height)

        nvg {
            /*
            val percent = hoverHandler.percent()
            if (percent > 50) {
                val bounds = nanoVGHelper.getWrappedStringBounds(this.instance, module.description, 200f, 14f, Fonts.REGULAR)
                drawRoundedRect(x + width + 10f, y, bounds[2] - bounds[0] + 10, bounds[3] - bounds[1] + 8, 5f, Color(buttonColor, (percent - 30) / 100f).rgba)
                drawWrappedString(module.description, x + width + 17f, y + 12f, 200f, -1, 14f, 1f, Fonts.REGULAR)
            }
             */
            rect(x, y, width, height, color)
            text(module.name, x + width / 2, y + height / 2, textColor, 18f, Fonts.MEDIUM, TextAlign.Middle)

            if (!extendAnim.isAnimating() && !extended || menuElements.isEmpty()) return@nvg

            var drawY = offs
            offs = height + floor(extendAnim.get(0f, getSettingHeight(), !extended))

            val scissor = scissor(x, y, width, offs)
            for (i in 0 until menuElements.size) {
                menuElements[i].y = drawY
                drawY += menuElements[i].render(nvg)
            }
            resetScissor(scissor)
        }
        return offs
    }

    fun mouseClicked(mouseButton: Int): Boolean {
        if (isButtonHovered) {
            if (mouseButton == 0) {
                if (colorAnim.start()) module.toggle()
                return true
            } else if (mouseButton == 1) {
                if (menuElements.size > 0) {
                    extendAnim.start()
                    extended = !extended
                    if (!extended) {
                        menuElements.forEach {
                            it.listening = false
                        }
                    }
                }
                return true
            }
        } else if (isMouseUnderButton) {
            for (menuElement in menuElements.reversed()) {
                if (menuElement.mouseClicked(mouseButton)) {
                    updateElements()
                    return true
                }
            }
        }
        return false
    }

    fun mouseReleased(state: Int) {
        if (extended) {
            for (menuElement in menuElements.reversed()) {
                menuElement.mouseReleased(state)
            }
        }
    }

    fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        if (extended) {
            for (menuElement in menuElements.reversed()) {
                if (menuElement.keyTyped(typedChar, keyCode)) return true
            }
        }
        return false
    }

    val isButtonHovered
        get() = isAreaHovered(x, y, width, height - 1)

    private val isMouseUnderButton
        get() = extended && isAreaHovered(x, y + height, width)

    private fun getSettingHeight(): Float {
        var totalHeight = 0f
        for (i in menuElements) totalHeight += i.h
        return totalHeight
    }
}