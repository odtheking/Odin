package com.github.stivais.ui.elements.impl

import com.github.stivais.ui.animation.Animation
import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.constraints.size
import com.github.stivais.ui.constraints.sizes.Bounding
import com.github.stivais.ui.elements.scope.ElementDSL
import com.github.stivais.ui.elements.scope.ElementScope
import com.github.stivais.ui.operation.AnimationOperation
import com.github.stivais.ui.utils.seconds

class Popup(element: Group, private val smooth: Boolean) : ElementScope<Group>(element) {
    fun closePopup(smooth: Boolean = this.smooth) {
        var finished = false

        if (smooth) {
            // todo: clean animations like these up
            AnimationOperation(Animation(0.25.seconds, Animations.EaseInQuint).onFinish { finished = true }) {
                element.alpha = 1f - it
//                element.scale = 1f - it
            }.add()
        } else {
            finished = true
        }

        operation {
            if (finished) {
                ui.main.removeElement(element)
            }
            finished
        }
    }
}

fun ElementDSL.popup(
    constraints: Constraints? = size(Bounding, Bounding),
    smooth: Boolean = false,
    block: Popup.() -> Unit,
): Popup {
    val group = Group(constraints)
    ui.main.addElement(group)
    if (smooth) {

        AnimationOperation(Animation(0.25.seconds, Animations.EaseOutQuint)) {
            group.alpha = it
        }.add()
    }
    return Popup(group, smooth).also(block)
}