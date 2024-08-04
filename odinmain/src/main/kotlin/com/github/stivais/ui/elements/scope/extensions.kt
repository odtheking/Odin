package com.github.stivais.ui.elements.scope

import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.color.color
import com.github.stivais.ui.color.darker
import com.github.stivais.ui.constraints.measurements.Pixel
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.utils.animate
import com.github.stivais.ui.utils.seconds

fun BlockScope.hoverEffect(
    duration: Number = 0.25.seconds,
    handler: ElementScope<*> = this,
) {
    val before = color!!
    val hover = Color.Animated(from = before, to = color { before.rgba.darker(1.25) })
    color = hover
    handler.onMouseEnterExit { hover.animate(duration) }
}

fun ElementDSL.focuses() {
    onClick {
        focusThis()
        true
    }
}

/**
 * Incompatible if the parent element size relies on the children
 */
fun ElementDSL.draggable(
    button: Int = 0,
    acceptsEvent: Boolean = true,
    moves: Element = element,
    coerce: Boolean = false
) {
    val px: Pixel = 0.px
    val py: Pixel = 0.px
    // note: if parent is Bounding, it can cause issues
    afterCreation {
        px.pixels = (moves.x - (moves.parent?.x ?: 0f))
        py.pixels = (moves.y - (moves.parent?.y ?: 0f))
        moves.constraints.x = px
        moves.constraints.y = py
    }
    var pressed = false
    var x = 0f
    var y = 0f
    onClick(button) {
        pressed = true
        x = ui.mx - (moves.x - (moves.parent?.x ?: 0f))
        y = ui.my - (moves.y - (moves.parent?.y ?: 0f))
        acceptsEvent
    }
    onMouseMove {
        if (pressed) {
            if (coerce) {
                px.pixels = (ui.mx - x).coerceIn(0f, parent?.width)
                py.pixels = (ui.my - y).coerceIn(0f, parent?.height)
            } else {
                px.pixels = ui.mx - x
                py.pixels = ui.my - y
            }
            redraw()
        }
        acceptsEvent
    }
    onRelease(button) {
        pressed = false
    }
}

fun ElementDSL.animateColor(to: Color, duration: Float, anim: Animations = Animations.Linear) {
    color = if (duration == 0f) to else Color.Animated(from = color ?: Color.TRANSPARENT, to = to).apply {
        animate(duration, anim)?.onFinish {
            color = to
        }
    }
}