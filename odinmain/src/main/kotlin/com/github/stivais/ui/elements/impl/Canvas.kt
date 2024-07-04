package com.github.stivais.ui.elements.impl

import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.renderer.Renderer

abstract class Canvas(constraints: Constraints? = null) : Element(constraints) {
    init {
        scissors = true
    }
}

inline fun canvas(
    constraints: Constraints? = null,
    crossinline render: (Renderer) -> Unit
): Canvas {
    return object : Canvas(constraints) {
        override fun draw() {
            renderer.push()
            renderer.translate(x, y)
            render(renderer)
            renderer.pop()
        }
    }
}