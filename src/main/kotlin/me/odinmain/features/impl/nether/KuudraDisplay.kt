package me.odinmain.features.impl.nether

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.addVec
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.RenderUtils.renderBoundingBox
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.render.getMCTextWidth
import me.odinmain.utils.render.mcText
import me.odinmain.utils.round
import me.odinmain.utils.skyblock.KuudraUtils
import me.odinmain.utils.skyblock.KuudraUtils.kuudraEntity
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.PlayerUtils
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object KuudraDisplay : Module(
    name = "Kuudra Display",
    description = "Displays information about Kuudra.",
    category = Category.NETHER
) {
    private val highlightKuudra by BooleanSetting("Highlight Kuudra", true, description = "Highlights the kuudra entity.")
    private val kuudraColor by ColorSetting("Kuudra Color", Color.RED, true, description = "Color of the kuudra highlight.").withDependency { highlightKuudra }
    private val thickness by NumberSetting("Thickness", 3f, 0.1, 8f, description = "Thickness of the kuudra highlight.").withDependency { highlightKuudra }
    private val kuudraSpawnAlert by BooleanSetting("Kuudra Spawn Alert", true, description = "Alerts you where kuudra spawns.")
    private val kuudraHPDisplay by BooleanSetting("Kuudra HP", true, description = "Renders kuudra's hp on him.")
    private val healthSize by NumberSetting("Health Size", 0.3f, 0.1f, 1.0f, 0.1, description = "Size of the health display.").withDependency { kuudraHPDisplay }
    private val healthFormat by BooleanSetting("Health Format", true, description = "Format of the health display (true for Absolute, false for Percentage).").withDependency { kuudraHPDisplay }
    private val scaledHealth by BooleanSetting("Use Scaled", true, description = "Use scaled health display.").withDependency { kuudraHPDisplay }
    private val hud by HudSetting("Health Display", 10f, 10f, 1f, true) {
        if (it) {
            mcText("§a99.975M/240M", 1f, 1f, 1, Color.WHITE, center = false)
            getMCTextWidth("99.975k/100k") + 2f to 10f
        } else {
            if (!KuudraUtils.inKuudra) return@HudSetting 0f to 0f

            mcText(getCurrentHealthDisplay(), 1f, 1f, 1, Color.WHITE, center = false)
            getMCTextWidth("99.975k/100k") + 2f to 10f
        }
    }

    private var kuudraHP = 100000f
    @SubscribeEvent
    fun renderWorldEvent(event: RenderWorldLastEvent) {
        if (!KuudraUtils.inKuudra) return

        kuudraEntity?.let {
            if (highlightKuudra)
                Renderer.drawBox(it.renderBoundingBox, kuudraColor, depth = false, fillAlpha = 0, outlineWidth = thickness)

            if (kuudraHPDisplay)
                Renderer.drawStringInWorld(getCurrentHealthDisplay(), it.positionVector.addVec(y = 10), Color.WHITE, depth = false, scale = healthSize, shadow = true)
        }
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || !KuudraUtils.inKuudra) return

        kuudraHP = kuudraEntity?.health ?: return
        val kuudraPos = kuudraEntity?.positionVector ?: return
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

    private fun getCurrentHealthDisplay(): String {
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
            kuudraHP <= 25000 && scaledHealth && LocationUtils.kuudraTier == 5 -> "$color${(health * 9.6).round(2)}M§7/§a240M §c❤"
            // Percentage
            healthFormat -> "$color${health}§a% §c❤"
            // Exact
            else -> "$color${health}K§7/§a100k §c❤"
        }
    }
}