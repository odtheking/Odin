package com.github.stivais.ui.elements.scope

import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.constraints.copies
import me.odinmain.utils.round

fun ElementDSL.slider(
    constraints: Constraints? = copies(),
    accepts: Boolean = false,
    onChange: (x: Float, y: Float, wasClick: Boolean) -> Unit,
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
}