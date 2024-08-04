package com.github.stivais.ui.elements.impl

import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.constraints.Type
import com.github.stivais.ui.constraints.measurements.Undefined
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.utils.loop

class Grid(
    constraints: Constraints?
) : Element(constraints) {

    val position = hashSetOf<Element>()

    override fun onElementAdded(element: Element) {
        val constraints = element.constraints
        if (constraints.x is Undefined && constraints.y is Undefined) {
            position.add(element)
        }
    }

    override fun positionChildren() {
        if (!enabled) return

        var currX = 0f
        var currY = 0f
        elements?.loop {
            if (position.contains(it)) {
                if (currX + it.width > width) {
                    currX = 0f
                    currY += it.height
                }
                it.position(x + currX, y + currY)
                currX += it.width
            } else {
                it.position(x, y)
            }
            it.positionChildren()
        }

        if (constraints.width.reliesOnChild()) width = constraints.width.get(this, Type.W)
        if (constraints.height.reliesOnChild()) height = constraints.height.get(this, Type.H) + sy
    }

    override fun draw() {
        renderer.hollowRect(x, y, width, height, 1f, Color.WHITE.rgba)
    }
}