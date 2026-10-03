package com.odtheking.odin.clickgui.settings.impl

import androidx.compose.runtime.Composable
import com.odtheking.odin.clickgui.GuiTheme
import com.odtheking.odin.clickgui.settings.RenderableSetting
import com.odtheking.odin.clickgui.ui.Button
import com.odtheking.odin.clickgui.ui.tooltip
import com.odtheking.odin.utils.ui.compose.Box
import com.odtheking.odin.utils.ui.compose.offset
import com.odtheking.odin.utils.ui.compose.size

class ActionSetting(
    name: String,
    desc: String,
    override val default: () -> Unit = {}
) : RenderableSetting<() -> Unit>(name, desc) {

    override var value: () -> Unit = default

    var action: () -> Unit by this::value

    @Composable
    override fun Content() {
        Box {
            Button(name, GuiTheme.ROW_WIDTH - INSET * 2, GuiTheme.ROW_HEIGHT - 4) { action() }.offset({ INSET }, { 2 })
        }.size(GuiTheme.ROW_WIDTH, GuiTheme.ROW_HEIGHT).tooltip(description)
    }

    private companion object {
        const val INSET = 3
    }
}