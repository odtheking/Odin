package com.github.stivais.ui.constraints.sizes

import com.github.stivais.ui.constraints.Constraint.Companion.HORIZONTAL
import com.github.stivais.ui.constraints.Size
import com.github.stivais.ui.constraints.Type
import com.github.stivais.ui.elements.Element

class Indent(val amount: Float): Size {

    override fun get(element: Element, type: Type): Float {
        return if (type.axis == HORIZONTAL) {
            (element.parent?.width ?: 0f) - (element.x - (element.parent?.x ?: 0f)) - amount
        } else {
            (element.parent?.height ?: 0f) - (element.y - (element.parent?.y ?: 0f)) - amount
        }
    }
}