package me.odinmain.aurora.components

import com.github.stivais.aurora.components.Component
import com.github.stivais.aurora.components.scope.ComponentScope
import com.github.stivais.aurora.dsl.px

fun ComponentScope<*>.draggable(
    moves: Component = component,
) {
    var initialized = false
    val px = 0.px
    val py = 0.px

    var clicked = false
    var clickedX = 0f
    var clickedY = 0f

    onClick {
        if (!initialized) {
            initialized = true
            px.amount = moves.internalX()
            py.amount = moves.internalX()
            moves.position.first = px
            moves.position.second = py
        }
        clicked = true
        clickedX = aurora.inputManager.mouseX - moves.internalX()
        clickedY = aurora.inputManager.mouseY - moves.internalY()
        moveToTop(moves)
        false
    }
    onRelease {
        clicked = false
    }
    onMouseMove {
        if (clicked) {
            val newX = aurora.inputManager.mouseX - clickedX
            val newY = aurora.inputManager.mouseY - clickedY
            px.amount = newX
            py.amount = newY
            moves.redraw()
        }
    }
}

fun moveToTop(component: Component) {
    component.parent?.components?.apply {
        remove(component)
        add(component)
    }
}