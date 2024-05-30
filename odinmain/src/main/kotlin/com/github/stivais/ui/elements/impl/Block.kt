package com.github.stivais.ui.elements.impl

import com.github.stivais.ui.color.Color
import com.github.stivais.ui.color.alpha
import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.constraints.Measurement
import com.github.stivais.ui.constraints.Type
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.constraints.sizes.Copying
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.elements.scope.BlockScope
import com.github.stivais.ui.elements.scope.ElementScope
import com.github.stivais.ui.renderer.Gradient
import com.github.stivais.ui.utils.replaceUndefined

// todo: cleanup
open class Block(constraints: Constraints?, color: Color) : Element(constraints?.replaceUndefined(w = Copying, h = Copying), color) {

    var outlineColor: Color? = null
    var outline: Measurement? = null

    override fun draw() {
        if (color!!.rgba.alpha != 0) {
            renderer.rect(x, y, width, height, color!!.get(this))
        }
        if (outlineColor != null && outlineColor!!.rgba.alpha != 0) {
            val thickness = outline!!.get(this, Type.W)
            renderer.hollowRect(x, y, width, height, thickness, outlineColor!!.get(this), 0f)
        }
    }

    // Maybe add width
    fun outline(color: Color, thickness: Measurement = 1.px): Block {
        outline = thickness
        outlineColor = color
        return this
    }

    override fun createScope(): ElementScope<*> {
        return BlockScope(this)
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
        if (outlineColor != null && outlineColor!!.rgba.alpha != 0) {
            val thickness = outline!!.get(this, Type.W)
            renderer.hollowRect(x, y, width, height, thickness, outlineColor!!.rgba, radii[0], radii[1], radii[2], radii[3])
        }
    }
}

class GradientBlock(
    constraints: Constraints?,
    color1: Color,
    var color2: Color,
    val radius: Float = 0f,
    val direction: Gradient
//    private val radii: FloatArray
) : Block(constraints, color1) {

    init {
//        require(radii.size == 4) { "Radii FloatArray for RoundedBlock must only have 4 values." }
    }

    override fun draw() {
        if (color!!.rgba.alpha != 0) {
            renderer.gradientRect(x, y, width, height, color!!.rgba, color2.rgba, radius, direction)
        }
        if (outlineColor != null && outlineColor!!.rgba.alpha != 0) {
            val thickness = outline!!.get(this, Type.W)
            renderer.hollowRect(x, y, width, height, thickness, outlineColor!!.rgba, radius)
        }
//        renderer.hollowRect(x, y, width, height, 1f, Color.WHITE.rgba)
    }
}