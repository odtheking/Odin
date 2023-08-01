package me.odinclient.ui.clickgui.elements

import cc.polyfrost.oneconfig.renderer.font.Fonts
import cc.polyfrost.oneconfig.utils.dsl.*
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.*
import me.odinclient.ui.clickgui.Panel
import me.odinclient.ui.clickgui.elements.menu.*
import me.odinclient.ui.clickgui.util.ColorUtil
import me.odinclient.ui.clickgui.util.ColorUtil.buttonColor
import me.odinclient.ui.clickgui.util.HoverHandler
import me.odinclient.utils.render.Color
import me.odinclient.utils.render.gui.GuiUtils.drawCustomCenteredText
import me.odinclient.utils.render.gui.GuiUtils.nanoVG
import me.odinclient.utils.render.gui.GuiUtils.scissor
import me.odinclient.utils.render.gui.MouseUtils.isAreaHovered
import me.odinclient.utils.render.gui.animations.impl.EaseInOut
import kotlin.math.floor

class ModuleButton(val module: Module, val panel: Panel) {

    val menuElements: ArrayList<Element<*>> = ArrayList()

    inline val x: Float
        get() = panel.x

    var y: Float = 0f
        get() = field + panel.y

    val width = Panel.width
    val height = 32f

    var extended = false
    private val extendAnim = EaseInOut(250)

    private val hoverHandler = HoverHandler(2000)

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

    fun draw(vg: VG): Float {
        var offs = height

        hoverHandler.handle(x, y, width, height)

        vg.nanoVG {
            val percent = hoverHandler.percent()
            if (percent > 70) {
                val bounds = nanoVGHelper.getWrappedStringBounds(this.instance, module.description, 200f, 14f, Fonts.REGULAR)
                drawRoundedRect(x + width + 10f, y, bounds[2] - bounds[0] + 10, bounds[3] - bounds[1] + 8, 5f, Color(buttonColor, percent / 100f).rgba)
                drawWrappedString(module.description, x + width + 17f, y + 12f, 200f, -1, 14f, 1f, Fonts.REGULAR)
            }

            if (module.enabled) drawRect(x, y, width, offs, ColorUtil.clickGUIColor.rgba)
            else drawRect(x, y, width, offs, ColorUtil.moduleColor(module.enabled))

            if (isButtonHovered) drawRect(x, y, width, offs, if (module.enabled) 0x55111111 else ColorUtil.hoverColor)

            drawCustomCenteredText(module.name, x + width / 2, y + height / 2, 18f, Fonts.MEDIUM)

            if (!extendAnim.isAnimating() && !extended || menuElements.isEmpty()) return@nanoVG

            var drawY = offs
            offs = height + floor(extendAnim.get(0f, getSettingHeight(), !extended))

            scissor(x, y, width, offs) {
                for (menuElement in menuElements) {
                    menuElement.y = drawY
                    drawY += menuElement.render(vg)
                }
            }
        }
        return offs
    }

    fun mouseClicked(mouseButton: Int): Boolean {
        if (isButtonHovered) {

            if (mouseButton == 0) {
                module.toggle()
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
        get() = isAreaHovered(x, y, width, height)

    private val isMouseUnderButton
        get() = extended && isAreaHovered(x, y + height, width)

    private fun getSettingHeight(): Float {
        var totalHeight = 0f
        for (i in menuElements) totalHeight += i.height
        return totalHeight
    }
}