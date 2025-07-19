package me.odinmain.features.impl.nether

import me.odinmain.clickgui.settings.Setting.Companion.withDependency
import me.odinmain.clickgui.settings.impl.BooleanSetting
import me.odinmain.clickgui.settings.impl.ColorSetting
import me.odinmain.clickgui.settings.impl.NumberSetting
import me.odinmain.features.Module
import me.odinmain.utils.addVec
import me.odinmain.utils.render.Colors
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.render.RenderUtils.renderBoundingBox
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.KuudraUtils
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.toFixed
import me.odinmain.utils.ui.getTextWidth
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object KuudraDisplay : Module(
    name = "Kuudra Display",
    description = "Displays information about Kuudra entity itself."
) {
    private val highlightKuudra by BooleanSetting("Highlight Kuudra", true, desc = "Highlights the kuudra entity.")
    private val kuudraColor by ColorSetting("Kuudra Color", Colors.MINECRAFT_RED, true, desc = "Color of the kuudra highlight.").withDependency { highlightKuudra }
    private val kuudraSpawnAlert by BooleanSetting("Kuudra Spawn Alert", true, desc = "Alerts you where kuudra spawns.")
    private val kuudraHPDisplay by BooleanSetting("Kuudra HP", true, desc = "Renders kuudra's hp on him.")
    private val showPercentage by BooleanSetting("Show Percentage", true, desc = "Shows the percentage of Kuudra's health instead of absolute.").withDependency { kuudraHPDisplay }
    private val healthSize by NumberSetting("Health Size", 0.3f, 0.1f, 1.0f, 0.1, desc = "Size of the health display.").withDependency { kuudraHPDisplay }
    private val scaledHealth by BooleanSetting("Use Scaled", true, desc = "Use scaled health display.").withDependency { kuudraHPDisplay }
    private val hud by HUD("Health Display", "Displays the current health of Kuudra.") { example ->
        if (!example && !KuudraUtils.inKuudra) return@HUD 0f to 0f
        val string = if (example) "§a99.975M/240M§c❤" else getCurrentHealthDisplay(KuudraUtils.kuudraEntity?.health ?: return@HUD 0 to 0)

        RenderUtils.drawText(string, 1f, 1f)

        getTextWidth(string) + 2f to 10f
    }

    private var kuudraHP = 100000f
    @SubscribeEvent
    fun renderWorldEvent(event: RenderWorldLastEvent) {
        if (!KuudraUtils.inKuudra) return

        KuudraUtils.kuudraEntity?.let {
            if (highlightKuudra)
                Renderer.drawBox(it.renderBoundingBox, kuudraColor, depth = true, fillAlpha = 0, outlineWidth = 3f)

            if (kuudraHPDisplay)
                Renderer.drawStringInWorld(getCurrentHealthDisplay(it.health), it.positionVector.addVec(y = 10), Colors.WHITE, depth = false, scale = healthSize, shadow = true)
        }
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || !KuudraUtils.inKuudra) return

        kuudraHP = KuudraUtils.kuudraEntity?.health ?: return
        val kuudraPos = KuudraUtils.kuudraEntity?.positionVector ?: return
        if (kuudraSpawnAlert && kuudraHP in 24900f..25000f) {
            when {
                kuudraPos.xCoord < -128 -> "§c§lRIGHT"
                kuudraPos.xCoord > -72 -> "§2§lLEFT"
                kuudraPos.zCoord > -84 -> "§a§lFRONT"
                kuudraPos.zCoord < -132 -> "§4§lBACK"
                else -> null
            }?.let { PlayerUtils.alert(it, playSound = false) }
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

            // Percentage
            showPercentage -> "$color${health}§a% §c❤"

            // Absolute
            else -> "$color${health}K§7/§a100k§c❤"
        }
    }
}