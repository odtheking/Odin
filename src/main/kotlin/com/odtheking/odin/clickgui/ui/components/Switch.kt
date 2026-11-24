package com.odtheking.odin.clickgui.ui.components

import androidx.compose.runtime.Composable
import com.odtheking.odin.clickgui.GuiTheme
import com.odtheking.odin.clickgui.hoverTint
import com.odtheking.odin.clickgui.ui.animateProgress
import com.odtheking.odin.clickgui.ui.hoverable
import com.odtheking.odin.clickgui.ui.rememberHover
import com.odtheking.odin.clickgui.ui.shrinkOnHover
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.Corners
import com.odtheking.odin.utils.render.circle
import com.odtheking.odin.utils.render.roundedRectClipped
import com.odtheking.odin.utils.ui.compose.*
import kotlin.math.roundToInt

@Composable
fun Switch(checked: Boolean, onClick: (() -> Unit)? = null): UiNode {
    val hover = rememberHover()
    val progress = animateProgress(checked, 200)
    return Box {
        Box().size(SWITCH_WIDTH, SWITCH_HEIGHT).outlined(SWITCH_CORNERS, 1.5f) { GuiTheme.surface.hoverTint(hover.value) }
        Canvas { graphics ->
            if (progress.value <= 0f) return@Canvas
            val fillRight = x + (SWITCH_WIDTH * progress.value).roundToInt()
            graphics.roundedRectClipped(x, y, right, bottom, x, y, fillRight, bottom, GuiTheme.accent.hoverTint(hover.value), SWITCH_CORNERS)
        }.size(SWITCH_WIDTH, SWITCH_HEIGHT)
        Canvas { graphics -> graphics.circle(x + SWITCH_HEIGHT / 2, y + SWITCH_HEIGHT / 2, SWITCH_KNOB_RADIUS, Colors.WHITE.rgba) }
            .size(SWITCH_HEIGHT).offset(x = { ((SWITCH_WIDTH - SWITCH_HEIGHT).toFloat() * progress.value).roundToInt() })
    }.clickable(onClick = onClick).hoverable(hover).shrinkOnHover(hover)
}

const val SWITCH_HEIGHT = 16
private const val SWITCH_WIDTH = 24
private const val SWITCH_KNOB_RADIUS = (SWITCH_HEIGHT / 2 - 4).toFloat()
private val SWITCH_CORNERS = Corners(9f)