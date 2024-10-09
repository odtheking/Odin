package me.odinmain.utils.ui

import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.constraints.positions.Linked
import com.github.stivais.ui.elements.impl.TextScope
import com.github.stivais.ui.elements.scope.ElementDSL

infix fun TextScope.and(other: TextScope) {
    other.element.constraints.x = Linked(element)
    other.size = size
}

fun ElementDSL.outline(constraints: Constraints, color: Color, radius: FloatArray? = null) = block(constraints, Color.TRANSPARENT, radius).outline(color)