package com.github.stivais.ui.elements.scope

import com.github.stivais.ui.UI
import com.github.stivais.ui.UI.Companion.logger
import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.color.brighter
import com.github.stivais.ui.constraints.measurements.Pixel
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.utils.animate
import com.github.stivais.ui.utils.seconds

fun BlockScope.hoverEffect(duration: Number = 0.25.seconds, accepts: Boolean = true) {
    val before = color!!
    val hover = Color.Animated(from = before, to = Color { before.rgba.brighter(1.2) })
    color = hover
    onMouseEnterExit {
        hover.animate(duration)
        redraw()
        accepts
    }
}

fun ElementDSL.focuses() {
    onClick {
        focusThis()
        true
    }
}

/**
 * Function that gets ran when the UI is finished initializing
 *
 * If UI is already initialized, it just runs the function
 *
 * @param function The function to run
 */
fun ElementDSL.onUIOpen(function: UI.() -> Unit) {
    if (element.initialized) {
        function(element.ui)
        return
    }
    onInitialization {
        if (ui.onOpen == null) ui.onOpen = arrayListOf()
        ui.onOpen!!.add(function)
    }
}

/**
 * Function that gets ran when the UI is closed
 *
 * @param block The function to run
 */
fun ElementDSL.onUIClose(block: UI.() -> Unit) {
    if (element.initialized) {
        if (ui.onClose == null) ui.onClose = arrayListOf()
        ui.onClose!!.add(block)
    } else {
        onInitialization {
            if (ui.onClose == null) ui.onClose = arrayListOf()
            ui.onClose!!.add(block)
        }
    }
}

/**
 * Incompatible if the parent element size relies on the children
 */
fun ElementDSL.draggable(acceptsEvent: Boolean = true, target: Element = element, coerce: Boolean = false) {
    val px: Pixel = 0.px
    val py: Pixel = 0.px
    // note: if parent is Bounding, it can cause issues
    onUIOpen {
        px.pixels = target.internalX
        py.pixels = target.internalY
        target.constraints.x = px
        target.constraints.y = py
    }
    var pressed = false
    var x = 0f
    var y = 0f
    onClick(0) {
        pressed = true
        x = ui.mx - target.internalX
        y = ui.my - target.internalY
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
    onRelease(0) {
        pressed = false
    }
}

fun ElementDSL.takeEvents(from: ElementDSL) {
    val to = element
    val f = from.element
    if (f.events == null) return logger.warning("Tried to take event from an element that doesn't have events")
    if (to.events != null) {
        to.events!!.putAll(f.events!!)
    } else {
        to.events = f.events
    }
    f.events = null
}

fun ElementDSL.animateColor(to: Color, duration: Float, anim: Animations = Animations.Linear) {
    color = if (duration == 0f) to else Color.Animated(from = color ?: Color.TRANSPARENT, to = to).apply {
        animate(duration, anim)?.onFinish {
            color = to
        }
    }
}