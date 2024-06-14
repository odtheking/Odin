package me.odinmain.features.settings.impl

import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.color.toHSB
import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.constraints.measurements.Animatable
import com.github.stivais.ui.constraints.measurements.center
import com.github.stivais.ui.constraints.sizes.Copying
import com.github.stivais.ui.elements.scope.ElementDSL
import com.github.stivais.ui.elements.scope.ElementScope
import com.github.stivais.ui.elements.scope.sliderBase
import com.github.stivais.ui.impl.`gray 38`
import com.github.stivais.ui.impl.`transparent fix`
import com.github.stivais.ui.renderer.Gradient.LeftToRight
import com.github.stivais.ui.renderer.Gradient.TopToBottom
import com.github.stivais.ui.utils.radii
import com.github.stivais.ui.utils.seconds
import me.odinmain.features.settings.Setting
import java.awt.Color.HSBtoRGB

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

    @JvmField
    val hueMax = Color { HSBtoRGB(value.hue, 1f, 1f) }

    override fun ElementScope<*>.createElement() {
        val size = Animatable(from = 40.px, to = if (allowAlpha) 270.px else 255.px)
        setting(size).column(constraints = copies()) {
            group(constrain(w = Copying, h = 40.px)) {
                text(
                    text = name,
                    pos = at(x = 6.px),
                    size = 40.percent
                )
                block(
                    constraints = constrain(x = -(6.px), w = 30.px, h = 50.percent),
                    color = `gray 38`,
                    radius = 5.radii()
                ) {
                    outline(color = hueMax)
                    block(
                        constraints = indent(2),
                        color = value,
                        radius = 4.radii()
                    )
                    onClick {
                        size.animate(0.25.seconds, Animations.EaseInOutQuint)
                        true
                    }
                }
            }
            `saturation and brightness`()
            divider(10.px)
            `hue slider`()
            if (allowAlpha) {
                divider(10.px)
                `alpha slider`()
            }
        }
    }

    private fun ElementDSL.`saturation and brightness`() {
        val x = Animatable.Raw((228f * value.saturation).coerceIn(8f, 220f))
        val y = Animatable.Raw((170f * (1f - value.brightness)).coerceIn(8f, 220f))
        block(
            size(w = 95.percent, h = 170.px),
            colors = Color.WHITE to hueMax,
            radius = 5f,
            gradient = LeftToRight
        ) {
            block(
                constraints = copies(),
                // temp fix until I figure out why nanovg doesn't render anything under 0.2f alpha
                colors = `transparent fix` to Color.BLACK,
                radius = 5f,
                gradient = TopToBottom
            ) {
                block(
                    constraints = constrain(x.center, y.center, w = 10.px, h = 10.px),
                    color = value,
                    radius = 5.radii()
                ).outline(Color.WHITE)
            }
            sliderBase(
                copies(),
                accepts = true,
                onChange = { px, py, isClick ->
                    val toX = (px * element.width).coerceIn(8f, element.width - 8f)
                    val toY = (py * element.height).coerceIn(8f, element.height - 8f)
                    if (isClick) {
                        x.animate(to = toX, 0.1.seconds, Animations.EaseOutQuad)
                        y.animate(to = toY, 0.1.seconds, Animations.EaseOutQuad)
                    } else {
                        x.to(toX)
                        y.to(toY)
                        redraw()
                    }
                    value.saturation = px
                    value.brightness = 1f - py
                }
               )
        }
    }

    private fun ElementDSL.`hue slider`() {
        val x = Animatable.Raw((228f * value.hue).coerceIn(8f, 220f))
        image(
            "/assets/odinmain/clickgui/HueGradient.png",
            size(w = 95.percent, h = 15.px),
            radius = 5.radii()
        ) {
            block(
                constraints = constrain(x.center, w = 10.px, h = 10.px),
                color = value,
                radius = 5.radii()
            ).outline(Color.WHITE)
            sliderBase(
                copies(),
                accepts = true,
                onChange = { px, _, isClick ->
                    val to = (px * element.width).coerceIn(8f, 220f)
                    if (isClick) x.animate(to = to, 0.75.seconds, Animations.EaseOutQuint) else x.to(to)
                    value.hue = px
                    redraw()
                }
            )
        }
    }

    private fun ElementDSL.`alpha slider`() {
        val x = Animatable.Raw((228f * value.alpha).coerceIn(8f, 220f))
        block(
            size(w = 95.percent, h = 15.px),
            colors = `transparent fix` to hueMax,
            radius = 5f,
            gradient = LeftToRight
        ) {
            block(
                constraints = constrain(x.center, w = 10.px, h = 10.px),
                color = value,
                radius = 5.radii()
            ).outline(Color.WHITE)
            sliderBase(
                copies(),
                accepts = true,
                onChange = { px, _, isClick ->
                    val to = (px * element.width).coerceIn(8f, 220f)
                    if (isClick) x.animate(to = to, 0.75.seconds, Animations.EaseOutQuint) else x.to(to)
                    value.alpha = px
                    redraw()
                }
            )
        }
    }
}
