package com.github.stivais.ui.elements.scope

import com.github.stivais.ui.color.Color
import com.github.stivais.ui.color.brighter
import com.github.stivais.ui.utils.animate
import com.github.stivais.ui.utils.seconds

fun BlockScope.hoverEffect(duration: Number = 0.25.seconds) {
    val before = color!!
    val hover = Color.Animated(from = before, to = Color { before.rgba.brighter(1.2) })
    color = hover
    onMouseEnterExit {
        hover.animate(duration)
        redraw()
        true
    }
}

fun ElementDSL.focuses() {
    onClick {
        focusThis()
        true
    }
}