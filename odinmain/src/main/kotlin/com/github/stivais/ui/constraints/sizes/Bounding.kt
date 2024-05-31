package com.github.stivais.ui.constraints.sizes

import com.github.stivais.ui.constraints.Constraint.Companion.HORIZONTAL
import com.github.stivais.ui.constraints.Size
import com.github.stivais.ui.constraints.Type
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.utils.loop

object Bounding : Size {

    override fun get(element: Element, type: Type): Float {
        if (element.elements == null) return 0f
        var value = 0f
        element.elements!!.loop { child ->
            if (!child.enabled) return@loop
            val new = if (type.axis == HORIZONTAL) child.internalX + child.width else child.internalY + child.height
            if (new > value) value = new
        }
        return value
    }

    override fun reliesOnChild(): Boolean {
        return true
    }
}