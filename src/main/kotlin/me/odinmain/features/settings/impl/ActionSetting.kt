package me.odinmain.features.settings.impl

import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.elements.scope.*
import com.github.stivais.ui.utils.radius
import com.github.stivais.ui.utils.seconds
import me.odinmain.features.impl.render.ClickGUI
import me.odinmain.features.impl.render.ClickGUI.`gray 38`
import me.odinmain.features.settings.Setting

/**
 * Setting, which contains a function that gets ran when clicked inside the UI
 *
 * This setting doesn't contain any value
 *
 * @param action Function that gets ran
 */
class ActionSetting(
    name: String,
    description: String = "",
    action: ElementDSL.() -> Unit = {}
) : Setting<ElementDSL.() -> Unit>(name, false, description) {

    override val default: ElementDSL.() -> Unit = action

    override var value: ElementDSL.() -> Unit = default

    override fun ElementScope<*>.createElement() {
        setting(40.px) {
            block(
                constraints = size(95.percent, 75.percent),
                color = `gray 38`,
                radius = radius(all = 5)
            ) {
                text(
                    text = name,
                )
                onClick {
                    value.invoke(this)
                    true
                }
                hoverEffect(0.25.seconds)
                outline(ClickGUI.color)
            }
        }
    }
}