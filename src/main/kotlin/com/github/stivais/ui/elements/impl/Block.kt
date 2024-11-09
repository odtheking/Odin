package com.github.stivais.ui.elements.impl

import com.github.stivais.ui.color.Color
import com.github.stivais.ui.color.alpha
import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.constraints.Measurement
import com.github.stivais.ui.constraints.Type
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.renderer.Gradient as GradientType


open class Block(
    constraints: Constraints?,
    color: Color,
    var outlineColor: Color? = null,
    var outlineThickness: Measurement? = null,
    protected val radius: FloatArray? = null
) : Element(constraints, color) {

    init {
        if (radius != null) {
            require(radius.size == 4) { "Block radius size must be 4" }
        }
    }

    override fun draw() {
        if (radius == null) {
            if (color!!.alpha != 0) {
                renderer.rect(x, y, width, height, color!!.get(this))
            }
            if (outlineColor != null && outlineColor!!.alpha != 0) {
                val thickness = outlineThickness!!.get(this, Type.W)
                renderer.hollowRect(x, y, width, height, thickness, outlineColor!!.get(this), 0f)
            }
        } else {
            if (color!!.alpha != 0) {
                renderer.rect(x, y, width, height, color!!.rgba, radius[0], radius[1], radius[2], radius[3])
            }
            if (outlineColor != null && outlineColor!!.alpha != 0) {
                val thickness = outlineThickness!!.get(this, Type.W)
                renderer.hollowRect(x, y, width, height, thickness, outlineColor!!.rgba, radius[0], radius[1], radius[2], radius[3])
            }
        }
    }

    class Gradient(
        constraints: Constraints?,
        color1: Color,
        private val color2: Color,
        radius: FloatArray?,
        private val gradient: GradientType
    ) : Block(constraints, color1, null, null, radius) {
        override fun draw() {
            if (radius == null) {
                renderer.gradientRect(x, y, width, height, color!!.get(this), color2.get(this), gradient)
                if (outlineColor != null && outlineColor!!.alpha != 0) {
                    val thickness = outlineThickness!!.get(this, Type.W)
                    renderer.hollowRect(x, y, width, height, thickness, outlineColor!!.get(this), 0f)
                }
            } else {
                renderer.gradientRect(x, y, width, height, color!!.rgba, color2.rgba, gradient, radius[0], radius[1], radius[2], radius[3])
                if (outlineColor != null && outlineColor!!.alpha != 0) {
                    val thickness = outlineThickness!!.get(this, Type.W)
                    renderer.hollowRect(x, y, width, height, thickness, outlineColor!!.rgba, radius[0], radius[1], radius[2], radius[3])
                }
            }
        }
    }
}