package com.odtheking.odin.clickgui.ui

import androidx.compose.runtime.*
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.ClickGUI
import com.odtheking.odin.clickgui.GuiTheme
import com.odtheking.odin.clickgui.hoverTint
import com.odtheking.odin.clickgui.settings.RenderableSetting
import com.odtheking.odin.utils.Color.Companion.hsbMax
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.*
import com.odtheking.odin.utils.ui.compose.*
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import kotlin.math.roundToInt

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
        .drawBehind { graphics -> graphics.text(mc.font, name, x + GuiTheme.PADDING, y + (height - 8) / 2, Colors.WHITE.rgba, false) }
}


@Composable
fun Gap(height: Int): UiNode = Box().height(height)

fun GuiGraphicsExtractor.textCentered(value: String, left: Int, top: Int, right: Int, bottom: Int, color: Int = Colors.WHITE.rgba) =
    text(mc.font, value, left + (right - left - mc.font.width(value)) / 2, top + (bottom - top - 8) / 2, color, false)

@Composable
fun UiNode.tooltip(text: String): UiNode {
    if (text.isEmpty()) return this
    val interaction = remember { InteractionSource() }
    val rest = animateProgress(600) { interaction.hovered }
    val lines = remember(text) { mc.font.split(Component.literal(text), 200) }
    val boxWidth = remember(lines) { (lines.maxOfOrNull { mc.font.width(it) } ?: 0) + 8 * 2 }
    val boxHeight = remember(lines) { lines.size * mc.font.lineHeight + 8 * 2 }
    return hoverable(interaction).drawBehind {
        if (!interaction.hovered || rest.value < 1f) return@drawBehind
        val boxX = (right + 10).coerceIn(0, (ClickGUI.virtualWidth - boxWidth).coerceAtLeast(0))
        val boxY = y.coerceIn(0, (ClickGUI.virtualHeight - boxHeight).coerceAtLeast(0))
        Overlay.defer { overlay ->
            overlay.roundedRectOutlined(boxX, boxY, boxX + boxWidth, boxY + boxHeight, GuiTheme.surface.rgba, GuiTheme.accent.hsbMax().rgba, 1.5f, GuiTheme.RADIUS)
            lines.forEachIndexed { index, line ->
                overlay.text(mc.font, line, boxX + 8, boxY + 8 + index * mc.font.lineHeight, Colors.WHITE.rgba, false)
            }
        }
    }
}

fun UiNode.outlined(corners: Corners, width: Float = 1f, fill: () -> Int = { GuiTheme.surface.rgba }) =
    drawBehind { graphics -> graphics.roundedRectOutlined(x, y, right, bottom, fill(), GuiTheme.accent.rgba, width, corners) }

fun UiNode.outlined(radius: Float = GuiTheme.RADIUS, width: Float = 1f, fill: () -> Int = { GuiTheme.surface.rgba }) =
    outlined(Corners(radius), width, fill)

@Composable
fun Switch(checked: Boolean, onClick: (() -> Unit)? = null): UiNode {
    val interaction = remember { InteractionSource() }
    val hover = animateProgress { interaction.hovered }
    val progress = animateProgress(checked, 200)
    return Box {
        Box().size(24, SWITCH_HEIGHT).outlined(SWITCH_CORNERS, 1.5f) { GuiTheme.surface.hoverTint(hover.value) }
        Canvas { graphics ->
            if (progress.value <= 0f) return@Canvas
            val fillRight = x + (24 * progress.value).roundToInt()
            graphics.roundedRectClipped(x, y, right, bottom, x, y, fillRight, bottom, GuiTheme.accent.hoverTint(hover.value), SWITCH_CORNERS)
        }.size(24, SWITCH_HEIGHT)
        Canvas { graphics -> graphics.circle(x + SWITCH_HEIGHT / 2, y + SWITCH_HEIGHT / 2, SWITCH_KNOB_RADIUS, Colors.WHITE.rgba) }
            .size(SWITCH_HEIGHT).offset(x = { ((24 - SWITCH_HEIGHT).toFloat() * progress.value).roundToInt() })
    }.clickable(onClick = onClick).hoverable(interaction).scale { 1f - 0.06f * hover.value }
}

@Composable
fun Button(text: String, width: Int, height: Int, onClick: () -> Unit): UiNode {
    val interaction = remember { InteractionSource() }
    val hover = animateProgress { interaction.hovered }
    return Box().size(width, height)
        .hoverable(interaction)
        .clickable(onClick = onClick)
        .scale { 1f - 0.06f * hover.value }
        .outlined { GuiTheme.surface.hoverTint(hover.value) }
        .drawBehind { graphics -> graphics.textCentered(text, x, y, right, bottom) }
}

@Composable
fun Pill(
    text: () -> String,
    color: () -> Int = { Colors.WHITE.rgba },
    onClick: (() -> Unit)? = null,
    onRightClick: (() -> Unit)? = null
): UiNode {
    val interaction = remember { InteractionSource() }
    val hover = animateProgress { interaction.hovered }
    return Canvas(size = { mc.font.width(text()) + GuiTheme.PADDING * 2 to PILL_HEIGHT }) { graphics ->
        graphics.text(mc.font, text(), x + GuiTheme.PADDING, y + (height - 8) / 2, color(), false)
    }
        .clickable(onRightClick = onRightClick, onClick = onClick)
        .hoverable(interaction)
        .outlined { GuiTheme.surface.hoverTint(hover.value, 1.2f) }
}

@Composable
fun Pill(text: String, onClick: (() -> Unit)? = null, onRightClick: (() -> Unit)? = null): UiNode {
    val current = rememberUpdatedState(text)
    return Pill({ current.value }, onClick = onClick, onRightClick = onRightClick)
}

@Composable
fun Icon(
    texture: Identifier,
    size: Int,
    hovered: (() -> Boolean)? = null,
    rotation: () -> Float = { 0f },
    onClick: (() -> Unit)? = null
): UiNode {
    val interaction = remember { InteractionSource() }
    val hover = animateProgress { hovered?.invoke() ?: interaction.hovered }
    return Canvas { graphics ->
        graphics.pose().pushMatrix()
        graphics.pose().translate(x + size / 2f, y + size / 2f)
        graphics.pose().scale(1f + 0.15f * hover.value)
        graphics.pose().rotate(rotation())
        graphics.pose().translate(-size / 2f, -size / 2f)
        graphics.roundedTexture(0, 0, size, size, texture)
        graphics.pose().popMatrix()
    }.clickable(onClick = onClick).size(size).hoverable(interaction)
}

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
        graphics.text(mc.font, placeholder, textX, y + (height - 8) / 2, Colors.MINECRAFT_GRAY.rgba, false)
    }
}

const val SWITCH_HEIGHT = 16
private const val PILL_HEIGHT = 17
private const val SWITCH_KNOB_RADIUS = (SWITCH_HEIGHT / 2 - 4).toFloat()
private val SWITCH_CORNERS = Corners(9f)