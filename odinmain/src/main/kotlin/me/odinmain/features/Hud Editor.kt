package me.odinmain.features

import com.github.stivais.ui.UI
import com.github.stivais.ui.UIScreen.Companion.init
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.color.withAlpha
import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.constraints.measurements.Pixel
import com.github.stivais.ui.constraints.positions.Linked
import com.github.stivais.ui.constraints.sizes.Bounding
import com.github.stivais.ui.constraints.sizes.Copying
import com.github.stivais.ui.elements.impl.Popup
import com.github.stivais.ui.elements.impl.canvas
import com.github.stivais.ui.elements.impl.popup
import com.github.stivais.ui.elements.scope.ElementDSL
import com.github.stivais.ui.utils.loop
import com.github.stivais.ui.utils.radius
import me.odinmain.config.Config
import me.odinmain.features.ModuleManager.HUDs
import me.odinmain.features.ModuleManager.setupHUD
import me.odinmain.features.impl.render.ClickGUI
import me.odinmain.features.impl.render.ClickGUI.`gray 26`
import me.odinmain.features.impl.render.ClickGUI.`gray 38`
import me.odinmain.utils.ui.outline
import kotlin.math.abs
import kotlin.math.sign
import me.odinmain.features.ModuleManager.hudUI as screen

private var selection: Popup? = null

private var snapX: Float = -1f
private var snapY: Float = -1f

fun openHUDEditor() = UI {

    // snapping lines
    canvas(copies()) { renderer ->
        if (snapX != -1f) renderer.line(snapX, 0f, snapX, ui.main.height, 1f, Color.WHITE.rgba)
        if (snapY != -1f) renderer.line(0f, snapY, ui.main.width, snapY, 1f, Color.WHITE.rgba)
    }.add()

    var advancedOptions: Popup? = null

    onClick {
        advancedOptions?.closePopup()
        advancedOptions = null

        selection?.closePopup()
        selection = null
        false
    }

    // preview huds
    HUDs.loop { hud ->
        val drawable = hud.Drawable(constrain(hud.x, hud.y, Bounding, Bounding), preview = true).add()
        hud.builder(Module.HUDScope(drawable).apply {

            // converts to px for mutability
            afterCreation {
                element.constraints.x = element.x.px
                element.constraints.y = element.y.px
            }
            // converts back to percent based value
            onRemove {
                hud.x.percent = element.x / ui.main.width
                hud.y.percent = element.y / ui.main.height
            }

            // resize
            onScroll { (amount) ->
                hud.scale += 0.1f * amount.sign
                true
            }

            // dragging
            var removePopup = false

            onClick {
                advancedOptions?.closePopup()
                advancedOptions = null

                selection?.closePopup()
                selection = selectionPopup(pressed = true, arrayListOf(element))
                removePopup = true
                true
            }
            onRelease {
                if (removePopup) {
                    selection?.closePopup()
                    selection = null
                    removePopup = false
                }
            }

            // advanced options

            onClick(1) {
                advancedOptions?.closePopup()
                advancedOptions = popup(
                    constraints = at(Linked(element) + 15.px, element.y.px),
                    smooth = true
                ) {
                    column(constrain(0.px, 0.px, Bounding, Bounding)) {
                        block(
                            size(160.px, 10.px),
                            color = `gray 26`,
                            radius(tl = 5, tr = 5)
                        )
                        column(size(160.px, Bounding)) {
                            background(color = Color.RGB(38, 38, 38, 0.7f))
                            hud.settings.loop {
                                it.apply {
                                    createElement()
                                }
                            }

                            group(size(Copying, h = 40.px)) {
                                block(
                                    size(90.percent, 70.percent),
                                    color = `gray 38`,
                                    radius(5)
                                ) {
                                    outline(ClickGUI.color)
                                    text("Reset")
                                }
                            }
                        }
                        block(
                            size(160.px, 10.px),
                            color = `gray 26`,
                            radius(bl = 5, br = 5)
                        )
                    }
                    // consume event
                    onClick { true }


//                    column(size(Bounding, Bounding)) {
//                        block(
//                            size(160.px, 10.px),
//                            color = `gray 26`,
//                            radius = radius(tl = 7, tr = 7)
//                        )
//
//                        column(size(h = Animatable(from = 35.px, to = Bounding))) {
//                            background(color = Color.RGB(38, 38, 38, 0.7f))
//                            block(
//                                size(160.px, 35.px),
//                                color = `gray 26`,
//                            ) {
//                                text("Options")
//                                onClick(0, 1) {
//                                    parent()!!.height.animate(0.25.seconds, Animations.EaseOutQuint)
//                                    true
//                                }
//                            }
//                            block(size(160.px, 140.px), Color.TRANSPARENT)
//                        }
//
//                        block(
//                            size(160.px, 50.px),
//                            color = `gray 26`,
//                            radius = radius(bl = 7, br = 7)
//                        ) {
//                            block(
//                                size(90.percent, 60.percent),
//                                color = `gray 38`,
//                                radius = radius(7)
//                            ) {
//                                outline(ClickGUI.color)
//                                text("Reset")
//                            }
//                        }
//                    }
                }
                true
            }
        })
    }

    // multi-"editing"
    selection()

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
        if (selection != null) {
            if (!selection!!.element.isInside(ui.mx, ui.my)) {
                selection!!.closePopup()
                selection = null
            }
        }
        if (box.enabled) {
            val selectedHUDs = arrayListOf<Module.HUD.Drawable>()

            element.elements?.loop {
                if (it is Module.HUD.Drawable && it.enabled && it.intersects(box.element)) {
                    selectedHUDs.add(it)
                }
            }
            selection?.closePopup()
            selection = selectionPopup(pressed = false, selectedHUDs)
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

fun ElementDSL.selectionPopup(pressed: Boolean, selectedHUDs: ArrayList<Module.HUD.Drawable>): Popup {
    var minX = 9999f
    var minY = 9999f
    var maxX = 0f
    var maxY = 0f
    selectedHUDs.loop {
        if (it.x < minX) minX = it.x
        if (it.x + it.screenWidth() > maxX) maxX = it.screenWidth() + it.x
        if (it.y < minY) minY = it.y
        if (it.y + it.screenHeight() > maxY) maxY = it.screenHeight() + it.y
    }
    val px = minX.px
    val py = minY.px
    return popup(constraints = constrain(px, py, (maxX - minX).px, (maxY - minY).px), smooth = true) {

        outline(
            constraints = copies(),
            color = Color.WHITE
        )

        var mouseDown = pressed
        var tx = ui.mx - (px.pixels)
        var ty = ui.my - (py.pixels)

        onClick {
            mouseDown = true
            tx = ui.mx - (px.pixels)
            ty = ui.my - (py.pixels)
            true
        }
        onRelease {
            mouseDown = false
            snapX = -1f
            snapY = -1f
        }
        // maybe make it snapping?
        onMouseMove {
            if (mouseDown) {
                var newX = (ui.mx - tx)
                var newY = (ui.my - ty)

                snapX = -1f
                snapY = -1f

                val centerX = parent!!.width / 2
                val centerY = parent!!.height / 2

                // Check for center snapping
                if (abs(newX + element.screenWidth() / 2 - centerX) <= SNAP_THRESHOLD) {
                    newX = centerX - element.screenWidth() / 2
                    snapX = centerX
                }
                if (abs(newY + element.screenHeight() / 2 - centerY) <= SNAP_THRESHOLD) {
                    newY = centerY - element.screenHeight() / 2
                    snapY = centerY
                }

                parent?.elements?.loop { other ->
                    if (other is Module.HUD.Drawable && !selectedHUDs.contains(other)) {

                        // horizontal edges
                        if (abs(newY - other.y) <= SNAP_THRESHOLD) {
                            newY = other.y
                            snapY = newY
                        } else if (abs(newY + element.screenHeight() - other.y) <= SNAP_THRESHOLD) {
                            newY = other.y - element.screenHeight()
                            snapY = other.y
                        } else if (abs(newY - (other.y + other.screenHeight())) <= SNAP_THRESHOLD) {
                            newY = other.y + other.screenHeight()
                            snapY = newY
                        }

                        // vertical edges
                        if (abs(newX - other.x) <= SNAP_THRESHOLD) {
                            newX = other.x
                            snapX = newX
                        } else if (abs(newX + element.screenWidth() - other.x) <= SNAP_THRESHOLD) {
                            newX = other.x - element.screenWidth()
                            snapX = other.x
                        } else if (abs(newX - (other.x + other.screenWidth())) <= SNAP_THRESHOLD) {
                            newX = other.x + other.screenWidth()
                            snapX = newX
                        }
                    }
                }

                val lastX = px.pixels
                val lastY = py.pixels

                px.pixels = newX.coerceIn(0f, parent!!.width - element.screenWidth())
                py.pixels = newY.coerceIn(0f, parent!!.height - element.screenHeight())

                selectedHUDs.loop {
                    if (lastX != px.pixels) (it.constraints.x as Pixel).pixels += px.pixels - lastX
                    if (lastY != py.pixels) (it.constraints.y as Pixel).pixels += py.pixels - lastY
                }
                redraw()
                true
            } else {
                false
            }
        }
    }
}

private const val SNAP_THRESHOLD = 5f
