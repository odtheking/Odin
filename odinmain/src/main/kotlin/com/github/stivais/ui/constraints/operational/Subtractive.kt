package com.github.stivais.ui.constraints.operational

import com.github.stivais.ui.constraints.Constraint
import com.github.stivais.ui.constraints.Measurement
import com.github.stivais.ui.constraints.Type
import com.github.stivais.ui.elements.Element

class Subtractive(val first: Constraint, val second: Constraint) : Measurement {
    override fun get(element: Element, type: Type): Float = first.get(element, type) - second.get(element, type)
}