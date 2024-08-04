package com.github.stivais.ui.constraints.positions

import com.github.stivais.ui.constraints.Constraint.Companion.HORIZONTAL
import com.github.stivais.ui.constraints.Position
import com.github.stivais.ui.constraints.Type
import com.github.stivais.ui.elements.Element

class Linked(val element: Element, private val previous: Linked?) : Position {
    override fun get(element: Element, type: Type): Float {
        val link = getNextValid(previous)?.element ?: return 0f
        return if (type.axis == HORIZONTAL) {
            (link.x - (link.parent?.x ?: 0f)) + link.width
        } else {
            (link.y - (link.parent?.y ?: 0f)) + link.height
        }
    }

    private fun getNextValid(previous: Linked?): Linked? {
        if (previous != null) {
            return if (previous.element.enabled) previous else getNextValid(previous.previous)
        }
        return null
    }
}