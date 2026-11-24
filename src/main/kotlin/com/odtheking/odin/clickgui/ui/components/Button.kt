package com.odtheking.odin.clickgui.ui.components

import androidx.compose.runtime.Composable
import com.odtheking.odin.clickgui.GuiTheme
import com.odtheking.odin.clickgui.hoverTint
import com.odtheking.odin.clickgui.ui.hoverable
import com.odtheking.odin.clickgui.ui.rememberHover
import com.odtheking.odin.clickgui.ui.shrinkOnHover
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.ui.compose.*

@Composable
fun Button(
    text: String,
    width: Int,
    height: Int,
    textColor: Int = Colors.WHITE.rgba,
    shrink: Boolean = true,
    fill: (hover: Float) -> Int = { GuiTheme.surface.hoverTint(it) },
    onClick: () -> Unit,
): UiNode {
    val hover = rememberHover()
    val node = Box().size(width, height)
        .hoverable(hover)
        .clickable(onClick = onClick)
    if (shrink) node.shrinkOnHover(hover)
    return node
        .outlined { fill(hover.value) }
        .drawBehind { graphics -> graphics.textCentered(text, x, y, right, bottom, textColor) }
}