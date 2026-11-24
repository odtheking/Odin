package com.odtheking.odin.clickgui.ui.components

import androidx.compose.runtime.Composable
import com.odtheking.odin.utils.ui.compose.Canvas
import com.odtheking.odin.utils.ui.compose.LocalHost
import com.odtheking.odin.utils.ui.compose.UiNode
import com.odtheking.odin.utils.ui.compose.clickable

@Composable
fun Scrim(onClick: (() -> Unit)? = null): UiNode {
    val host = LocalHost.current
    return Canvas(size = { host.width to host.height }) { graphics -> graphics.fill(x, y, right, bottom, SCRIM_COLOR) }
        .clickable(onClick = onClick)
}

private const val SCRIM_COLOR = 0x80000000.toInt()