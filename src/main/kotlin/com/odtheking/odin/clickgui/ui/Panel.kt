package com.odtheking.odin.clickgui.ui

import androidx.compose.runtime.*
import com.mojang.blaze3d.platform.InputConstants
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.GuiTheme
import com.odtheking.odin.features.Category
import com.odtheking.odin.features.Module
import com.odtheking.odin.features.impl.render.ClickGUIModule.PanelData
import com.odtheking.odin.utils.render.Corners
import com.odtheking.odin.utils.render.roundedRect
import com.odtheking.odin.utils.render.roundedShadow
import com.odtheking.odin.utils.ui.compose.*

class Panel(val category: Category, val modules: List<Module>, val data: PanelData) {

    @Composable
    fun Content(search: () -> String, onPress: () -> Unit) {
        val host = LocalHost.current
        val sorted = remember { modules.sortedByDescending { mc.font.width(it.name) } }
        val scroll = remember(search()) { ScrollState() }

        Column {
            Header {
                data.extended = !data.extended
                scroll.offset = 0
            }

            AnimatedVisibility({ data.extended }) {
                Box {
                    Column { for (module in sorted) key(module) { ModuleView(module, search) } }.offset(y = { scroll.offset })
                }
                    .width(GuiTheme.ROW_WIDTH)
                    .height {
                        scroll.maxScroll = height - MIN_VISIBLE
                        (height + scroll.offset).coerceAtLeast(0)
                    }
                    .clip()
            }

            Box().size(GuiTheme.ROW_WIDTH, GuiTheme.CAP).drawBehind { graphics ->
                val accented = data.extended && sorted.lastOrNull()?.enabled == true
                graphics.roundedRect(x, y, right, bottom, (if (accented) GuiTheme.accent else GuiTheme.background).rgba, CORNERS_BOTTOM)
            }
        }
            .width(GuiTheme.ROW_WIDTH)
            .offset({ data.x.coerceIn(0, host.maxX()) }, { data.y.coerceIn(0, host.maxY()) })
            .onPress(onPress)
            .onScroll { amount ->
                if (data.extended) scroll.by(amount)
                data.extended
            }
            .drawBehind { graphics -> graphics.roundedShadow(x, y, right, bottom, GuiTheme.shadow.rgba, GuiTheme.PANEL_BLUR, CORNERS) }
    }

    @Composable
    private fun Header(onToggle: () -> Unit) {
        val host = LocalHost.current
        var grabX by remember { mutableIntStateOf(0) }
        var grabY by remember { mutableIntStateOf(0) }

        Box().size(GuiTheme.ROW_WIDTH, GuiTheme.ROW_HEIGHT)
            .pointerInput(
                onDrag = { event ->
                    if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
                        data.x = (event.x().toInt() - grabX).coerceIn(0, host.maxX())
                        data.y = (event.y().toInt() - grabY).coerceIn(0, host.maxY())
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
}

private fun UiHost.maxX() = (width - GuiTheme.ROW_WIDTH).coerceAtLeast(0)
private fun UiHost.maxY() = (height - GuiTheme.ROW_HEIGHT).coerceAtLeast(0)

private const val MIN_VISIBLE = 72

private val CORNERS = Corners(GuiTheme.RADIUS)
private val CORNERS_TOP = Corners.top(GuiTheme.RADIUS)
private val CORNERS_BOTTOM = Corners.bottom(GuiTheme.RADIUS)