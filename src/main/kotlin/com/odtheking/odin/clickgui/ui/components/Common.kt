package com.odtheking.odin.clickgui.ui.components

import androidx.compose.runtime.Composable
import com.mojang.blaze3d.platform.InputConstants
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.GuiTheme
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.Corners
import com.odtheking.odin.utils.render.roundedRect
import com.odtheking.odin.utils.render.roundedRectOutlined
import com.odtheking.odin.utils.render.roundedShadow
import com.odtheking.odin.utils.ui.compose.*
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.input.MouseButtonEvent

@Composable
fun Gap(height: Int): UiNode = Box().height(height)

fun UiNode.background(corners: Corners, color: () -> Int) =
    drawBehind { graphics -> graphics.roundedRect(x, y, right, bottom, color(), corners) }

fun UiNode.background(radius: Float = GuiTheme.RADIUS, color: () -> Int = { GuiTheme.surface.rgba }) =
    background(Corners(radius), color)

fun UiNode.outlined(corners: Corners, width: Float = 1f, fill: () -> Int = { GuiTheme.surface.rgba }) =
    drawBehind { graphics -> graphics.roundedRectOutlined(x, y, right, bottom, fill(), GuiTheme.accent.rgba, width, corners) }

fun UiNode.outlined(radius: Float = GuiTheme.RADIUS, width: Float = 1f, fill: () -> Int = { GuiTheme.surface.rgba }) =
    outlined(Corners(radius), width, fill)

fun UiNode.dropShadow(corners: Corners = GuiTheme.CORNERS) =
    drawBehind { graphics -> graphics.roundedShadow(x, y, right, bottom, GuiTheme.shadow.rgba, GuiTheme.PANEL_BLUR, corners) }

fun UiNode.centeredY(parentHeight: Int, x: Int) = offset({ x }, { (parentHeight - height) / 2 })

fun UiNode.centeredX(parentWidth: Int, y: () -> Int) = offset({ (parentWidth - width) / 2 }, y)

fun UiNode.centeredX(parentWidth: Int, y: Int) = centeredX(parentWidth) { y }

fun UiNode.draggable(onDrag: () -> Unit = {}, onRelease: (() -> Unit)? = null, onMove: (x: Float, y: Float) -> Unit): UiNode {
    val move: UiNode.(MouseButtonEvent) -> Unit = { event ->
        onMove(((event.x() - x) / width).toFloat().coerceIn(0f, 1f), ((event.y() - y) / height).toFloat().coerceIn(0f, 1f))
    }
    return pointerInput(
        onDrag = { event ->
            onDrag()
            move(event)
        },
        onRelease = { onRelease?.invoke() },
    ) { event -> (event.button() == InputConstants.MOUSE_BUTTON_LEFT).also { if (it) move(event) } }
}

fun GuiGraphicsExtractor.textMiddle(value: String, x: Int, top: Int, bottom: Int, color: Int = Colors.WHITE.rgba) =
    text(mc.font, value, x, top + (bottom - top - 8) / 2, color, false)

fun GuiGraphicsExtractor.textCentered(
    value: String, left: Int, top: Int, right: Int, bottom: Int,
    color: Int = Colors.WHITE.rgba,
) = textMiddle(value, left + (right - left - mc.font.width(value)) / 2, top, bottom, color)