package com.github.stivais.ui.elements.scope

import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.constraints.constrain
import com.github.stivais.ui.constraints.measurements.Animatable
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.constraints.sizes.Copying
import com.github.stivais.ui.elements.impl.Group
import com.github.stivais.ui.utils.animate
import com.github.stivais.ui.utils.radii
import com.github.stivais.ui.utils.seconds
import me.odinmain.utils.round

inline fun ElementDSL.button(
    constraints: Constraints? = null,
    color: Color.Animated,
    on: Boolean = false,
    radii: FloatArray? = null,
    crossinline dsl: BlockScope.() -> Unit = {}
) = block(
    constraints = constraints,
    color = color,
    radius = radii
) {
    if (on) {
        color.swap()
    }
    onClick {
        color.animate(0.15.seconds)
        false
    }
    hoverEffect(0.1.seconds)
    dsl()
}


inline fun ElementDSL.slider(
    constraints: Constraints?,
    color: Color,
    value: Double,
    min: Double,
    max: Double,
    crossinline onChange: (percent: Float) -> Unit
) = block(constraints, Color.RGB(26, 26, 26), radii(3)) {
    var dragging = false
    val sliderWidth = Animatable.Raw(0f)

    block(
        constraints = constrain(0.px, 0.px, sliderWidth, Copying),
        color = color,
        radius = radii(all = 4)
    ) {
        hoverEffect(0.25.seconds)
    }
    onClick {
        val pos = (ui.mx - element.x).coerceIn(0f, element.width)
        sliderWidth.animate(to = pos, 0.75.seconds, Animations.EaseOutQuint)
        onChange(pos / element.width)
        dragging = true
        true
    }
    onMouseMove {
        if (dragging) {
            val pos = (ui.mx - element.x).coerceIn(0f, element.width)
            sliderWidth.to(pos)
            redraw()
            onChange(pos / element.width)
        }
        true
    }
    onRelease(0) {
        dragging = false
    }
    onUIOpen {
        sliderWidth.to(((value - min) / (max - min) * element.width).toFloat())
    }
}

inline fun ElementDSL.sliderBase(
    constraints: Constraints? = null,
    accepts: Boolean = false,
    crossinline onChange: (x: Float, y: Float, wasClick: Boolean) -> Unit,
    crossinline dsl: ElementScope<Group>.() -> Unit = {},
) = group(constraints) {

    var dragging = false

    onClick {
        onChange(
            ((ui.mx - element.x).coerceIn(0f, element.width) / element.width).round(2).toFloat(),
            ((ui.my - element.y).coerceIn(0f, element.height) / element.height).round(2).toFloat(),
            true
        )
        dragging = true
        accepts
    }
    onMouseMove {
        if (dragging) {
            onChange(
                ((ui.mx - element.x).coerceIn(0f, element.width) / element.width).round(2).toFloat(),
                ((ui.my - element.y).coerceIn(0f, element.height) / element.height).round(2).toFloat(),
                false
            )
        }
        accepts
    }
    onRelease {
        dragging = false
    }
    dsl()
}

inline fun ElementDSL.popupMenu(
    constraints: Constraints? = null,
    crossinline block: ElementScope<Group>.() -> Unit
): ElementScope<Group> {
    var group: ElementScope<Group>? = null
    ui.main.createScope().apply {
        group = group(constraints) {
            block()
            draggable()
        }
    }
    return group!!
}