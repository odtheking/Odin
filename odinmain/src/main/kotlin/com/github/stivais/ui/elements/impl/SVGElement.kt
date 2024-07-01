package com.github.stivais.ui.elements.impl

import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.renderer.SVG

class SVGElement(
    private val svg: SVG,
    constraints: Constraints? = null,
    private val radii: FloatArray,
) : Element(constraints) {

    override fun draw() {
        renderer.svg(svg, x, y, width, height, 1f, radii[0], radii[1], radii[2], radii[3])
    }
}