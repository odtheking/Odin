package com.odtheking.odin.clickgui.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.odtheking.odin.utils.ui.compose.*

@Composable
fun ScrollColumn(width: Int, viewport: () -> Int, spacing: Int = 0, content: @Composable () -> Unit): UiNode {
    val scroll = remember { ScrollState() }
    return Box { Column(spacing, content).offset(y = { scroll.offset }) }
        .width(width)
        .height {
            val visible = viewport()
            scroll.maxScroll = height - visible
            visible
        }
        .clip()
        .onScroll { amount -> scroll.by(amount); true }
}