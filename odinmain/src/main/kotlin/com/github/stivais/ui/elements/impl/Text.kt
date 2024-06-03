package com.github.stivais.ui.elements.impl

import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.constraints.measurements.Pixel
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.elements.scope.ElementScope
import com.github.stivais.ui.utils.replaceUndefined

open class Text(
    text: String,
    textColor: Color,
    constraints: Constraints?,
    size: Measurement
) : Element(constraints.replaceUndefined(w = 0.px, h = size), textColor) {

    var text: String = text
        set(value) {
            if (field == value) return
            field = value
            previousHeight = 0f // forces recalculation
        }

    // uses to check if width should be recalculated as it is expensive to do so
    private var previousHeight = 0f

    override fun prePosition() {
        if (!renders) return
        height = constraints.height.get(this, Type.H)
        if (previousHeight != height) {
            previousHeight = height
            val newWidth =  renderer.textWidth(text, height)
            (constraints.width as Pixel).pixels = newWidth
        }
    }

    override fun draw() {
        renderer.text(text, x, y, height, color!!.get(this))
    }
}

class TextScope(text: Text) : ElementScope<Text>(text) {
    var string: String
        get() = element.text
        set(value) {
            element.text = value
        }
}
