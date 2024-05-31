package com.github.stivais.ui.impl

import com.github.stivais.ui.UI
import com.github.stivais.ui.UIScreen
import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.color.color
import com.github.stivais.ui.constraints.at
import com.github.stivais.ui.constraints.measurements.Animatable
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.constraints.size
import com.github.stivais.ui.constraints.sizes.Bounding
import com.github.stivais.ui.elements.scope.*
import com.github.stivais.ui.renderer.Renderer
import com.github.stivais.ui.utils.animate
import com.github.stivais.ui.utils.radii
import com.github.stivais.ui.utils.seconds
import me.odinmain.OdinMain
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.ModuleManager.modules
import me.odinmain.features.impl.render.ClickGUIModule.color
import me.odinmain.features.impl.render.ClickGUIModule.lastSeenVersion
import me.odinmain.utils.capitalizeFirst

@JvmField
val ClickGUITheme = Color { color.rgba }

@JvmField
val `gray 26`: Color = Color.RGB(26, 26, 26)

@JvmField
val `gray 38`: Color = Color.RGB(38, 38, 38)

@JvmField
val `transparent fix`: Color = Color.RGB(255, 255, 255, 0.2f)

fun clickGUI(renderer: Renderer) = UI(renderer) {
    openCloseAnim(0.5.seconds)
    text(
        text = "odin${if (OdinMain.onLegitVersion) "" else "-client"} $lastSeenVersion",
        pos = at(x = 1.px, y = -(0.px)),
        size = 12.px
    )
    for (panel in Category.entries) {
        column(at(x = panel.x.px, y = panel.y.px)) {
            onUIClose {
                panel.x = element.x
                panel.y = element.y
            }
            onScroll { (amount) ->
                child(1)!!.scroll(amount * 1.25f, 0.1.seconds, Animations.Linear)
                true
            }
            block(
                constraints = size(240.px, 40.px),
                color = `gray 26`,
                radius = radii(tl = 5, tr = 5)
            ) {
                text(
                    text = panel.name.capitalizeFirst(),
                    size = 20.px
                )
                onClick(1) {
                    sibling()!!.height.animate(0.5.seconds, Animations.EaseInOutQuint)
                    panel.extended = !panel.extended
                    true
                }
                draggable(target = parent!!)
            }
            column(size(h = Animatable(from = Bounding, to = 0.px, swapIf = !panel.extended))) {
                scissors()
                for (module in modules) {
                    if (module.category != panel) continue
                    module(module)
                }
                background(color = Color.RGB(38, 38, 38, 0.7f))
            }
            block(
                constraints = size(240.px, 10.px),
                color = `gray 26`,
                radius = radii(br = 5, bl = 5)
            )
        }
    }
}

private fun ElementScope<*>.module(module: Module) {
    column(size(h = Animatable(from = 32.px, to = Bounding))) {
        button(
            constraints = size(w = 240.px, h = 32.px),
            color = color(from = `gray 26`, to = ClickGUITheme),
            on = module.enabled
        ) {
            text(
                text = module.name,
                size = 16.px
            )
            onClick {
                module.toggle()
                true
            }
            onClick(1) {
                parent()!!.height.animate(0.25.seconds, Animations.EaseInOutQuint)
                true
            }
        }
        for (setting in module.settings) {
            if (setting.hidden) continue
            setting.apply {
                createElement()
            }
        }
    }
}

// todo: cleanup
fun ElementDSL.openCloseAnim(
    duration: Float,
    animationIn: Animations = Animations.EaseOutQuint,
    animationOut: Animations = Animations.EaseInBack
) {
    onUIOpen {
        animate(duration, animationIn) {
            alpha = it
            scale = it
        }
    }
    onUIClose {
        (window as UIScreen).keep()
        animate(duration, animationOut) {
            val percent = 1f - it
            alpha = percent
            scale = percent
        }.onFinish {
            window.close()
        }
    }
}