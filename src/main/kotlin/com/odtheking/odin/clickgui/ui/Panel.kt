package com.odtheking.odin.clickgui.ui

import androidx.compose.runtime.*
import com.mojang.blaze3d.platform.InputConstants
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.ClickGUI
import com.odtheking.odin.clickgui.GuiTheme
import com.odtheking.odin.features.Category
import com.odtheking.odin.features.ModuleManager
import com.odtheking.odin.features.impl.render.ClickGUIModule
import com.odtheking.odin.utils.render.Corners
import com.odtheking.odin.utils.render.roundedRect
import com.odtheking.odin.utils.render.roundedShadow
import com.odtheking.odin.utils.ui.compose.*

@Composable
fun Panel(category: Category, onPress: () -> Unit) {
    val data = remember { ClickGUIModule.panelSetting.getOrPut(category.name) { ClickGUIModule.PanelData(0, 0) } }
    val modules = remember { ModuleManager.modulesByCategory[category]?.sortedByDescending { mc.font.width(it.name) }.orEmpty() }
    val scroll = remember(ClickGUI.searchString) { ScrollState() }

    Column {
        PanelHeader(category, data) {
            data.extended = !data.extended
            scroll.offset = 0
        }

        AnimatedVisibility({ data.extended }) {
            Box {
                Column { for (module in modules) key(module) { ModuleView(module) } }.offset(y = { scroll.offset })
            }
                .width(GuiTheme.ROW_WIDTH)
                .height {
                    scroll.maxScroll = height - MIN_VISIBLE
                    (height + scroll.offset).coerceAtLeast(0)
                }
                .clip()
        }

        Box().size(GuiTheme.ROW_WIDTH, GuiTheme.CAP).drawBehind { graphics ->
            val accented = data.extended && modules.lastOrNull()?.enabled == true
            graphics.roundedRect(x, y, right, bottom, (if (accented) GuiTheme.accent else GuiTheme.background).rgba, CORNERS_BOTTOM)
        }
    }
        .width(GuiTheme.ROW_WIDTH)
        .offset({ data.x.coerceIn(0, maxX()) }, { data.y.coerceIn(0, maxY()) })
        .onPress(onPress)
        .onScroll { amount ->
            if (data.extended) scroll.by(amount)
            data.extended
        }
        .drawBehind { graphics -> graphics.roundedShadow(x, y, right, bottom, GuiTheme.shadow.rgba, GuiTheme.PANEL_BLUR, CORNERS) }
}

@Composable
private fun PanelHeader(category: Category, data: ClickGUIModule.PanelData, onToggle: () -> Unit) {
    var grabX by remember { mutableIntStateOf(0) }
    var grabY by remember { mutableIntStateOf(0) }

    Box().size(GuiTheme.ROW_WIDTH, GuiTheme.ROW_HEIGHT)
        .pointerInput(
            onDrag = { event ->
                if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
                    data.x = (event.x().toInt() - grabX).coerceIn(0, maxX())
                    data.y = (event.y().toInt() - grabY).coerceIn(0, maxY())
                }
            },
        ) { event ->
            when (event.button()) {
                InputConstants.MOUSE_BUTTON_RIGHT -> onToggle()
                InputConstants.MOUSE_BUTTON_LEFT -> {
                    grabX = event.x().toInt() - x
                    grabY = event.y().toInt() - y
                }
                else -> return@pointerInput false
            }
            true
        }
        .drawBehind { graphics ->
            graphics.roundedRect(x, y, right, bottom, GuiTheme.background.rgba, CORNERS_TOP)
            graphics.textCentered("§l${category.name}", x, y, right, bottom)
        }
}

private fun maxX() = (ClickGUI.virtualWidth - GuiTheme.ROW_WIDTH).coerceAtLeast(0)
private fun maxY() = (ClickGUI.virtualHeight - GuiTheme.ROW_HEIGHT).coerceAtLeast(0)

private const val MIN_VISIBLE = 72

private val CORNERS = Corners(GuiTheme.RADIUS)
private val CORNERS_TOP = Corners.top(GuiTheme.RADIUS)
private val CORNERS_BOTTOM = Corners.bottom(GuiTheme.RADIUS)