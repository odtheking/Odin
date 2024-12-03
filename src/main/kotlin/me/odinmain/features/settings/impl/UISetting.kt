package me.odinmain.features.settings.impl

import com.github.stivais.aurora.constraints.Constraint
import com.github.stivais.aurora.dsl.*
import com.github.stivais.aurora.elements.ElementScope
import com.github.stivais.aurora.elements.impl.Block.Companion.outline
import me.odinmain.features.impl.render.ClickGUI
import me.odinmain.features.impl.render.ClickGUI.`gray 38`
import me.odinmain.features.settings.Setting
import me.odinmain.features.settings.Setting.Renders.Companion.setting

// doesn't save
class UISetting(
    height: Constraint.Size,
    description: String,
    val scope: ElementScope<*>.() -> Unit
) : Setting<Any?>("", false, description), Setting.Renders {
    override val default: Any? = null
    override var value: Any? = null

    private val _height = height

    override fun ElementScope<*>.create() = setting(_height, scope)
}

@Suppress("FunctionName")
inline fun ActionSetting(
    name: String,
    description: String = "",
    crossinline action: ElementScope<*>.() -> Unit
): UISetting = UISetting(40.px, description) {
    block(
        constraints = size(95.percent, 75.percent),
        color = `gray 38`,
        radius = 5.radius()
    ) {
        hoverEffect(factor = 1.25f)
        outline(ClickGUI.color, thickness = 1.px)
        text(name)

        onClick {
            action.invoke(this)
            true
        }
    }
}