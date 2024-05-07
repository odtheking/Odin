package com.github.stivais.ui.constraints.measurements

import com.github.stivais.ui.constraints.Constraint.Companion.HORIZONTAL
import com.github.stivais.ui.constraints.Measurement
import com.github.stivais.ui.constraints.Type
import com.github.stivais.ui.elements.Element

class Percent(percent: Float) : Measurement {

    private var percent = percent
        set(value) {
            field = value.coerceIn(0f, 1f)
        }

    override fun get(element: Element, type: Type): Float {
        return (if (type.axis == HORIZONTAL) element.parent!!.width else element.parent!!.height) * percent
    }
}