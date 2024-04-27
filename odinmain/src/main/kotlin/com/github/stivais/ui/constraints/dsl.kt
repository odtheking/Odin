package com.github.stivais.ui.constraints

import com.github.stivais.ui.constraints.measurements.LeftPixel
import com.github.stivais.ui.constraints.measurements.Percent
import com.github.stivais.ui.constraints.measurements.Pixel
import com.github.stivais.ui.constraints.measurements.Undefined
import com.github.stivais.ui.constraints.operational.Additive
import com.github.stivais.ui.constraints.operational.Subtractive
import com.github.stivais.ui.constraints.positions.Center

// A lot of options depending on preferences

fun constrain(x: Position, y: Position, w: Size, h: Size) = Constraints(x, y, w, h)

fun c(x: Position, y: Position, w: Size, h: Size) = Constraints(x, y, w, h)

fun at(x: Position, y: Position) = Constraints(x, y, Undefined, Undefined)

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

// todo: check if indent is 0 and make it uses copying() and also make it an object to reduce amount of initialized classes
fun copyParent(indent: Number = 0f): Constraints {
    val px = indent.px
    return Constraints(px, px, -px, -px)
}

operator fun Constraint.plus(other: Constraint) = Additive(this, other)

operator fun Constraint.minus(other: Constraint) = Subtractive(this, other)
