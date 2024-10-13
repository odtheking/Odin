package com.github.stivais.ui.constraints.sizes

import com.github.stivais.ui.constraints.Constraint.Companion.HORIZONTAL
import com.github.stivais.ui.constraints.Size
import com.github.stivais.ui.constraints.Type
import com.github.stivais.ui.elements.Element

class Aspect(private val ratio: Float) : Size {
    override fun get(element: Element, type: Type): Float {
        return if (type.axis == HORIZONTAL) element.height * ratio else element.width / ratio
    }
}