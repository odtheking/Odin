package com.github.stivais.ui.constraints

import com.github.stivais.ui.constraints.measurements.LeftPixel
import com.github.stivais.ui.constraints.measurements.Percent
import com.github.stivais.ui.constraints.measurements.Pixel
import com.github.stivais.ui.constraints.measurements.Undefined
import com.github.stivais.ui.constraints.operational.Additive
import com.github.stivais.ui.constraints.operational.Subtractive
import com.github.stivais.ui.constraints.positions.Center
import com.github.stivais.ui.constraints.sizes.Copying

// A lot of options depending on preferences

fun constrain(
    x: Position = Undefined,
    y: Position = Undefined,
    w: Size = Undefined,
    h: Size = Undefined
) = Constraints(x, y, w, h)

fun c(x: Position, y: Position, w: Size, h: Size) = Constraints(x, y, w, h)

fun at(x: Position = Undefined, y: Position = Undefined) = Constraints(x, y, Undefined, Undefined)

fun size(w: Size, h: Size) = Constraints(Undefined, Undefined, w, h)

fun x(x: Position) = at(x, Undefined)

fun Position.toX() = x(this)

fun y(y: Position) = at(Undefined, y)

fun Position.toY() = y(this)

fun width(w: Size) = size(w, Undefined)

fun w(w: Size) = width(w)

fun Size.toWidth() = width(this)

fun height(h: Size) = size(Undefined, h)

fun h(h: Size) = height(h)

fun Size.toHeight() = height(this)


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

fun center(): Constraints = Constraints(Center, Center, Undefined, Undefined)

fun copyParent(indent: Number = 0f): Constraints {
    if (indent == 0f) {
        return Constraints(0.px, 0.px, Copying, Copying)
    }
    val px = indent.px
    return Constraints(px, px, -px, -px)
}

operator fun Constraint.plus(other: Constraint) = Additive(this, other)

operator fun Constraint.minus(other: Constraint) = Subtractive(this, other)
