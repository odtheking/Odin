package com.odtheking.odin.clickgui.ui.components

import androidx.compose.runtime.Composable
import com.odtheking.odin.clickgui.ui.hoverable
import com.odtheking.odin.clickgui.ui.rememberHover
import com.odtheking.odin.utils.render.roundedTexture
import com.odtheking.odin.utils.ui.compose.Canvas
import com.odtheking.odin.utils.ui.compose.UiNode
import com.odtheking.odin.utils.ui.compose.clickable
import com.odtheking.odin.utils.ui.compose.size
import net.minecraft.resources.Identifier

@Composable
fun Icon(
    texture: Identifier,
    size: Int,
    hovered: (() -> Boolean)? = null,
    rotation: () -> Float = { 0f },
    onClick: (() -> Unit)? = null
): UiNode {
    val hover = rememberHover(hovered = hovered)
    return Canvas { graphics ->
        graphics.pose().pushMatrix()
        graphics.pose().translate(x + size / 2f, y + size / 2f)
        graphics.pose().scale(1f + 0.15f * hover.value)
        graphics.pose().rotate(rotation())
        graphics.pose().translate(-size / 2f, -size / 2f)
        graphics.roundedTexture(0, 0, size, size, texture)
        graphics.pose().popMatrix()
    }.clickable(onClick = onClick).size(size).hoverable(hover)
}