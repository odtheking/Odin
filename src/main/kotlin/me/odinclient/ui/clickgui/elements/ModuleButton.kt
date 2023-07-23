package me.odinclient.ui.clickgui.elements

import cc.polyfrost.oneconfig.renderer.font.Fonts
import cc.polyfrost.oneconfig.utils.dsl.VG
import cc.polyfrost.oneconfig.utils.dsl.drawRect
import me.odinclient.ui.clickgui.Panel
import me.odinclient.ui.clickgui.elements.menu.*
import me.odinclient.ui.clickgui.util.ColorUtil
import me.odinclient.utils.gui.MouseUtils.isAreaHovered
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.*
import me.odinclient.utils.gui.GuiUtils.drawCustomCenteredText
import me.odinclient.utils.gui.GuiUtils.nanoVG
import me.odinclient.utils.gui.GuiUtils.scissor
import me.odinclient.utils.gui.animations.impl.EaseInOut
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

    init {
        updateElements()
        if (module.keyCode != -999) menuElements.add(ElementKeyBind(this, module))
        menuElements.add(ElementDescription(this, module.description))
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
                    is DualSetting -> ElementDual(this, setting)
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

    fun draw(vg: VG): Float {
        var offs = height

        vg.nanoVG {
            if (module.enabled) drawRect(x, y, width, offs, ColorUtil.clickGUIColor.rgb)
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

    private val isButtonHovered
        get() = isAreaHovered(x, y, width, height)

    private val isMouseUnderButton
        get() = extended && isAreaHovered(x, y + height, width)

    private fun getSettingHeight(): Float {
        var totalHeight = 0f
        for (i in menuElements) totalHeight += i.height
        return totalHeight
    }
}