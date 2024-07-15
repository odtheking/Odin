package me.odinmain.features.settings.impl

import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.constraints.at
import com.github.stivais.ui.constraints.constrain
import com.github.stivais.ui.constraints.measurements.Animatable
import com.github.stivais.ui.constraints.percent
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.elements.scope.ElementDSL
import com.github.stivais.ui.elements.scope.ElementScope
import com.github.stivais.ui.renderer.Image
import com.github.stivais.ui.utils.seconds
import me.odinmain.features.settings.Setting

/**
 * A setting intended to show or hide other settings in the GUI.
 *
 * @author Bonsai
 */
class DropdownSetting(
    name: String,
    override val default: Boolean = false
): Setting<Boolean>(name, false, "") {

    override var value: Boolean = default

    var enabled: Boolean by this::value

    override fun ElementDSL.createElement() {
        setting(40.px) {
            text(
                text = name,
                pos = at(6.px),
                size = 40.percent
            )
            image(
                Image("/assets/odinmain/clickgui/chevron.svg", type = Image.Type.VECTOR),
                constraints = constrain(-6.px, w = 30.px, h = 30.px)
            ) {
                val rotate = Animatable(from = Math.toRadians(270.0).px, to = Math.toRadians(90.0).px)
                element.rotateAnim = rotate
                onClick {
                    rotate.animate(0.25.seconds, Animations.EaseInOutQuint)
                    value = !value
                    true
                }
            }
        }
    }
}