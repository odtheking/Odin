package me.odinclient.ui.clickgui.elements

import me.odinclient.features.settings.Setting
import me.odinclient.utils.render.gui.MouseUtils.isAreaHovered
import me.odinclient.utils.render.gui.nvg.NVG

open class Element<S : Setting<*>>(val parent: ModuleButton, val setting: S, type: ElementType) {

    inline val name: String
        get () = setting.name

    val w: Float
        inline get() = parent.width

    var h: Float = when (type) {
        ElementType.SLIDER -> 40f
        else -> DEFAULT_HEIGHT
    }

    var extended = false
    var listening = false

    val x: Float
        inline get() = parent.x

    var y: Float = 0f
        get() = field + parent.y

    open val isHovered
        get() = isAreaHovered(x, y, w, h)

    open fun render(nvg: NVG): Float {
        draw(nvg)
        return h
    }

    protected open fun draw(nvg: NVG) {}

    open fun mouseClicked(mouseButton: Int): Boolean = isAreaHovered(x, y, w, h)
    open fun mouseReleased(state: Int) {}

    open fun keyTyped(typedChar: Char, keyCode: Int): Boolean = false

    companion object {
        const val DEFAULT_HEIGHT = 32f
    }
}