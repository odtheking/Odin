package com.github.stivais.ui.constraints.operational

import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.elements.Element

class Additive(val first: Constraint, val second: Constraint) : Measurement {
    override fun get(element: Element, type: Type): Float = first.get(element, type) + second.get(element, type)

    override fun reliesOnChild(): Boolean {
        return first.reliesOnChild() || second.reliesOnChild()
    }
}