package me.odinclient.clickgui.elements

import cc.polyfrost.oneconfig.config.core.OneColor
import cc.polyfrost.oneconfig.utils.dsl.VG
import me.odinclient.features.settings.Setting
import me.odinclient.features.settings.impl.ColorSetting
import me.odinclient.features.settings.impl.SelectorSetting
import me.odinclient.clickgui.ClickGUI
import me.odinclient.clickgui.util.MouseUtils.isAreaHovered
import me.odinclient.utils.render.HUDRenderUtils.startDraw

open class Element<S : Setting<*>>(private val parent: ModuleButton, val setting: S, private val type: ElementType) {
    val clickgui: ClickGUI = parent.panel.clickgui

    var offset = 0
    val width = parent.width
    var height: Int

    var displayName: String = setting.name
    var extended = false
    var listening = false

    val x: Int
        get() = parent.xAbsolute

    val y: Int
        get() = offset + parent.y + parent.panel.y

    val isHovered
        get() = isAreaHovered(x, y, width, height)

    init {
        height = when (type) {
            ElementType.TEXT_FIELD -> 12
            ElementType.SLIDER -> 18
            else -> DEFAULT_HEIGHT
        }
    }

    fun update() {
        displayName = setting.name
        when (type) {
            ElementType.SELECTOR -> {
                height = if (extended)
                        (((setting as? SelectorSetting)?.options?.size ?: (setting as SelectorSetting).options.size) * (16 + 2) + DEFAULT_HEIGHT)
                else DEFAULT_HEIGHT
            }

            ElementType.COLOR -> {
                height = if (extended)
                    if ((setting as ColorSetting).allowAlpha)
                        DEFAULT_HEIGHT * 5
                    else
                        DEFAULT_HEIGHT * 4
                else 18
            }

            else -> {}
        }
    }

    fun drawScreen(partialTicks: Float, vg: VG): Int {
        vg.startDraw(x, y, width, height) { drawRect(color = OneColor(38, 38, 38, 178).rgb) }
        renderElement(partialTicks, vg)
        return height
    }

    protected open fun renderElement(partialTicks: Float, vg: VG) {}

    open fun mouseClicked(mouseButton: Int): Boolean = isHovered

    open fun mouseReleased(state: Int) {}

    open fun keyTyped(typedChar: Char, keyCode: Int): Boolean {
        return false
    }

    companion object {
        const val DEFAULT_HEIGHT = 15
    }
}