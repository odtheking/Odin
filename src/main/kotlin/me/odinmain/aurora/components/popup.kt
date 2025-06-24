package me.odinmain.aurora.components

import com.github.stivais.aurora.animations.Animation
import com.github.stivais.aurora.components.impl.Group
import com.github.stivais.aurora.components.scope.ContainerScope
import com.github.stivais.aurora.measurements.Constraints
import com.github.stivais.aurora.measurements.Measurement
import com.github.stivais.aurora.utils.Timing.Companion.seconds

fun ContainerScope<*>.popup(
    position: Constraints<Measurement.Position>,
    size: Constraints<Measurement.Size>,
    block: ContainerScope<Group>.() -> Unit
) {
    val group = Group(aurora.main, aurora, position, size)
    val scope = ContainerScope(group)
    aurora.main.redraw()
    scope.block()
    scope.onRemove { event ->
        event.cancel()
        Animation(0.5.seconds, Animation.Style.EaseOutQuint).onFinish {
            event.finalize()
        }
    }
}
