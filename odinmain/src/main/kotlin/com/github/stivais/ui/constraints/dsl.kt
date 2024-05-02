package com.github.stivais.ui.constraints

import com.github.stivais.ui.constraints.measurements.LeftPixel
import com.github.stivais.ui.constraints.measurements.Percent
import com.github.stivais.ui.constraints.measurements.Pixel
import com.github.stivais.ui.constraints.measurements.Undefined
import com.github.stivais.ui.constraints.operational.Additive
import com.github.stivais.ui.constraints.operational.Subtractive
import com.github.stivais.ui.constraints.positions.Center
import com.github.stivais.ui.constraints.sizes.Copying

fun constrain(
    x: Position = Undefined,
    y: Position = Undefined,
    w: Size = Undefined,
    h: Size = Undefined
) = Constraints(x, y, w, h)

fun at(x: Position = Undefined, y: Position = Undefined) = Constraints(x, y, Undefined, Undefined)

fun size(w: Size = Undefined, h: Size = Undefined) = Constraints(Undefined, Undefined, w, h)

val Number.px: Pixel
    get() {
        val value = this.toFloat()
        return if (value < 0) LeftPixel(value) else Pixel(value)
    }

val Number.percent: Percent
    get() {
        val value = this.toFloat() / 100f
        return Percent(value)
    }

fun center(): Constraints = at(Center, Center)

fun copyParent(indent: Number = 0f): Constraints {
    if (indent == 0f) {
        return Constraints(0.px, 0.px, Copying, Copying)
    }
    val px = indent.px
    return Constraints(px, px, -px, -px)
}

operator fun Constraint.plus(other: Constraint) = Additive(this, other)

operator fun Constraint.minus(other: Constraint) = Subtractive(this, other)
