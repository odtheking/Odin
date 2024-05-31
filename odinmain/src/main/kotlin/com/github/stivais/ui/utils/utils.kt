package com.github.stivais.ui.utils

import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.constraints.Position
import com.github.stivais.ui.constraints.Size
import com.github.stivais.ui.constraints.measurements.Undefined

fun getRGBA(red: Int, green: Int, blue: Int, alpha: Int): Int {
    return ((alpha shl 24) and 0xFF000000.toInt()) or ((red shl 16) and 0x00FF0000) or ((green shl 8) and 0x0000FF00) or (blue and 0x000000FF)
}

fun Constraints?.replaceUndefined(
    x: Position = Undefined,
    y: Position = Undefined,
    w: Size = Undefined,
    h: Size = Undefined
): Constraints {
    if (this == null) return Constraints(x, y, w, h)
    return this.apply {
        if (this.x is Undefined) this.x = x
        if (this.y is Undefined) this.y = y
        if (this.width is Undefined) this.width = w
        if (this.height is Undefined) this.height = h
    }
}

// using this with an arraylist is just as fast as an iterator, but is more memory efficient
inline fun <E> ArrayList<E>.loop(block: (E) -> Unit) {
    if (this.size == 0) return
    for (i in 0..<this.size) {
        block(this[i])
    }
}

/**
 * Multiplies a number by 1_000_000_000 to match nanoseconds
 */
val Number.seconds
    get() = this.toFloat() * 1_000_000_000