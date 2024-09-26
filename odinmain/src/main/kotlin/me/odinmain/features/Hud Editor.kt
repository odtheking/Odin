package me.odinmain.features

import com.github.stivais.ui.UI
import com.github.stivais.ui.UIScreen.Companion.init
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.color.withAlpha
import com.github.stivais.ui.constraints.constrain
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.constraints.sizes.Bounding
import com.github.stivais.ui.elements.scope.ElementDSL
import com.github.stivais.ui.utils.loop
import me.odinmain.config.Config
import me.odinmain.features.ModuleManager.HUDs
import me.odinmain.features.ModuleManager.setupHUD
import kotlin.math.abs
import kotlin.math.sign
import me.odinmain.features.ModuleManager.hudUI as screen

fun openHUDEditor() = UI {

    // multi-"editing"
    selection()

    // preview huds
    HUDs.loop { hud ->
        val drawable = hud.Drawable(constrain(hud.x, hud.y, Bounding, Bounding), preview = true).add()
        hud.builder(Module.HUDScope(drawable).apply {
            onScroll { (amount) ->
                hud.scale += 0.1f * amount.sign
                true
            }
            onRelease {
                hud.x.percent = element.x / ui.main.width
                hud.y.percent = element.y / ui.main.height
            }
            draggableSnapping()
        })
    }

    // removes actual hud elements, so it can display previewed ones, and refresh when closed
    onCreation {
        screen.empty()
    }
    onRemove {
        HUDs.loop {
            setupHUD(it)
            screen.init()
            Config.save()
        }
    }
}

// not done
private fun ElementDSL.selection() {
    val x = 0.px
    val y = 0.px
    val w = 0.px
    val h = 0.px

    val box = block(
        constraints = constrain(x, y, w, h),
        color = Color.BLUE.withAlpha(0.25f)
    ) {
        outline(Color.BLUE)
        enabled = false
    }

    var clickedX = 0f
    var clickedY = 0f

    onClick {
        box.enabled = true
        clickedX = ui.mx
        clickedY = ui.my

        x.pixels = clickedX
        y.pixels = clickedY
        w.pixels = 0f
        h.pixels = 0f
        true
    }
    onRelease {
        if (box.enabled) {
            box.enabled = false
        }
    }
    onMouseMove {
        if (box.enabled) {
            val newW = ui.mx - clickedX
            val newH = ui.my - clickedY
            if (newW < 0f) x.pixels = clickedX + newW
            if (newH < 0f) y.pixels = clickedY + newH
            w.pixels = abs(newW)
            h.pixels = abs(newH)
            box.redraw()
            true
        } else {
            false
        }
    }
}

private const val SNAP_THRESHOLD = 10f

// slightly modified version of draggable that snaps to nearby elements
private fun ElementDSL.draggableSnapping() {
    val px = 0.px
    val py = 0.px

    afterCreation {
        px.pixels = element.x
        py.pixels = element.y
        element.constraints.x = px
        element.constraints.y = py
    }

    var mousePressed = false
    var clickedX = 0f
    var clickedY = 0f

    onClick(0) {
        mousePressed = true
        clickedX = ui.mx - (element.x - (element.parent?.x ?: 0f))
        clickedY = ui.my - (element.y - (element.parent?.y ?: 0f))
        true
    }

    onRelease(0) {
        mousePressed = false
    }

    onMouseMove {
        if (mousePressed) {
            var newX = ui.mx - clickedX
            var newY = ui.my - clickedY

            newX = newX.coerceIn(0f, parent!!.width - element.screenWidth())
            newY = newY.coerceIn(0f, parent!!.height - element.screenHeight())

            parent?.elements?.loop snap@ { other ->
                if (other == element) return@snap

                if (abs(newX + element.screenWidth() - other.x) <= SNAP_THRESHOLD) {
                    newX = other.x - element.screenWidth()
                } else if (abs(newX - (other.x + other.screenWidth())) <= SNAP_THRESHOLD) {
                    newX = other.x + other.screenWidth()
                }

                if (abs(newY + element.screenHeight() - other.y) <= SNAP_THRESHOLD) {
                    newY = other.y - element.screenHeight()
                } else if (abs(newY - (other.y + other.screenHeight())) <= SNAP_THRESHOLD) {
                    newY = other.y + other.screenHeight()
                }
            }

            px.pixels = newX.coerceIn(0f, parent!!.width - element.screenWidth())
            py.pixels = newY.coerceIn(0f, parent!!.height - element.screenHeight())

            redraw()
            true
        } else {
            false
        }
    }
}