package com.odtheking.odin.features.impl.nether

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.addVec
import com.odtheking.odin.utils.render.drawText
import com.odtheking.odin.utils.render.drawWireFrameBox
import com.odtheking.odin.utils.render.textDim
import com.odtheking.odin.utils.skyblock.KuudraUtils
import com.odtheking.odin.utils.toFixed

object KuudraInfo : Module(
    name = "Kuudra Info",
    description = "Displays information about Kuudra entity itself."
) {
    private val highlightKuudra by BooleanSetting("Highlight Kuudra", true, desc = "Highlights the kuudra entity.")
    private val kuudraColor by ColorSetting("Kuudra Color", Colors.MINECRAFT_RED, true, desc = "Color of the kuudra highlight.").withDependency { highlightKuudra }
    private val kuudraHPDisplay by BooleanSetting("Kuudra HP", true, desc = "Renders Kuudra's HP infront of it.")
    private val healthSize by NumberSetting("Health Size", 4f, 3.0..8.0, 0.1, desc = "Size of the health display.").withDependency { kuudraHPDisplay }
    private val scaledHealth by BooleanSetting("Use Scaled", true, desc = "Use scaled health for the display meaning the health will update in tier 5 when below 25,000.").withDependency { kuudraHPDisplay }
    private val hud by HUD("Health Display", "Displays the current health of Kuudra.") { example ->
        if (!example && !KuudraUtils.inKuudra) return@HUD 0 to 0
        textDim(if (example) "§a99.975M/240M§c❤" else getCurrentHealthDisplay(KuudraUtils.kuudraEntity?.health ?: return@HUD 0 to 0), 0, 0)
    }

    init {
        on<RenderEvent.Extract> {
            if (!KuudraUtils.inKuudra) return@on

            KuudraUtils.kuudraEntity?.let {
                if (highlightKuudra)
                    drawWireFrameBox(it.boundingBox, kuudraColor, depth = true)

                if (kuudraHPDisplay) {
                    drawText(
                        getCurrentHealthDisplay(it.health),
                        it.position().add(it.lookAngle.multiply(13.0, 13.0, 13.0).addVec(y = 10.0)), healthSize, depth = true
                    )
                }
            }
        }
    }

    private fun getCurrentHealthDisplay(kuudraHP: Float): String {
        val color = when {
            kuudraHP > 99000 -> "§a"
            kuudraHP > 75000 -> "§2"
            kuudraHP > 50000 -> "§e"
            kuudraHP > 25000 -> "§6"
            kuudraHP > 10000 -> "§c"
            else -> "§4"
        }
        val health = kuudraHP / 1000

        return when {
            // Scaled
            kuudraHP <= 25000 && scaledHealth && KuudraUtils.kuudraTier == 5 -> "$color${(health * 9.6).toFixed()}M§7/§a240M§c❤"
            // Absolute
            else -> "$color${health}K§7/§a100k§c❤"
        }
    }
}