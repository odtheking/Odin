package com.github.stivais.ui.elements.impl

import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.events.Lifetime
import com.github.stivais.ui.renderer.Image

class ImageElement(
    var image: Image,
    constraints: Constraints? = null,
    private val radii: FloatArray,
) : Element(constraints) {

    init {
        registerEvent(Lifetime.Initialized) {
            renderer.createImage(image)
            false
        }
        registerEvent(Lifetime.Uninitialized) {
            renderer.deleteImage(image)
            false
        }
    }

    override fun draw() {
        renderer.image(image, x, y, width, height, radii[0], radii[1], radii[2], radii[3])
    }
}