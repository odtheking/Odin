package me.odinmain.features.settings.impl

import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.color.toHSB
import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.constraints.measurements.Animatable
import com.github.stivais.ui.constraints.sizes.Copying
import com.github.stivais.ui.elements.scope.ElementScope
import com.github.stivais.ui.elements.scope.draggable
import com.github.stivais.ui.renderer.Gradient.LeftToRight
import com.github.stivais.ui.renderer.Gradient.TopToBottom
import com.github.stivais.ui.utils.radii
import com.github.stivais.ui.utils.seconds
import me.odinmain.features.settings.Setting

class NewColorSetting(
    name: String,
    color: Color.HSB,
    val allowAlpha: Boolean = true,
    description: String = "",
    hidden: Boolean = false
) : Setting<Color.HSB>(name, hidden, description) {

    constructor(name: String, color: Color, description: String = "", hidden: Boolean = false) : this(
        name = name,
        color = color.toHSB(),
        allowAlpha = true,
        description = description,
        hidden = hidden
    )

    override val default: Color.HSB = color

    override var value: Color.HSB = default

    override fun ElementScope<*>.createElement() {
        val size = Animatable(from = 40.px, to = if (allowAlpha) 285.px else 255.px)
        setting(size) {
            column(constraints = copies()) {
                group(
                    constraints = constrain(
                        w = Copying,
                        h = 40.px
                    )
                ) {
                    text(
                        text = name,
                        pos = at(x = 6.px),
                        size = 40.percent
                    )
                    block(
                        constraints = constrain(x = -(6.px), w = 30.px, h = 50.percent),
                        color = value,
                        radii(all = 5)
                    ).onClick {
                        size.animate(0.25.seconds, Animations.EaseInOutQuint)
                        true
                    }
                }
                // saturation & brightness
                block(
                    constraints = size(w = 95.percent, h = 170.px),
                    colors = Color.WHITE to value,
                    direction = LeftToRight
                ) {
                    block(
                        constraints = copies(),
                        colors = Color.TRANSPARENT to Color.BLACK,
                        direction = TopToBottom
                    )
                    block(
                        constraints = size(10.px, 10.px),
                        color = Color.BLACK,
                        radii(5f)
                    ) {
                        draggable(coerce = true)
                    }
                }
                if (allowAlpha) {
                    divider(
                        amount = 10.px
                    )
                    block(
                        size(95.percent, h = 15.px),
                        color = Color.WHITE,
                    )
                }
            }
        }
    }
}