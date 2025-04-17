package me.odinmain.utils.ui.clickgui.elements

import me.odinmain.features.settings.Setting
import me.odinmain.utils.ui.clickgui.ClickGUI
import me.odinmain.utils.ui.clickgui.util.HoverHandler
import me.odinmain.utils.ui.util.MouseUtils.isAreaHovered

/**
 * Renders all the modules.
 *
 * Backend made by Aton, with some changes
 * Design mostly made by Stivais
 *
 * @author Stivais, Aton
 * @see [Element]
 */
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

    private val hoverHandler = HoverHandler(1250, 200)

    open fun render(): Float {
        hoverHandler.handle(x, y, w, h)
        if (hoverHandler.percent() > 0) {
            ClickGUI.setDescription(setting.description, x + w + 10f, y, hoverHandler)
        }
        draw()
        return h
    }

    protected open fun draw() {}

    open fun mouseClicked(mouseButton: Int): Boolean = isAreaHovered(x, y, w, h)
    open fun mouseReleased(state: Int) {}

    open fun keyTyped(typedChar: Char, keyCode: Int): Boolean = false

    companion object {
        const val DEFAULT_HEIGHT = 32f
    }
}