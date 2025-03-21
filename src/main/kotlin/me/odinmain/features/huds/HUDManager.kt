package me.odinmain.features.huds

import com.github.stivais.aurora.color.Color
import com.github.stivais.aurora.constraints.impl.measurements.Pixel
import com.github.stivais.aurora.dsl.*
import com.github.stivais.aurora.elements.Element
import com.github.stivais.aurora.elements.ElementScope
import com.github.stivais.aurora.elements.Layout.Companion.section
import com.github.stivais.aurora.elements.impl.Block.Companion.outline
import com.github.stivais.aurora.elements.impl.Popup
import com.github.stivais.aurora.elements.impl.popup
import com.github.stivais.aurora.utils.loop
import com.github.stivais.aurora.utils.withAlpha
import me.odinmain.config.Config
import me.odinmain.features.impl.render.ClickGUI
import me.odinmain.features.impl.render.ClickGUI.`gray 26`
import me.odinmain.features.impl.render.ClickGUI.`gray 38`
import me.odinmain.features.impl.render.ClickGUI.hoverInformation
import me.odinmain.features.settings.Setting
import me.odinmain.features.settings.Setting.Renders.Companion.onValueChanged
import me.odinmain.utils.runIn
import me.odinmain.utils.ui.renderer.NVGRenderer
import me.odinmain.utils.ui.screens.UIHandler
import kotlin.math.abs

object HUDManager {

    val HUDs = arrayListOf<HUD>()

    var UI: UIHandler? = null

    private var selected: Popup? = null
    private var snapLineX: Float = -1f
    private var snapLineY: Float = -1f
    private const val SNAP_THRESHOLD = 5f

    fun setupHUDs() {
        UI = UIHandler(Aurora(renderer = NVGRenderer) {
            HUDs.loop { hud ->
                val representation = hud.Representation()
                representation.add()
                HUD.Scope(representation, preview = false).apply { hud.builder(this) }
            }
        })
        UI!!.open()
    }

    fun resetHUDs() {
        HUDs.loop { hud ->
            hud.settings.loop {
                it.reset()
            }
        }
    }

    fun makeHUDEditor() = Aurora(renderer = NVGRenderer) {

        var hudOptions: Popup? = null

        object : Element(copies()) {
            override fun draw() {
                renderer.line(snapLineX, 0f, snapLineX, ui.main.height, 1f, Color.RED.rgba)
                renderer.line(0f, snapLineY, ui.main.width, snapLineY, 1f, Color.GREEN.rgba)
            }
        }.add()

        HUDs.loop { hud ->
            val representation = hud.Representation()
            representation.add()
            HUD.Scope(representation, preview = true).apply {
                hud.builder(this)

                onRemove {
                    hud.x.value = (element.x / ui.main.width) * 100f
                    hud.y.value = (element.y / ui.main.height) * 100f
                }

                onScroll { (amount) ->
                    hud.scale.set(hud.scale.value + hud.scale.increment * amount)
                    representation.scaleTransformation = hud.scale.value
                }

                var removeSelected = false

                onClick {
                    hudOptions?.closePopup()
                    hudOptions = null

                    selected?.closePopup()
                    selected = createSelection(pressed = true, arrayListOf(element))
                    removeSelected = true
                    true
                }

                onRelease {
                    if (removeSelected) {
                        selected?.closePopup()
                        selected = null
                        removeSelected = false
                    }
                }

                onClick(button = 1) {
                    hudOptions?.closePopup()
                    hudOptions = popup(
                        constraints = at((element.x + element.screenWidth() + 20).px, element.y.px),
                        smooth = true
                    ) {
                        column {
                            block(
                                size(180.px, 35.px),
                                color = `gray 26`,
                                radius = radius(tl = 5, tr = 5)
                            ) {
                                text(hud.name)
                            }
                            column(size(w = 180.px)) {
                                block(
                                    copies(),
                                    color = Color.RGB(38, 38, 38, 0.7f)
                                )
                                hud.settings.loop {
                                    if (it is Setting.Renders && !it.hidden) {
                                        createScope(it.Drawable()) {
                                            hoverInformation(description = it.description)
                                            onValueChanged { refreshHUDs() }
                                            it.apply { create() }
                                        }
                                    }
                                }
                                section(size = 40.px) {
                                    // maybe make you confirm with an [are you sure]
                                    block(
                                        size(90.percent, 70.percent),
                                        color = `gray 38`,
                                        radius = 5.radius()
                                    ) {
                                        outline(
                                            ClickGUI.color,
                                            thickness = 1.px
                                        )
                                        hoverEffect(factor = 1.25f)
                                        text(string = "Reset")

                                        onClick {
                                            hud.settings.loop {
                                                it.reset()
                                            }
                                            true
                                        }
                                    }
                                }
                            }
                            block(
                                size(180.px, 10.px),
                                color = `gray 26`,
                                radius = radius(bl = 5, br = 5)
                            )
                        }
                        // used to consume any click events, so they do not click through
                        onClick { true }
                    }
                    true
                }
            }
        }

        // used purely to close hud options/selections
        onClick {
            hudOptions?.closePopup()
            hudOptions = null
            selected?.closePopup()
            selected = null
            false
        }

        dragSelection()

        onAdd {
            UI?.close()
        }
        onRemove {
            Config.save()
            UI?.close()
            runIn(1) {
                setupHUDs()
            }
        }
    }

    private fun ElementScope<*>.createSelection(
        pressed: Boolean,
        selectedHUDs: ArrayList<HUD.Representation>
    ): Popup {
        redraw()
        // get bounding box of selectedHUDs
        var minX = 9999f;   var minY = 9999f // I wish I could rust :(
        var maxX = 0f;      var maxY = 0f
        selectedHUDs.loop {
            // check if it has converted to pixel for mutability, if not convert it
            if (it.constraints.x !is Pixel) {
                it.constraints.x = it.x.px
                it.constraints.y = it.y.px
            }
            minX = minOf(minX, it.x)
            maxX = maxOf(maxX, it.x + it.screenWidth())
            minY = minOf(minY, it.y)
            maxY = maxOf(maxY, it.y + it.screenHeight())
        }

        val px = minX.px
        val py = minY.px

        return popup(constraints = constrain(px, py, (maxX - minX).px, (maxY - minY).px), smooth = true) {
            outlineBlock(
                constraints = copies(),
                color = Color.WHITE,
                thickness = 1.px
            )

            var mouseDown = pressed
            var offsetX = ui.mx - px.pixels
            var offsetY = ui.my - py.pixels

            onClick {
                mouseDown = true
                offsetX = ui.mx - px.pixels
                offsetY = ui.my - py.pixels
                true
            }
            onRelease {
                mouseDown = false
                snapLineX = -1f
                snapLineY = -1f
            }
            onMouseMove {
                if (mouseDown) {
                    var newX = (ui.mx - offsetX)
                    var newY = (ui.my - offsetY)

                    snapLineX = -1f
                    snapLineY = -1f

                    val parent = element.parent ?: return@onMouseMove false
                    val centerX = parent.width / 2
                    val centerY = parent.height / 2

                    // Check for center snapLine
                    if (abs(newX + element.screenWidth() / 2 - centerX) <= SNAP_THRESHOLD) {
                        newX = centerX - element.screenWidth() / 2
                        snapLineX = centerX
                    }
                    if (abs(newY + element.screenHeight() / 2 - centerY) <= SNAP_THRESHOLD) {
                        newY = centerY - element.screenHeight() / 2
                        snapLineY = centerY
                    }

                    parent.children?.loop { other ->
                        if (other is HUD.Representation && !selectedHUDs.contains(other)) {

                            // horizontal edges
                            if (abs(newY - other.y) <= SNAP_THRESHOLD) {
                                newY = other.y
                                snapLineY = newY
                            } else if (abs(newY + element.screenHeight() - other.y) <= SNAP_THRESHOLD) {
                                newY = other.y - element.screenHeight()
                                snapLineY = other.y
                            } else if (abs(newY - (other.y + other.screenHeight())) <= SNAP_THRESHOLD) {
                                newY = other.y + other.screenHeight()
                                snapLineY = newY
                            }

                            // vertical edges
                            if (abs(newX - other.x) <= SNAP_THRESHOLD) {
                                newX = other.x
                                snapLineX = newX
                            } else if (abs(newX + element.screenWidth() - other.x) <= SNAP_THRESHOLD) {
                                newX = other.x - element.screenWidth()
                                snapLineX = other.x
                            } else if (abs(newX - (other.x + other.screenWidth())) <= SNAP_THRESHOLD) {
                                newX = other.x + other.screenWidth()
                                snapLineX = newX
                            }
                        }
                    }

                    val lastX = px.pixels
                    val lastY = py.pixels

                    px.pixels = newX.coerceIn(0f, parent.width - element.screenWidth())
                    py.pixels = newY.coerceIn(0f, parent.height - element.screenHeight())

                    selectedHUDs.loop {
                        if (lastX != px.pixels) (it.constraints.x as Pixel).pixels += px.pixels - lastX
                        if (lastY != py.pixels) (it.constraints.y as Pixel).pixels += py.pixels - lastY
                    }
                    redraw()
                    true
               } else false
            }
        }
    }

    private fun ElementScope<*>.dragSelection() {
        val x = 0.px
        val y = 0.px
        val w = 0.px
        val h = 0.px

        val box = block(
            constraints = constrain(x, y, w, h),
            color = Color.BLUE.withAlpha(0.25f)
        ) {
            outline(Color.BLUE, thickness = 1.px)
            toggle()
        }

        var clickedX = 0f
        var clickedY = 0f

        onClick {
            box.toggle()
            clickedX = ui.mx
            clickedY = ui.my

            x.pixels = clickedX
            y.pixels = clickedY
            w.pixels = 0f
            h.pixels = 0f
            box.redraw()
            true
        }
        onRelease {
            if (selected != null) {
                if (!selected!!.element.isInside(ui.mx, ui.my)) {
                    selected!!.closePopup()
                    selected = null
                }
            }
            if (box.enabled) {
                val selectedHUDs = arrayListOf<HUD.Representation>()

                element.children?.loop {
                    if (it is HUD.Representation && it.enabled && it.intersects(box.element)) {
                        selectedHUDs.add(it)
                    }
                }
                selected?.closePopup()
                selected = createSelection(pressed = false, selectedHUDs)

                box.toggle()
                box.redraw()
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
            } else false
        }
    }
}