package me.odinmain.utils.ui

import com.github.stivais.ui.constraints.positions.Linked
import com.github.stivais.ui.elements.impl.TextScope

infix fun TextScope.and(other: TextScope) {
    other.element.constraints.x = Linked(element)
    other.size = size
}