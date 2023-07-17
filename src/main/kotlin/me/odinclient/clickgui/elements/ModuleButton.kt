package me.odinclient.clickgui.elements

import cc.polyfrost.oneconfig.renderer.font.Fonts
import cc.polyfrost.oneconfig.utils.dsl.VG
import me.odinclient.features.Module
import me.odinclient.clickgui.Panel
import me.odinclient.clickgui.util.ColorUtil
import me.odinclient.clickgui.util.FontUtil.drawCustomCenteredText
import me.odinclient.clickgui.util.MouseUtils.isAreaHovered
import me.odinclient.clickgui.elements.menu.*
import me.odinclient.features.settings.impl.*
import me.odinclient.utils.render.HUDRenderUtils.startDraw
import me.odinclient.OdinClient.Companion.mc

class ModuleButton(val module: Module, val panel: Panel) {
    val menuElements: ArrayList<Element<*>> = ArrayList()

    var x = 0
    var y = 0

    val width = panel.width
    val height = (mc.fontRendererObj.FONT_HEIGHT + 2)
    var extended = false

    val xAbsolute: Int
        get() = x + panel.x

    private val yAbsolute: Int
        get() = y + panel.y

    init {
        updateElements()
        if (module.keyCode != -999) menuElements.add(ElementKeyBind(this, module))
    }

    fun updateElements() {
        var position = -1 // This looks weird, but it starts at -1 because it gets incremented before being used.
        for (setting in module.settings) {
            /** Don't show hidden settings */
            if (setting.shouldBeVisible && !setting.hidden) run addElement@{
                position++
                if (menuElements.any { it.setting === setting }) return@addElement
                val newElement = when (setting) {
                    is BooleanSetting -> ElementCheckBox(this, setting)
                    is NumberSetting -> ElementSlider(this, setting)
                    is SelectorSetting -> ElementSelector(this, setting)
                    is StringSetting -> ElementTextField(this, setting)
                    is ColorSetting -> ElementColor(this, setting)
                    is ActionSetting -> ElementAction(this, setting)
                    else -> return@addElement
                }
                menuElements.add(position, newElement)
            } else {
                menuElements.removeIf {
                    it.setting === setting
                }
            }
        }
    }

    fun drawScreen(partialTicks: Float, vg: VG): Int {
        var offs = height + 1

        vg.startDraw(xAbsolute, yAbsolute, width, height + 1) {
            if (extended && menuElements.isNotEmpty()) {
                for (menuElement in menuElements) {
                    menuElement.offset = offs
                    menuElement.update()

                    offs += menuElement.drawScreen(partialTicks, vg)
                }
            }

            drawRect(color = ColorUtil.moduleColor(module.enabled))

            if (isButtonHovered)
                drawRect(color = if (module.enabled) ColorUtil.boxHoverColor else ColorUtil.hoverColor)

            vg.drawCustomCenteredText(module.name, xAbsolute + width / 2, yAbsolute + height + 4, 17f, Fonts.MEDIUM, ColorUtil.textColor)
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

    private val isButtonHovered
        get() = isAreaHovered(xAbsolute, yAbsolute, width, height)

    private val isMouseUnderButton
        get() = extended && isAreaHovered(xAbsolute, yAbsolute + height, width)
}