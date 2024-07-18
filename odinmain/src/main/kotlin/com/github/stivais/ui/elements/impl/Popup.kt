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

class Popup(element: Group) : ElementScope<Group>(element) {
    fun closePopup(smooth: Boolean = false) {
        if (smooth) {
            // todo: clean animations like these up
            AnimationOperation(Animation(0.25.seconds, Animations.EaseInQuint).onFinish { closePopup() }) {
                element.alpha = 1f - it
                element.scale = 1f - it
            }.add()
        } else {
            ui.main.removeElement(element)
        }
    }
}

fun ElementDSL.popup(constraints: Constraints? = size(Bounding, Bounding), block: Popup.() -> Unit): Popup {
    val group = Group(constraints)
    ui.main.addElement(group)
    return Popup(group).also(block)
}