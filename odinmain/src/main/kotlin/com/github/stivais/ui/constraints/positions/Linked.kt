package com.github.stivais.ui.constraints.positions

import com.github.stivais.ui.constraints.Constraint.Companion.HORIZONTAL
import com.github.stivais.ui.constraints.Position
import com.github.stivais.ui.constraints.Type
import com.github.stivais.ui.elements.Element

class Linked(val link: Element?) : Position {
    override fun get(element: Element, type: Type): Float {
        if (link == null) return 0f
        return if (type.axis == HORIZONTAL) link.internalX + link.width else link.internalY + link.height
    }
}