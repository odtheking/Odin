package com.github.stivais.ui.constraints.positions

import com.github.stivais.ui.constraints.Constraint.Companion.HORIZONTAL
import com.github.stivais.ui.constraints.Position
import com.github.stivais.ui.constraints.Type
import com.github.stivais.ui.elements.Element

object Center : Position {
    override fun get(element: Element, type: Type): Float {
        return if (type.axis == HORIZONTAL) (element.parent?.width ?: 0f) / 2f - element.width / 2f
        else (element.parent?.height ?: 0f) / 2f - element.height / 2f
    }
}