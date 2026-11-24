package com.odtheking.odin.clickgui

import androidx.compose.runtime.*
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.settings.impl.HUDSetting
import com.odtheking.odin.clickgui.settings.impl.HudElement
import com.odtheking.odin.features.ModuleManager.hudSettingsCache
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.render.roundedOutline
import com.odtheking.odin.utils.ui.compose.*
import net.minecraft.client.gui.GuiGraphicsExtractor
import kotlin.math.sign

object HudLayer {
    val ui by lazy {
        UiHost {
            for (setting in hudSettingsCache) key(setting) {
                if (setting.isEnabled) Handle(setting)
            }
        }
    }

    fun render(graphics: GuiGraphicsExtractor, mouseX: Int, mouseY: Int) {
        for (setting in hudSettingsCache) if (setting.isEnabled) setting.drawExample(graphics)
        ui.render(graphics, mc.window.guiScaledWidth, mc.window.guiScaledHeight, mouseX, mouseY)
    }

    fun settle() = ui.settle()

    fun hovered(mouseX: Int, mouseY: Int): HUDSetting? = ui.hitTest(mouseX, mouseY) as? HUDSetting

    @Composable
    private fun Handle(setting: HUDSetting) {
        var grabX by remember { mutableIntStateOf(0) }
        var grabY by remember { mutableIntStateOf(0) }
        var guides by remember { mutableStateOf<Pair<Int?, Int?>?>(null) }
        OnDismiss { guides = null }
        val hud = setting.hud
        val interaction = remember { InteractionSource() }

        Canvas(size = { hud.scaledWidth to hud.scaledHeight }) {}
            .offset({ hud.x }, { hud.y })
            .tag(setting)
            .hoverable(interaction)
            .pointerInput(
                onDrag = { event ->
                    hud.x = event.x().toInt() - grabX
                    hud.y = event.y().toInt() - grabY
                    guides = if (mc.hasShiftDown()) null else hud.snap(hudSettingsCache.mapNotNull { if (it.isEnabled) it.hud else null })
                },
                onRelease = { guides = null },
            ) { event ->
                grabX = event.x().toInt() - hud.x
                grabY = event.y().toInt() - hud.y
                true
            }
            .onScroll { amount ->
                hud.scale = (hud.scale + amount.sign.toFloat() * SCROLL_STEP).coerceIn(HudElement.MIN_SCALE, HudElement.MAX_SCALE)
                true
            }
            .drawBehind { graphics ->
                graphics.roundedOutline(x - 1, y - 1, right + 1, bottom + 1, Colors.WHITE.rgba, if (interaction.hovered) 1.5f else 1f, 3f)
                val (lineX, lineY) = guides ?: return@drawBehind
                val (width, height) = mc.window.guiScaledWidth to mc.window.guiScaledHeight
                lineX?.coerceAtMost(width - 1)?.let { graphics.fill(it, 0, it + 1, height, GUIDE_COLOR) }
                lineY?.coerceAtMost(height - 1)?.let { graphics.fill(0, it, width, it + 1, GUIDE_COLOR) }
            }
    }

    private val GUIDE_COLOR = Colors.MINECRAFT_AQUA.rgba

    private const val SCROLL_STEP = 0.1f
}