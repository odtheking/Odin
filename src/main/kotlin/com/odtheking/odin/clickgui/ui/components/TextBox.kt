package com.odtheking.odin.clickgui.ui.components

import androidx.compose.runtime.*
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.GuiTheme
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.ui.compose.FocusRequester
import com.odtheking.odin.utils.ui.compose.TextInput
import com.odtheking.odin.utils.ui.compose.UiNode
import com.odtheking.odin.utils.ui.compose.drawBehind

@Composable
fun TextBox(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    maxLength: Int = 32,
    centered: Boolean = false,
    radius: Float = GuiTheme.RADIUS,
    filter: (String) -> String = { it },
    focusRequester: FocusRequester? = null
): UiNode {
    var focused by remember { mutableStateOf(false) }
    return TextInput(
        value, onValueChange, maxLength, centered, filter, focusRequester,
        onFocusChanged = { focused = it },
    ).outlined(radius).drawBehind { graphics ->
        if (value.isNotEmpty() || focused || placeholder.isEmpty()) return@drawBehind
        val textX = if (centered) x + width / 2 - mc.font.width(placeholder) / 2 else x + 4
        graphics.textMiddle(placeholder, textX, y, bottom, Colors.MINECRAFT_GRAY.rgba)
    }
}