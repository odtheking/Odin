package com.odtheking.odin.clickgui.ui

import androidx.compose.runtime.*
import com.odtheking.odin.clickgui.GuiTheme
import com.odtheking.odin.clickgui.blend
import com.odtheking.odin.clickgui.settings.RenderableSetting
import com.odtheking.odin.clickgui.ui.components.textCentered
import com.odtheking.odin.clickgui.ui.components.tooltip
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color.Companion.brighter
import com.odtheking.odin.utils.Color.Companion.darker
import com.odtheking.odin.utils.ui.compose.Box
import com.odtheking.odin.utils.ui.compose.clickable
import com.odtheking.odin.utils.ui.compose.drawBehind
import com.odtheking.odin.utils.ui.compose.size

@Composable
fun ModuleView(module: Module, search: () -> String) {
    val settings = remember { module.settings.values.filterIsInstance<RenderableSetting<*>>() }
    var expanded by remember { mutableStateOf(false) }

    AnimatedVisibility({ module.name.contains(search(), true) }, durationMillis = 150) {
        ModuleHeader(module) { if (settings.isNotEmpty()) expanded = !expanded }
        AnimatedVisibility(expanded, durationMillis = 250) {
            for (setting in settings) AnimatedVisibility(setting.isVisible) { setting.Content() }
        }
    }
}

@Composable
private fun ModuleHeader(module: Module, onExpand: () -> Unit) {
    val toggled = animateProgress(module.enabled)
    val hover = rememberHover()

    Box().size(GuiTheme.ROW_WIDTH, GuiTheme.ROW_HEIGHT)
        .hoverable(hover)
        .tooltip(module.description)
        .clickable(onRightClick = onExpand) { module.toggle() }
        .drawBehind { graphics ->
            var color = blend(GuiTheme.background.rgba, GuiTheme.accent.rgba, toggled.value)
            if (hover.value > 0f)
                color = blend(color, blend(GuiTheme.background.brighter(1.4f).rgba, GuiTheme.accent.darker(0.8f).rgba, toggled.value), hover.value)
            graphics.fill(x, y, right, bottom, color)
            graphics.textCentered(module.name, x, y, right, bottom)
        }
}