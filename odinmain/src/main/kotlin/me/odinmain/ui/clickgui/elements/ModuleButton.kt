package me.odinmain.ui.clickgui.elements

import me.odinmain.features.Module
import me.odinmain.features.impl.render.ClickGUIModule
import me.odinmain.features.settings.impl.*
import me.odinmain.ui.clickgui.ClickGUI
import me.odinmain.ui.clickgui.Panel
import me.odinmain.ui.clickgui.elements.menu.*
import me.odinmain.ui.clickgui.util.ColorUtil.brighter
import me.odinmain.ui.clickgui.util.ColorUtil.clickGUIColor
import me.odinmain.ui.clickgui.util.ColorUtil.moduleButtonColor
import me.odinmain.ui.clickgui.util.ColorUtil.textColor
import me.odinmain.ui.clickgui.util.HoverHandler
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.gui.*
import me.odinmain.utils.render.gui.MouseUtils.isAreaHovered
import me.odinmain.utils.render.gui.animations.impl.ColorAnimation
import me.odinmain.utils.render.gui.animations.impl.EaseInOut
import kotlin.math.floor

/**
 * Renders all the modules.
 *
 * Backend made by Aton, with some changes
 * Design mostly made by Stivais
 *
 * @author Stivais, Aton
 * @see [Element]
 */
class ModuleButton(val module: Module, val panel: Panel) {

    val menuElements: ArrayList<Element<*>> = ArrayList()

    val x: Float
        inline get() = panel.x

    var y: Float = 0f
        get() = field + panel.y

    private val colorAnim = ColorAnimation(150)

    val color: Color
        get() = colorAnim.get(clickGUIColor, moduleButtonColor, module.enabled).brighter(1 + hover.percent() / 500f)

    val width = Panel.width
    val height = 32f

    var extended = false

    private val extendAnim = EaseInOut(250)
    private val hoverHandler = HoverHandler(1000, 200)
    private val hover = HoverHandler(250)

    init {
        updateElements()
        if (module.keyCode != -999) menuElements.add(ElementKeyBind(this, module))
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

    fun draw(): Float {
        var offs = height

        hoverHandler.handle(x, y, width, height - 1)
        hover.handle(x, y, width, height - 1)

        if (hoverHandler.percent() > 0) {
            ClickGUI.setDescription(module.description, x + width + 10f, y, hoverHandler)
        }


        roundedRectangle(x, y, width, height, color)
            text(module.name, x + width / 2, y + height / 2, textColor, 18f, Fonts.REGULAR, TextAlign.Middle)
            val textWidth = getTextWidth(module.name, 18f, Fonts.REGULAR)

            if (textWidth > width - 80)// too long text, not drawing symbol
            else if (module.tag == Module.TagType.RISKY) {
                /*NanoVGHelper.INSTANCE.drawSvg(this.context,
                    "/assets/odinmain/ui/clickgui/bannableIcon.svg", x + width / 2 + textWidth / 2 + 10f, y + 4f, 25f, 25f, javaClass
                )

                 */
            } else if (module.tag == Module.TagType.FPSTAX) {
                /*NanoVGHelper.INSTANCE.drawSvg(this.context,
                    "/assets/odinmain/ui/clickgui/fpsHeavyIcon.svg", x + width / 2 + textWidth / 2 + 20f, y, 35f, 35f, javaClass
                )

                 */
            } else if (module.tag == Module.TagType.NEW && ClickGUIModule.firstTimeOnVersion) {
                /*NanoVGHelper.INSTANCE.drawSvg(this.context,
                    "/assets/odinmain/ui/clickgui/newFeatureIcon.svg", x + width / 2 + textWidth / 2 + 10f, y, 35f, 35f, javaClass
                )

                 */
            }


            if (!extendAnim.isAnimating() && !extended || menuElements.isEmpty()) return offs

            var drawY = offs
            offs = height + floor(extendAnim.get(0f, getSettingHeight(), !extended))

            //val scissor = scissor(x, y, width, offs)
            for (i in 0 until menuElements.size) {
                menuElements[i].y = drawY
                drawY += menuElements[i].render()
            }
            //resetScissor(scissor)

        return offs
    }

    fun mouseClicked(mouseButton: Int): Boolean {
        if (isButtonHovered) {
            if (mouseButton == 0) {
                if (colorAnim.start()) module.toggle()
                return true
            } else if (mouseButton == 1) {
                if (menuElements.size > 0) {
                    if (extendAnim.start()) extended = !extended
                    if (!extended) {
                        menuElements.forEach {
                            it.listening = false
                        }
                    }
                }
                return true
            }
        } else if (isMouseUnderButton) {
            for (i in menuElements.size - 1 downTo 0) {
                if (menuElements[i].mouseClicked(mouseButton)) {
                    updateElements()
                    return true
                }
            }
        }
        return false
    }

    fun mouseReleased(state: Int) {
        if (extended) {
            for (i in menuElements.size - 1 downTo 0) {
                menuElements[i].mouseReleased(state)
            }
        }
    }

    fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        if (extended) {
            for (i in menuElements.size - 1 downTo 0) {
                if (menuElements[i].keyTyped(typedChar, keyCode)) return true
            }
        }
        return false
    }

    private val isButtonHovered: Boolean
        get() = isAreaHovered(x, y, width, height - 1)

    private val isMouseUnderButton: Boolean
        get() = extended && isAreaHovered(x, y + height, width)

    private fun getSettingHeight(): Float {
        var totalHeight = 0f
        for (i in menuElements) totalHeight += i.h
        return totalHeight
    }
}