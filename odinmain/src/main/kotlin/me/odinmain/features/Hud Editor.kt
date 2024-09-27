package me.odinmain.features

import com.github.stivais.ui.UI
import com.github.stivais.ui.UIScreen.Companion.init
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.color.withAlpha
import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.constraints.measurements.Pixel
import com.github.stivais.ui.constraints.sizes.Bounding
import com.github.stivais.ui.elements.impl.Popup
import com.github.stivais.ui.elements.impl.popup
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
            onRemove {
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

    var popup: Popup? = null

//    canvas(at(0.px, 0.px)) { renderer ->
//        renderer.hollowRect(minX, minY, maxX - minX, maxY - minY, 1f, Color.WHITE.rgba)
//    }.add()

    onClick {
        if (popup != null) {
            popup!!.closePopup()
            popup = null
        }
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

            val selectedHUDs = arrayListOf<Module.HUD.Drawable>()

            var minX = 9999f
            var minY = 9999f
            var maxX = 0f
            var maxY = 0f

            element.elements?.loop {
                if (it is Module.HUD.Drawable && it.enabled && it.intersects(box.element)) {
                    if (it.x < minX) minX = it.x
                    if (it.x + it.screenWidth() > maxX) maxX = it.screenWidth() + it.x
                    if (it.y < minY) minY = it.y
                    if (it.y + it.screenHeight() > maxY) maxY = it.screenHeight() + it.y
                    selectedHUDs.add(it)
                }
            }

            maxX -= minX
            maxY -= minY
            val popupX = (minX).px
            val popupY = (minY).px

            popup = popup(constraints = constrain(popupX, popupY, maxX.px, maxY.px)) {
                block(
                    constraints = copies(),
                    color = Color.TRANSPARENT
                ).outline(Color.WHITE)

                var pressed = false
                var tx = 0f
                var ty = 0f

                onClick {
                    pressed = true
                    tx = ui.mx - (popupX.pixels)
                    ty = ui.my - (popupY.pixels)
                    true
                }
                onRelease {
                    pressed = false
                }
                onMouseMove {
                    if (pressed) {
                        val lastX = popupX.pixels
                        val lastY = popupY.pixels
                        popupX.pixels = ui.mx - tx
                        popupY.pixels = ui.my - ty

                        selectedHUDs.loop {
                            (it.constraints.x as Pixel).pixels += popupX.pixels - lastX
                            (it.constraints.y as Pixel).pixels += popupY.pixels - lastY
                        }
                        redraw()
                        true
                    } else {
                        false
                    }
                }
            }
            redraw()

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

private const val SNAP_THRESHOLD = 5f

// slightly modified version of draggable that snaps to nearby elements
private fun ElementDSL.draggableSnapping() {
    val px = 0.px
    val py = 0.px
    var snappedLineX: Float? = null
    var snappedLineY: Float? = null

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
        snappedLineX = null
        snappedLineY = null
        redraw()
    }

    onMouseMove {
        if (mousePressed) {
            var newX = ui.mx - clickedX
            var newY = ui.my - clickedY

            newX = newX.coerceIn(0f, parent!!.width - element.screenWidth())
            newY = newY.coerceIn(0f, parent!!.height - element.screenHeight())

            snappedLineX = null
            snappedLineY = null

            // Calculate center lines
            val centerX = parent!!.width / 2
            val centerY = parent!!.height / 2

            // Check for center snapping
            if (abs(newX + element.screenWidth() / 2 - centerX) <= SNAP_THRESHOLD) {
                newX = centerX - element.screenWidth() / 2
                snappedLineX = centerX.toFloat()
            }
            if (abs(newY + element.screenHeight() / 2 - centerY) <= SNAP_THRESHOLD) {
                newY = centerY - element.screenHeight() / 2
                snappedLineY = centerY.toFloat()
            }

            parent?.elements?.forEach { other ->
                if (other != element) {
                    // Check horizontal edges
                    if (abs(newY - other.y) <= SNAP_THRESHOLD) {
                        newY = other.y
                        snappedLineY = newY
                    } else if (abs(newY + element.screenHeight() - other.y) <= SNAP_THRESHOLD) {
                        newY = other.y - element.screenHeight()
                        snappedLineY = other.y
                    } else if (abs(newY - (other.y + other.screenHeight())) <= SNAP_THRESHOLD) {
                        newY = other.y + other.screenHeight()
                        snappedLineY = newY
                    }

                    // Check vertical edges
                    if (abs(newX - other.x) <= SNAP_THRESHOLD) {
                        newX = other.x
                        snappedLineX = newX
                    } else if (abs(newX + element.screenWidth() - other.x) <= SNAP_THRESHOLD) {
                        newX = other.x - element.screenWidth()
                        snappedLineX = other.x
                    } else if (abs(newX - (other.x + other.screenWidth())) <= SNAP_THRESHOLD) {
                        newX = other.x + other.screenWidth()
                        snappedLineX = newX
                    }
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



