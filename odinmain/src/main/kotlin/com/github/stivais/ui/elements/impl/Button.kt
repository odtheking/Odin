package com.github.stivais.ui.elements.impl

import com.github.stivais.ui.animation.Animation
import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.events.Mouse
import com.github.stivais.ui.utils.getRGBA
import com.github.stivais.ui.utils.seconds
import kotlin.math.max
import kotlin.math.roundToInt

class Button(constraints: Constraints? = null, color: Color) : Block(constraints, color) {

    private val circles = arrayListOf<Triple<Float, Float, Animation>>()

    init {
        registerEvent(Mouse.Clicked(0)) {
            val anim = Animation(5.seconds, Animations.Linear, 0f, 1f)
            circles.add(Triple(ui.mx, ui.my, anim))
            false
        }
    }

    override fun draw() {// figure out rounded scissoring
//        super.draw()
        renderer.rect(x, y, width, height, color!!.get(this), 5f)
        renderer.pushScissor(x, y, width, height)
        circles.removeIf { (x, y, anim) ->
            val amount = anim.get()
            var size = max(width, height) * 2.5f
            var opacity = 1f
            if (amount <= 0.5f) {
                size *= Animations.EaseInOutQuint.getValue(amount * 2f)
            } else {
                opacity = 1f - (amount - 0.5f) * 2f
            }
            renderer.rect(
                x - size / 2f,
                y - size / 2f,
                size,
                size,
                getRGBA(
                    150, 150, 150,
                    (opacity * 0.2f * 255).roundToInt()
                ),
                radius = size / 2f
            )
            anim.finished
        }
        renderer.popScissor()
    }
}