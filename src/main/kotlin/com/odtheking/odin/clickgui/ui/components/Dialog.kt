package com.odtheking.odin.clickgui.ui.components

import androidx.compose.runtime.*
import com.mojang.blaze3d.platform.InputConstants
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.GuiTheme
import com.odtheking.odin.utils.ui.animations.Easing
import com.odtheking.odin.utils.ui.compose.*

const val DIALOG_PADDING = 8
const val DIALOG_BUTTON_HEIGHT = 20

fun dialogListHeight(top: Int, max: Int, reserved: Int = 0) =
    (mc.window.guiScaledHeight - top - reserved - DIALOG_BUTTON_HEIGHT - DIALOG_PADDING * 2 - 20).coerceIn(48, max)

@Composable
fun Dialog(width: Int, heightOf: () -> Int, content: @Composable () -> Unit): UiNode {
    val open = remember { Animatable(0f) }
    SideEffect { open.animateTo(1f, 150, Easing.EASE_IN_OUT) }

    return Box(content)
        .width(width)
        .height { heightOf() }
        .centered()
        .scale { 0.92f + (1f - 0.92f) * open.value }
        .dropShadow()
        .background(GuiTheme.CORNERS) { GuiTheme.background.rgba }
}

@Composable
fun PromptDialog(title: String, onConfirm: (String) -> Unit, onCancel: () -> Unit, scrim: Boolean = true) {
    var text by remember { mutableStateOf("") }
    val focus = remember { FocusRequester() }
    val host = LocalHost.current
    DisposableEffect(host) {
        host.focus(focus)
        onDispose {}
    }

    if (scrim) Scrim()
    Dialog(240, { 80 }) {
        Text({ "§l$title" }).centeredX(240, DIALOG_PADDING)
        TextBox(text, { text = it }, placeholder = "Enter text", maxLength = 32, focusRequester = focus)
            .size(240 - DIALOG_PADDING * 2, DIALOG_BUTTON_HEIGHT)
            .at(DIALOG_PADDING, 24)
        Row(8) {
            Button("Done", 100, DIALOG_BUTTON_HEIGHT) { onConfirm(text) }
            Button("Cancel", 100, DIALOG_BUTTON_HEIGHT, onClick = onCancel)
        }.centeredX(240, 52)
    }.onKey { event ->
        when (event.key) {
            InputConstants.KEY_RETURN, InputConstants.KEY_NUMPADENTER -> onConfirm(text)
            InputConstants.KEY_ESCAPE -> onCancel()
            else -> return@onKey false
        }
        true
    }
}
