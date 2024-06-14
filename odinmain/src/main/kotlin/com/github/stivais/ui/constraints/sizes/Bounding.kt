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
            // maybe improve later
            var ignoreIfCopying = false
            val new = if (type.axis == HORIZONTAL) {
                if (child.constraints.width is Copying) ignoreIfCopying = true
                child.internalX + child.width
            } else {
                if (child.constraints.height is Copying) ignoreIfCopying = true
                child.internalY + child.height
            }
            if (new > value && !ignoreIfCopying) value = new
        }
        return value
    }

    override fun reliesOnChild(): Boolean {
        return true
    }
}