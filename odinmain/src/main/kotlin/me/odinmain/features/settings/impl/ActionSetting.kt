package me.odinmain.features.settings.impl

import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.percent
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.constraints.size
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.elements.block
import com.github.stivais.ui.elements.text
import com.github.stivais.ui.events.onClick
import com.github.stivais.ui.impl.mainColor
import com.github.stivais.ui.utils.hoverEffect
import com.github.stivais.ui.utils.radii
import com.github.stivais.ui.utils.seconds
import me.odinmain.features.settings.Setting

/**
 * Setting that gets ran when clicked.
 *
 * @author Aton
 */
class ActionSetting(
    name: String,
    description: String = "",
    action: () -> Unit = {}
) : Setting<() -> Unit>(name, false, description) {

    override val default: () -> Unit = action

    override var value: () -> Unit = default

    var action: () -> Unit by this::value

    override fun getElement(parent: Element): SettingElement = parent.setting(40.px) {
        block(
            constraints = size(95.percent, 75.percent),
            color = Color.RGB(38, 38, 38),
            radius = radii(all = 5)
        ) {
            text(
                text = name,
            )
            onClick {
                action.invoke()
                true
            }
            hoverEffect(0.25.seconds)
            outline(mainColor)
        }
    }
}