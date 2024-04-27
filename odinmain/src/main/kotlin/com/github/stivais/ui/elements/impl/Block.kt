package com.github.stivais.ui.elements.impl

import com.github.stivais.ui.color.Color
import com.github.stivais.ui.color.alpha
import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.constraints.sizes.Copying
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.utils.replaceUndefined
import me.odinmain.utils.render.rectangleOutline
import me.odinmain.utils.render.roundedRectangle

open class Block(constraints: Constraints?, color: Color) : Element(constraints?.replaceUndefined(w = Copying, h = Copying)) {

    var outlineColor: Color? = null

    init {
        this.color = color
    }

    override fun draw() {
        if (color!!.rgba.alpha != 0) {
            roundedRectangle(x, y, width, height, me.odinmain.utils.render.Color(color!!.rgba))
//            renderer.rect(x, y, width, height, color!!.rgba)
        }
        if (outlineColor != null && outlineColor!!.rgba.alpha != 0) {
            rectangleOutline(x, y, width, height, me.odinmain.utils.render.Color(outlineColor!!.rgba), 0f, 1f)
//            renderer.border(x, y, width, height, 1f, null, outlineColor!!.rgba)
        }
    }

    // Maybe add width
    fun outline(color: Color): Block {
        outlineColor = color
        return this
    }
}

class RoundedBlock(constraints: Constraints?, color: Color, private val radii: FloatArray) : Block(constraints, color) {

    init {
        require(radii.size == 4) { "Radii FloatArray for RoundedBlock must only have 4 values." }
    }

    override fun draw() {
        if (color!!.rgba.alpha != 0) {
            val clr = me.odinmain.utils.render.Color(color!!.rgba)
            roundedRectangle(
                x = x,
                y = y,
                w = width,
                h = height,
                color = clr,
                borderColor = clr,
                shadowColor = clr,
                borderThickness = 0f,
                topL = radii[0],
                topR = radii[3],
                botL = radii[1],
                botR = radii[2],
                edgeSoftness = 0.5f
            )
        }
        if (outlineColor != null && outlineColor!!.rgba.alpha != 0) {
            rectangleOutline(x, y, width, height, me.odinmain.utils.render.Color(outlineColor!!.rgba), 0f, 1f)
        }
    }
}