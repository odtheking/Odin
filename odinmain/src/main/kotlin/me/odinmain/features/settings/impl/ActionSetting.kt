package me.odinmain.features.settings.impl

import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.percent
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.constraints.size
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.elements.button
import com.github.stivais.ui.elements.text
import com.github.stivais.ui.events.onClick
import com.github.stivais.ui.impl.mainColor
import com.github.stivais.ui.utils.radii
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
        button(
            constraints = size(95.percent, 75.percent),
            // only use for hover effect so color isn't important
            offColor = Color.RGB(38, 38, 38),
            onColor = Color.RGB(38, 38, 38),
            radii = radii(all = 5)
        ) {
            text(
                text = name,
            )
            onClick(0) {
                action.invoke()
                true
            }
            outline(mainColor)
        }
    }
}