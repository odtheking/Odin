package com.github.stivais.ui.elements.impl

import com.github.stivais.ui.UI
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.constraints.measurements.Pixel
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.elements.scope.ElementScope
import com.github.stivais.ui.renderer.Font
import com.github.stivais.ui.utils.replaceUndefined

open class Text(
    text: String,
    val font: Font = UI.defaultFont,
    color: Color = Color.WHITE,
    constraints: Constraints? = null,
    size: Size,
) : Element(constraints.replaceUndefined(w = 0.px, h = size), color) {

    open var text: String = text
        set(value) {
            if (field == value) return
            field = value
            previousHeight = 0f // forces recalculation
        }

    // uses to check if width should be recalculated as it is expensive to do so
    protected var previousHeight = 0f

    override fun preSize() {
        height = constraints.height.get(this, Type.H)
        if (previousHeight != height) {
            previousHeight = height
            val newWidth = getTextWidth()
            (constraints.width as Pixel).pixels = newWidth
        }
    }

    open fun getTextWidth(): Float {
        return renderer.textWidth(text, height)
    }

    override fun draw() {
//        renderer.hollowRect(x, y, width, height, 1f, Color.WHITE.rgba)
        renderer.text(text, x, y, height, color!!.get(this), font)
    }

    class Supplied(
        val supplier: () -> Any?,
        font: Font,
        color: Color,
        constraints: Constraints?,
        size: Size
    ) : Text(supplier().toString(), font, color, constraints, size) {

        override fun draw() {
            text = supplier().toString()
            super.draw()
        }
    }
}

class TextScope(text: Text) : ElementScope<Text>(text) {
    var string: String
        get() = element.text
        set(value) {
            element.text = value
        }
}
