package com.github.stivais.ui.constraints.measurements

import com.github.stivais.ui.constraints.Constraint.Companion.HORIZONTAL
import com.github.stivais.ui.constraints.Measurement
import com.github.stivais.ui.constraints.Position
import com.github.stivais.ui.constraints.Size
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

class Test(val c: Position): Position {
    override fun get(element: Element, type: Type): Float {
        return c.get(element, type) - (if (type.axis == HORIZONTAL) element.width else element.height) / 2
    }
}

class Test2(val s: Size): Size {

    override fun get(element: Element, type: Type): Float {
        TODO("Not yet implemented")
    }


}

class Test3(val amount: Float): Size {

    override fun get(element: Element, type: Type): Float {
        return if (type.axis == HORIZONTAL) {
            (element.parent?.width ?: 0f) - element.internalX - amount
        } else {
            (element.parent?.height ?: 0f) - element.internalY - amount
        }
    }
}

val Number.indent
    get() = Test3(this.toFloat())

val Position.center
    get() = Test(this)

operator fun Position.not() = center

operator fun Position.unaryPlus() = center