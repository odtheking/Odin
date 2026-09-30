package com.odtheking.odin.features.impl.dungeon.dungeonwaypoints

import androidx.compose.runtime.*
import com.mojang.blaze3d.platform.InputConstants
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.GuiTheme
import com.odtheking.odin.clickgui.ui.Button
import com.odtheking.odin.clickgui.ui.TextBox
import com.odtheking.odin.utils.render.Corners
import com.odtheking.odin.utils.render.roundedRect
import com.odtheking.odin.utils.render.roundedShadow
import com.odtheking.odin.utils.ui.compose.*

internal const val DIALOG_PADDING = 8
internal const val DIALOG_BUTTON_HEIGHT = 20

private const val PROMPT_WIDTH = 240
private val CORNERS = Corners(GuiTheme.RADIUS)

@Composable
internal fun Dialog(width: Int, heightOf: () -> Int, content: @Composable () -> Unit): UiNode =
    Box(content)
        .width(width)
        .height { heightOf() }
        .offset({ (mc.window.guiScaledWidth - width) / 2 }, { (mc.window.guiScaledHeight - heightOf()) / 2 })
        .drawBehind { graphics ->
            graphics.roundedShadow(x, y, right, bottom, GuiTheme.shadow.rgba, GuiTheme.PANEL_BLUR, CORNERS)
            graphics.roundedRect(x, y, right, bottom, GuiTheme.background.rgba, CORNERS)
        }

internal fun UiNode.centeredX(parentWidth: Int, y: Int) = offset({ (parentWidth - width) / 2 }, { y })

@Composable
internal fun PromptDialog(title: String, onConfirm: (String) -> Unit, onCancel: () -> Unit, scrim: Boolean = true) {
    var text by remember { mutableStateOf("") }
    val focus = remember { FocusRequester() }
    val host = LocalHost.current
    DisposableEffect(host) {
        host.focus(focus)
        onDispose {}
    }

    if (scrim) Canvas(size = { mc.window.guiScaledWidth to mc.window.guiScaledHeight }) { graphics ->
        graphics.fill(x, y, right, bottom, 0x80000000.toInt())
    }
    Dialog(PROMPT_WIDTH, { 80 }) {
        Text({ "§l$title" }).centeredX(PROMPT_WIDTH, DIALOG_PADDING)
        TextBox(text, { text = it }, placeholder = "Enter text", maxLength = 32, focusRequester = focus)
            .size(PROMPT_WIDTH - DIALOG_PADDING * 2, DIALOG_BUTTON_HEIGHT)
            .at(DIALOG_PADDING, 24)
        Button("Done", 100, DIALOG_BUTTON_HEIGHT) { onConfirm(text) }.at(16, 52)
        Button("Cancel", 100, DIALOG_BUTTON_HEIGHT, onCancel).at(124, 52)
    }.onKey { event ->
        when (event.key) {
            InputConstants.KEY_RETURN, InputConstants.KEY_NUMPADENTER -> onConfirm(text)
            InputConstants.KEY_ESCAPE -> onCancel()
            else -> return@onKey false
        }
        true
    }
}
