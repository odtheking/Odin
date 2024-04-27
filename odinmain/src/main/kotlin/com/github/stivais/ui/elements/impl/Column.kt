package com.github.stivais.ui.elements.impl

import com.github.stivais.ui.color.Color
import com.github.stivais.ui.color.alpha
import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.constraints.Type
import com.github.stivais.ui.constraints.measurements.Pixel
import com.github.stivais.ui.constraints.measurements.Undefined
import com.github.stivais.ui.constraints.positions.Linked
import com.github.stivais.ui.constraints.sizes.Bounding
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.utils.replaceUndefined
import me.odinmain.utils.render.roundedRectangle

// todo: rework it so it works horiziontally, and if a width is defined, it wraps etc
class Column(constraints: Constraints?, var padding: Float = 0f) : Element(constraints.replaceUndefined(w = Bounding, h = Bounding)) {

    override fun draw() {
        if (color != null && color!!.rgba.alpha != 0) {
            roundedRectangle(x, y, width, height, me.odinmain.utils.render.Color(color!!.rgba))
//            renderer.rect(x, y, width, height, color!!.rgba)
        }
    }

    override fun onElementAdded(element: Element) {
        if (element.constraints.x is Undefined) element.constraints.x = Pixel(0f)
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