package com.github.stivais.ui.testing

import com.github.stivais.ui.UI
import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.constraints.measurements.Animatable
import com.github.stivais.ui.constraints.positions.Center
import com.github.stivais.ui.constraints.sizes.Bounding
import com.github.stivais.ui.elements.*
import com.github.stivais.ui.events.onClick
import com.github.stivais.ui.renderer.Renderer
import com.github.stivais.ui.utils.animate
import com.github.stivais.ui.utils.draggable
import com.github.stivais.ui.utils.radii
import com.github.stivais.ui.utils.seconds
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.ModuleManager.modules
import me.odinmain.features.impl.render.ClickGUIModule.color
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.capitalizeFirst
import kotlin.math.roundToInt

@JvmField
val mainColor = Color { color.rgba }

// note: currently settings that have a dependency don't hide
// note: scissoring isn't complete
fun clickGUI(renderer: Renderer) = UI(renderer) {
    for (panel in Category.entries) {
        column(at(x = panel.x.px, y = panel.y.px)) {
            block(
                constraints = size(240.px, 40.px),
                color = Color.RGB(26, 26, 26),
                radius = radii(tl = 5, tr = 5)
            ) {
                text(
                    text = panel.name.capitalizeFirst(),
                    size = 20.px
                )
                onClick(1) {
                    sibling()!!.height().animate(0.5.seconds, Animations.EaseInOutQuint)
                    true
                }
                draggable(target = parent!!)
            }
            column(height(Animatable(from = Bounding, to = 0.px, swapIf = !panel.extended))) {
                for (module in modules) {
                    if (module.category != panel) continue
                    module(module)
                }
                background(color = Color.RGB(38, 38, 38, 0.7f))
            }
            block(
                constraints = size(240.px, 10.px),
                color = Color.RGB(26, 26, 26),
                radius = radii(br = 5, bl = 5)
            )
        }
    }
}

private fun Element.module(module: Module) {
    column(height(Animatable(from = 32.px, to = Bounding))) {
        scissors()
        button(
            constraints = size(w = 240.px, h = 32.px),
            offColor = Color.RGB(26, 26, 26),
            onColor = mainColor,
            on = module.enabled
        ) {
            text(
                text = module.name,
                size = 14.px
            )
            onClick(0) {
                module.toggle()
                true
            }
            onClick(1) {
                parent!!.height().animate(0.25.seconds, Animations.EaseInOutQuint)
                true
            }
        }
        for (setting in module.settings) {
            if (setting.hidden) continue
            // temp
            when (setting) {
                is BooleanSetting -> group(size(w = 240.px, h = 40.px)) {
                    text(
                        text = setting.name,
                        at = at(6.px, Center),
                        size = 12.px
                    )
                    button(
                        constraints = constrain(x = -10.px, y = Center, w = 20.px, h = 20.px),
                        offColor = Color.RGB(38, 38, 38),
                        onColor = mainColor,
                        on = setting.enabled,
                        radii = radii(all = 5)
                    ) {
                        onClick(0) {
                            setting.enabled = !setting.enabled
                            true
                        }
                        outline(color = mainColor)
                    }
                }

                is NumberSetting -> group(size(240.px, 40.px)) {
                    text(
                        text = setting.name,
                        at(x = 6.px, y = Center - 3.px),
                        size = 12.px
                    )
                    val display = text(
                        text = "${(setting.valueDouble * 100.0).roundToInt() / 100.0}",
                        at(x = -6.px, y = Center - 3.px),
                        size = 12.px
                    )
                    val slider = slider(
                        constraints = constrain(6.px, -5.px, 228.px, 7.px),
                        value = setting.valueDouble,
                        min = setting.min,
                        max =  setting.max,
                        onChange = { percent ->
                            setting.valueDouble = percent * (setting.max - setting.min) + setting.min
                            display.text = "${(setting.valueDouble * 100.0).roundToInt() / 100.0}"
                        }
                    )
                    takeEvents(from = slider)
                }
            }
        }
    }
}