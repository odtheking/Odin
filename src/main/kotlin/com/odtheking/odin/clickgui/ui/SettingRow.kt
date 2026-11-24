package com.odtheking.odin.clickgui.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.odtheking.odin.clickgui.GuiTheme
import com.odtheking.odin.clickgui.settings.RenderableSetting
import com.odtheking.odin.clickgui.ui.components.textMiddle
import com.odtheking.odin.clickgui.ui.components.tooltip
import com.odtheking.odin.utils.ui.compose.*

@Composable
fun RenderableSetting<*>.SettingRow(
    height: Int = GuiTheme.ROW_HEIGHT,
    trailing: @Composable (hovered: () -> Boolean) -> Unit = {},
): UiNode {
    val interaction = remember { InteractionSource() }
    return Box {
        val row = Row(GuiTheme.PADDING) { trailing { interaction.hovered } }
        row.offset({ GuiTheme.ROW_WIDTH - GuiTheme.PADDING - row.width }, { (height - row.height) / 2 })
    }
        .size(GuiTheme.ROW_WIDTH, height)
        .hoverable(interaction)
        .tooltip(description)
        .drawBehind { graphics -> graphics.textMiddle(name, x + GuiTheme.PADDING, y, bottom) }
}