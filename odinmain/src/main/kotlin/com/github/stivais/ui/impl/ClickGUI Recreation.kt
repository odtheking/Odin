package com.github.stivais.ui.impl

import com.github.stivais.ui.UI
import com.github.stivais.ui.UIScreen
import com.github.stivais.ui.animation.Animation
import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.constraints.measurements.Animatable
import com.github.stivais.ui.constraints.sizes.Bounding
import com.github.stivais.ui.elements.impl.Popup
import com.github.stivais.ui.elements.impl.popup
import com.github.stivais.ui.elements.scope.*
import com.github.stivais.ui.operation.AnimationOperation
import com.github.stivais.ui.operation.UIOperation
import com.github.stivais.ui.renderer.Renderer
import com.github.stivais.ui.utils.animate
import com.github.stivais.ui.utils.radii
import com.github.stivais.ui.utils.seconds
import me.odinmain.OdinMain
import me.odinmain.config.Config
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.ModuleManager.modules
import me.odinmain.features.impl.render.ClickGUIModule.color
import me.odinmain.features.impl.render.ClickGUIModule.lastSeenVersion
import me.odinmain.utils.capitalizeFirst
import me.odinmain.utils.skyblock.modMessage

@JvmField
val ClickGUITheme = Color { color.rgba }

@JvmField
val `gray 26`: Color = Color.RGB(26, 26, 26)

@JvmField
val `gray 38`: Color = Color.RGB(38, 38, 38)

@JvmField
val `transparent fix`: Color = Color.RGB(255, 255, 255, 0.2f)

fun clickGUI(renderer: Renderer) = UI(renderer) ui@{
    // used for search bar without needing to iterate over all elements
    val moduleElements = ArrayList<Pair<Module, ElementDSL>>()
    onRemove { Config.save() }
    text(
        text = "odin${if (OdinMain.isLegitVersion) "" else "-client"} $lastSeenVersion",
        pos = at(x = 1.px, y = -(0.px)),
        size = 12.px
    )
    for (panel in Category.entries) {
        column(at(x = panel.x.px, y = panel.y.px)) {
            onRemove {
                panel.x = element.x
                panel.y = element.y
            }
            onScroll { (amount) ->
                child(1)!!.scroll(amount, 0.1.seconds, Animations.Linear)
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
                    val it = module(module)
                    moduleElements.add(module to it)
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

    block(
        constrain(y = 80.percent, w = 25.percent, h = 5.percent),
        color = `gray 26`,
        radius = 5.radii()
    ) {
        textInput(placeholder = "Search") { str ->
            for ((module, element) in moduleElements) {
                element.enabled = module.name.contains(str, true)
            }
            this@ui.redraw()
        }
    }.draggable(button = 1)

    openAnim(0.5.seconds, Animations.EaseOutQuint)
    closeAnim(0.5.seconds, Animations.EaseInBack)
}

private fun ElementScope<*>.module(module: Module) = column(
    constraints = size(h = Animatable(from = 32.px, to = Bounding))
) {
    val color = Color.Animated(from = `gray 26`, to = ClickGUITheme)
    if (module.enabled) color.swap()
    block(
        constraints = size(240.px, h = 32.px),
        color = color
    ) {
        tooltip(module.description)
        text(
            text = module.name,
            size = 16.px
        )
        onClick {
            color.animate(0.15.seconds)
            module.toggle()
            true
        }
        onClick(1) {
            parent()!!.height.animate(0.25.seconds, Animations.EaseOutQuint)
            true
        }
        hoverEffect(duration = 0.1.seconds)
    }
    for (setting in module.settings) {
        if (setting.hidden) continue
        setting.apply {
            createElement()
        }
    }
}

fun ElementDSL.openAnim(
    duration: Float,
    animation: Animations,
) {
    onCreation {
        // test
        AnimationOperation(Animation(duration, animation)) {
            element.alpha = it
            element.scale = it
        }.add()
    }
}

fun ElementDSL.closeAnim(duration: Float, animation: Animations) {
    onRemove {
        UIScreen.closeAnimHandler = ui.window as UIScreen
        // test
        AnimationOperation(Animation(duration, animation).onFinish { UIScreen.closeAnimHandler = null }) {
            element.alpha = 1f - it
            element.scale = 1f - it
        }.add()
    }
}

fun ElementDSL.tooltip(string: String) {
    if (string.isEmpty()) return

    var popup: Popup? = null
    onHover(1.seconds) {
        val x: Position = (element.x + element.width + 5).px
        val y = (element.y + 5).px
        popup = popup(at(x, y)) {
            block(
                constraints = constrain(0.px, 0.px, Bounding + 10.px, 30.px),
                color = `gray 38`,
                radius = 5.radii()
            ) {
                outline(ClickGUITheme, 2.px)
                text(text = string)
            }
            element.alphaAnim = Animatable(0.px, 1.px).apply { animate(0.25.seconds) }
        }
    }
    onMouseExit {
        popup?.let {
            it.element.alphaAnim?.animate(0.25.seconds, Animations.Linear)
            it.element.alphaAnim?.animation?.onFinish {
                it.close()
                popup = null
            }
        }
    }
}

fun ElementDSL.onHover(duration: Float, block: () -> Unit) {
    onMouseEnter {
        val start = System.nanoTime()
        UIOperation {
            if (System.nanoTime() - start >= duration) {
                block()
                return@UIOperation true
            }
            !element.isInside(ui.mx, ui.my) || !element.renders
        }.add()
    }
}