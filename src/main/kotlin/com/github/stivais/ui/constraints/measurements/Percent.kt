package com.github.stivais.ui.constraints.measurements

import com.github.stivais.ui.constraints.Constraint.Companion.HORIZONTAL
import com.github.stivais.ui.constraints.Measurement
import com.github.stivais.ui.constraints.Type
import com.github.stivais.ui.elements.Element

class Percent(var percent: Float) : Measurement {

    override fun get(element: Element, type: Type): Float {
        return (if (type.axis == HORIZONTAL) element.parent!!.width else element.parent!!.height) * percent
    }
}