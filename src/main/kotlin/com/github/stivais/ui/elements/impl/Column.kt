package com.github.stivais.ui.elements.impl

import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.constraints.Size
import com.github.stivais.ui.constraints.Type
import com.github.stivais.ui.constraints.measurements.Undefined
import com.github.stivais.ui.constraints.plus
import com.github.stivais.ui.constraints.sizes.Bounding
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.utils.loop
import com.github.stivais.ui.utils.replaceUndefined

// rework start
class Column(
    constraints: Constraints?,
    private val paddingX: Size?,
    private val paddingY: Size?,
) : Element(constraints?.replaceUndefined(w = Bounding, h = Bounding)) {

    private val positionedElements = hashSetOf<Element>()

    init {
        this.constraints.apply {
            width += paddingX
            height += paddingY
        }
    }

    override fun onElementAdded(element: Element) {
        val constraints = element.constraints
        if (constraints.y is Undefined) {
            positionedElements.add(element)
        }
    }

    override fun positionChildren() {
        if (!enabled) return

        val px = paddingX?.get(this, Type.W) ?: 0f
        val py = paddingY?.get(this, Type.H) ?: 0f

        var increment = 0f
        elements?.loop {
            if (positionedElements.contains(it)) {
                it.position(x + px, y + py + increment)
                increment += (it.height * it.scale) + py
            } else {
                it.position(x + px, y + py)
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


class Row(
    constraints: Constraints?,
    private val paddingX: Size?,
    private val paddingY: Size?,
) : Element(constraints?.replaceUndefined(w = Bounding, h = Bounding)) {

    private val positionedElements = hashSetOf<Element>()

    init {
        this.constraints.apply {
            width += paddingX
            height += paddingY
        }
    }

    override fun onElementAdded(element: Element) {
        val constraints = element.constraints
        if (constraints.x is Undefined) {
            positionedElements.add(element)
        }
    }

    override fun positionChildren() {
        if (!enabled) return

        val px = paddingX?.get(this, Type.W) ?: 0f
        val py = paddingY?.get(this, Type.H) ?: 0f

        var increment = 0f
        elements?.loop {
            if (positionedElements.contains(it)) {
                it.position(x + px + increment, y + py)
                increment += (it.width * it.scale) + py
            } else {
                it.position(x + px, y + py)
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