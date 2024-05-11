package com.github.stivais.ui.elements.impl

import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.constraints.Measurement
import com.github.stivais.ui.constraints.Type
import com.github.stivais.ui.constraints.measurements.Pixel
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.utils.replaceUndefined

class Text(
    text: String,
    textColor: Color,
    constraints: Constraints?,
    size: Measurement
) : Element(constraints.replaceUndefined(w = 0.px, h = size), textColor) {

    var text: String = text
        set(value) {
            if (field == value) return
            field = value
            previousHeight = 0f // silly way to recalculate
        }

    // uses to check if width should be recalculated as it is an expensive one
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