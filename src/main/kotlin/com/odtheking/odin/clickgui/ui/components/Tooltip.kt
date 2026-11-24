package com.odtheking.odin.clickgui.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.GuiTheme
import com.odtheking.odin.clickgui.ui.hoverable
import com.odtheking.odin.clickgui.ui.rememberHover
import com.odtheking.odin.utils.Color.Companion.hsbMax
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.roundedRectOutlined
import com.odtheking.odin.utils.ui.compose.LocalHost
import com.odtheking.odin.utils.ui.compose.Overlay
import com.odtheking.odin.utils.ui.compose.UiNode
import com.odtheking.odin.utils.ui.compose.drawBehind
import net.minecraft.network.chat.Component

@Composable
fun UiNode.tooltip(text: String): UiNode {
    if (text.isEmpty()) return this
    val host = LocalHost.current
    val rest = rememberHover(600)
    val lines = remember(text) { mc.font.split(Component.literal(text), 200) }
    val boxWidth = remember(lines) { (lines.maxOfOrNull { mc.font.width(it) } ?: 0) + 8 * 2 }
    val boxHeight = remember(lines) { lines.size * mc.font.lineHeight + 8 * 2 }
    return hoverable(rest).drawBehind {
        if (!rest.hovered || rest.value < 1f) return@drawBehind
        val boxX = (right + 10).coerceIn(0, (host.width - boxWidth).coerceAtLeast(0))
        val boxY = y.coerceIn(0, (host.height - boxHeight).coerceAtLeast(0))
        Overlay.defer { overlay ->
            overlay.roundedRectOutlined(boxX, boxY, boxX + boxWidth, boxY + boxHeight, GuiTheme.surface.rgba, GuiTheme.accent.hsbMax().rgba, 1.5f, GuiTheme.RADIUS)
            lines.forEachIndexed { index, line ->
                overlay.text(mc.font, line, boxX + 8, boxY + 8 + index * mc.font.lineHeight, Colors.WHITE.rgba, false)
            }
        }
    }
}
