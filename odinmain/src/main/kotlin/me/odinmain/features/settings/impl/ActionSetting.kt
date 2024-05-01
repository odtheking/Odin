package me.odinmain.features.settings.impl

import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.percent
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.constraints.size
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.elements.button
import com.github.stivais.ui.events.onClick
import com.github.stivais.ui.utils.radii
import me.odinmain.features.settings.Setting

/**
 * Setting that gets ran when clicked.
 *
 * @author Aton
 */
class ActionSetting(
    name: String,
    hidden: Boolean = false,
    description: String = "",
    override val default: () -> Unit = {}
) : Setting<() -> Unit>(name, hidden, description) {

    override var value: () -> Unit = default

    var action: () -> Unit by this::value

    override fun getUIElement(parent: Element): SettingElement = parent.setting(40.px) {
        button(
            constraints = size(80.percent, 80.percent),
            // only use for hover effect so color isn't important
            offColor = Color.RGB(38, 38, 38),
            onColor = Color.RGB(38, 38, 38),
            radii = radii(all = 5)
        ) {
            onClick(0) {
                action.invoke()
                true
            }
        }
    }
}