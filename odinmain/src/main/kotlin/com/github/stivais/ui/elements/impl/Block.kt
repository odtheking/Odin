package com.github.stivais.ui.elements.impl

import com.github.stivais.ui.color.Color
import com.github.stivais.ui.color.alpha
import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.constraints.sizes.Copying
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.utils.replaceUndefined

open class Block(constraints: Constraints?, color: Color) : Element(constraints?.replaceUndefined(w = Copying, h = Copying)) {

    var outlineColor: Color? = null

    init {
        this.color = color
    }

    override fun draw() {
        if (color!!.rgba.alpha != 0) {
            renderer.rect(x, y, width, height, color!!.rgba)
        }
        if (outlineColor != null && outlineColor!!.rgba.alpha != 0) {
            renderer.hollowRect(x, y, width, height, 1f, outlineColor!!.rgba, 0f)
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
            renderer.rect(x, y, width, height, color!!.rgba, radii[0], radii[1], radii[2], radii[3])
        }
    }
}