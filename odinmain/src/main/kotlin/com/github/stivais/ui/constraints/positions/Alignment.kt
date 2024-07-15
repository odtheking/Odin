package com.github.stivais.ui.constraints.positions

import com.github.stivais.ui.constraints.Constraint.Companion.HORIZONTAL
import com.github.stivais.ui.constraints.Position
import com.github.stivais.ui.constraints.Type
import com.github.stivais.ui.elements.Element

interface Alignment : Position {

    val position: Position

    class Center(override val position: Position) : Alignment {
        override fun get(element: Element, type: Type): Float {
            return position.get(element, type) - (if (type.axis == HORIZONTAL) element.width else element.height) / 2
        }
    }

    class Right(override val position: Position) : Alignment {
        override fun get(element: Element, type: Type): Float {
            return position.get(element, type) - element.width
        }
    }
}