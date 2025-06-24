package me.odinmain.features.settings

import com.github.stivais.aurora.components.scope.ComponentScope
import com.github.stivais.aurora.components.scope.ContainerScope
import com.github.stivais.aurora.events.AuroraEvent

/**
 *
 */
abstract class RepresentableSetting<T>(
    name: String,
    description: String,
) : Setting<T>(name, description) {

    /**
     * If this setting should be hidden inside the ClickGUI.
     *
     * Recommended to use [hide] to set this value to true.
     */
    private var hidden: Boolean = false

    /**
     * Hides this setting inside the ClickGUI.
     */
    fun hide(): Setting<T> {
        hidden = true
        return this
    }

    abstract fun ContainerScope<*>.represent(): ComponentScope<*>

    /**
     * Creates the components to represent the setting.
     *
     * Also adds operations, which track visibility and if the value has changed.
     */
    fun visualize(container: ContainerScope<*>) {
        val representation = container.represent()

        var hashcode = value.hashCode()
        representation.operation {
            if (hashcode != value.hashCode()) {
                hashcode = value.hashCode()
                representation.aurora.post(ValueChanged, representation.component)
                representation.component.redraw()
            }
            false
        }
    }

    /**
     * Posted when the setting's value is changed.
     */
    data object ValueChanged : AuroraEvent.NonSpecific
}