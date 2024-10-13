package com.github.stivais.ui.constraints

import com.github.stivais.ui.constraints.measurements.Percent
import com.github.stivais.ui.constraints.measurements.Pixel
import com.github.stivais.ui.constraints.measurements.Undefined
import com.github.stivais.ui.constraints.operational.Additive
import com.github.stivais.ui.constraints.operational.CoerceMaxOld
import com.github.stivais.ui.constraints.operational.Multiplicative
import com.github.stivais.ui.constraints.operational.Subtractive
import com.github.stivais.ui.constraints.positions.Alignment
import com.github.stivais.ui.constraints.positions.Center
import com.github.stivais.ui.constraints.sizes.Copying
import com.github.stivais.ui.constraints.sizes.Indent

fun constrain(
    x: Position = Undefined,
    y: Position = Undefined,
    w: Size = Undefined,
    h: Size = Undefined
) = Constraints(x, y, w, h)

fun at(x: Position = Undefined, y: Position = Undefined) = Positions(x, y)

fun size(w: Size = Undefined, h: Size = Undefined) = Constraints(Undefined, Undefined, w, h)

val Number.px: Pixel
    get() {
        val value = this.toFloat()
        return Pixel(value)
    }

val Number.percent: Percent
    get() {
        val value = this.toFloat() / 100f
        return Percent(value)
    }

val Number.indent
    get() = Indent(this.toFloat())

val Position.center
    get() = Alignment.Center(this)

val Position.alignRight
    get() = Alignment.Right(this)

fun center(): Constraints = at(Center, Center)

fun copies(indent: Number = 0f): Constraints {
    if (indent == 0f) {
        return Constraints(0.px, 0.px, Copying, Copying)
    }
    return Constraints(indent.px, indent.px, -(indent.px), -(indent.px))
}

fun indent(amount: Number): Constraints {
    val indent = amount.indent
    return size(indent, indent)
}

operator fun Position.plus(other: Position?) = if (other == null) this else Additive(this, other)

operator fun Size.plus(other: Size?) = if (other == null) this else Additive(this, other)

operator fun Constraint.minus(other: Constraint) = Subtractive(this, other)

operator fun Constraint.times(other: Constraint) = Multiplicative(this, other)

fun Constraint.coerce(max: Float) = CoerceMaxOld(max, this)
