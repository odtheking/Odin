package com.github.stivais.ui.elements.impl

import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.renderer.Image

class ImageElement(
    private val image: Image,
    constraints: Constraints? = null,
    private val radii: FloatArray,
) : Element(constraints) {

    override fun draw() {
        renderer.image(image, x, y, width, height, radii[0], radii[1], radii[2], radii[3])
    }
}