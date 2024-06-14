package com.github.stivais.ui.constraints.positions

import com.github.stivais.ui.constraints.Constraint.Companion.HORIZONTAL
import com.github.stivais.ui.constraints.Position
import com.github.stivais.ui.constraints.Type
import com.github.stivais.ui.elements.Element

class Linked(private val previous: Linked?, val element: Element) : Position {
    override fun get(element: Element, type: Type): Float {
        val link = getNextValid(previous)?.element ?: return 0f
        return if (type.axis == HORIZONTAL) link.internalX + link.width else link.internalY + link.height
    }

    private fun getNextValid(previous: Linked?): Linked? {
        if (previous != null) {
            return if (previous.element.renders) previous else getNextValid(previous.previous)
        }
        return null
    }
}