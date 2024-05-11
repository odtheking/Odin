package com.github.stivais.ui.elements.impl

import com.github.stivais.ui.color.Color
import com.github.stivais.ui.color.alpha
import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.constraints.Type
import com.github.stivais.ui.constraints.measurements.Pixel
import com.github.stivais.ui.constraints.measurements.Undefined
import com.github.stivais.ui.constraints.positions.Center
import com.github.stivais.ui.constraints.positions.Linked
import com.github.stivais.ui.constraints.sizes.Bounding
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.utils.replaceUndefined

// todo: rework it so it works horiziontally, and if a width is defined, it wraps etc
class Column(constraints: Constraints?, var padding: Float = 0f, var wraps: Boolean = false) : Element(constraints.replaceUndefined(w = Bounding, h = Bounding)) {

    private val positioning = arrayListOf<Element>()

//    override fun preChildPositioning() {
//        var x = 0f
//        var y = 0f
//        positioning.forLoop { element ->
//
//        }
//    }

    override fun draw() {
        if (color != null && color!!.rgba.alpha != 0) {
            renderer.rect(x, y, width, height, color!!.get(this))
        }
    }

    override fun onElementAdded(element: Element) {
//        val constraints = element.constraints
//        if (constraints.y is Undefined) {
//            positioning.add(element)
//            constraints.y = 0.px
//            if (constraints.x is Undefined) {
//                constraints.x = if (wraps || this.constraints.width.reliesOnChild()) 0.px else Center
//            }
//        }

        if (element.constraints.x is Undefined) {
            element.constraints.x = if (constraints.width !is Bounding) Center else Pixel(0f)
        }
        if (element.constraints.y is Undefined) {
            val last = elements?.lastOrNull { it.constraints.y is Linked }
            element.constraints.y = Linked(last)
            element.y = element.constraints.y.get(element, Type.Y)
            height = constraints.height.get(element, Type.H)
        }
    }

    fun background(color: Color): Column {
        this.color = color
        return this
    }
}