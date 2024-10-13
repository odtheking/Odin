package com.github.stivais.ui.constraints.measurements

import com.github.stivais.ui.constraints.Constraint.Companion.HORIZONTAL
import com.github.stivais.ui.constraints.Measurement
import com.github.stivais.ui.constraints.Type
import com.github.stivais.ui.elements.Element

open class Pixel(var pixels: Float): Measurement {

    override fun get(element: Element, type: Type): Float = pixels

    operator fun unaryMinus(): LeftPixel = LeftPixel(pixels)
}

// todo: make it work with any position
class LeftPixel(pixels: Float): Pixel(pixels) {
    override fun get(element: Element, type: Type): Float {
        return if (type.axis == HORIZONTAL) {
            val ix = (element.x - (element.parent?.x ?: 0f))
            (element.parent?.width ?: 0f) - (if (type.isPosition) element.width else ix) - pixels
        } else {
            val iy = (element.x - (element.parent?.x ?: 0f))
            (element.parent?.height ?: 0f) - (if (type.isPosition) element.height else iy) - pixels
        }
    }
}
