package com.odtheking.odin.clickgui.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.GuiTheme
import com.odtheking.odin.clickgui.hoverTint
import com.odtheking.odin.clickgui.ui.hoverable
import com.odtheking.odin.clickgui.ui.rememberHover
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.ui.compose.Canvas
import com.odtheking.odin.utils.ui.compose.UiNode
import com.odtheking.odin.utils.ui.compose.clickable

@Composable
fun Pill(
    text: () -> String,
    color: () -> Int = { Colors.WHITE.rgba },
    onClick: (() -> Unit)? = null,
    onRightClick: (() -> Unit)? = null
): UiNode {
    val hover = rememberHover()
    return Canvas(size = { mc.font.width(text()) + GuiTheme.PADDING * 2 to PILL_HEIGHT }) { graphics ->
        graphics.textMiddle(text(), x + GuiTheme.PADDING, y, bottom, color())
    }
        .clickable(onRightClick = onRightClick, onClick = onClick)
        .hoverable(hover)
        .outlined { GuiTheme.surface.hoverTint(hover.value, 1.2f) }
}

@Composable
fun Pill(text: String, onClick: (() -> Unit)? = null, onRightClick: (() -> Unit)? = null): UiNode {
    val current = rememberUpdatedState(text)
    return Pill({ current.value }, onClick = onClick, onRightClick = onRightClick)
}

private const val PILL_HEIGHT = 17