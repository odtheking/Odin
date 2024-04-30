package com.github.stivais.ui.elements.impl

import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.constraints.Measurement
import com.github.stivais.ui.constraints.measurements.Pixel
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.utils.replaceUndefined

// todo: Text width doesn't update when height updates
class Text(
    text: String,
    textColor: Color,
    constraints: Constraints?,
    val size: Measurement
) : Element(constraints.replaceUndefined(w = 0.px, h = size)) {

    var text: String = text
        set(value) {
            if (field == value) return
            field = value
            needsUpdate = true
//            (constraints.width as Pixel).pixels = renderer.textWidth(value, height)
        }

    private var needsUpdate = true

    init {
        this.color = textColor

        onInitialization {
//            // needs size to be set
//            parent?.position()
//            val width = renderer.textWidth(text, height)
//            (this.constraints.width as Pixel).pixels = width
//            this.width = width
        }
    }

    override fun draw() {
        if (needsUpdate) {
            parent?.position() // sub optimal
            (constraints.width as Pixel).pixels = renderer.textWidth(text, height)
            needsUpdate = false
        }
        renderer.text(text, x, y, height, color!!.rgba)
    }
}