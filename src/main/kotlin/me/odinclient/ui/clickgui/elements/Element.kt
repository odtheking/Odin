package me.odinclient.ui.clickgui.elements

import cc.polyfrost.oneconfig.utils.dsl.VG
import me.odinclient.utils.render.gui.MouseUtils.isAreaHovered
import me.odinclient.features.settings.Setting

open class Element<S : Setting<*>>(val parent: ModuleButton, val setting: S, type: ElementType) {

    val width = parent.width
    var height: Float = when (type) {
        ElementType.SLIDER -> 38f
        else -> DEFAULT_HEIGHT
    }

    val displayName: String = setting.name
    var extended = false
    var listening = false

    inline val x: Float
        get() = parent.x

    var y: Float = 0f
        get() = field + parent.y

    open val isHovered
        get() = isAreaHovered(x, y, width, height)

    open fun render(vg: VG): Float {
        draw(vg)
        return height
    }

    protected open fun draw(vg: VG) {}

    open fun mouseClicked(mouseButton: Int): Boolean = isAreaHovered(x, y, width, height)
    open fun mouseReleased(state: Int) {}

    open fun keyTyped(typedChar: Char, keyCode: Int): Boolean = false

    companion object {
        const val DEFAULT_HEIGHT = 32f
    }
}