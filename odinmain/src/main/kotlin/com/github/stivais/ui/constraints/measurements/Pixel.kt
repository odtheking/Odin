package com.github.stivais.ui.constraints.measurements

import com.github.stivais.ui.constraints.Constraint.Companion.HORIZONTAL
import com.github.stivais.ui.constraints.Measurement
import com.github.stivais.ui.constraints.Type
import com.github.stivais.ui.elements.Element

open class Pixel(var pixels: Float): Measurement {

    override fun get(element: Element, type: Type): Float = pixels

    operator fun unaryMinus(): LeftPixel = LeftPixel(pixels)
}

// todo: find a better name
class LeftPixel(pixels: Float): Pixel(pixels) {
    override fun get(element: Element, type: Type): Float {
        return if (type.axis == HORIZONTAL) {
            (element.parent?.width ?: 0f) - (if (type.isPosition) element.width else element.internalX) - pixels
        } else {
            (element.parent?.height ?: 0f) - (if (type.isPosition) element.height else element.internalY) - pixels
        }
    }
}
