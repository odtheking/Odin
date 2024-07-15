package me.odinmain.features.impl.kuudra

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.ui.hud.HudElement
import me.odinmain.utils.addVec
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.RenderUtils.renderBoundingBox
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.render.getMCTextWidth
import me.odinmain.utils.render.mcText
import me.odinmain.utils.round
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.KuudraUtils.kuudraEntity
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object KuudraDisplay : Module(
    name = "Kuudra Display",
    description = "Displays information about Kuudra.",
    category = Category.KUUDRA
) {
    private val highlightKuudra: Boolean by BooleanSetting("Highlight Kuudra", true, description = "Highlights the kuudra entity")
    private val kuudraColor: Color by OldColorSetting("Kuudra Color", Color.RED, true, description = "Color of the kuudra highlight").withDependency { highlightKuudra }
    private val thickness: Float by NumberSetting("Thickness", 3f, 0.1, 8f, description = "Thickness of the kuudra highlight").withDependency { highlightKuudra }
    private val kuudraSpawnAlert: Boolean by BooleanSetting("Kuudra Spawn Alert", true, description = "Alerts you where kuudra spawns")
    private val kuudraHPDisplay: Boolean by BooleanSetting("Kuudra HP", true, description = "Show the kuudra's health")
    private val healthSize: Float by NumberSetting("Health Size", 0.3f, 0.1f, 1.0f, 0.1, description = "Size of the health display").withDependency { kuudraHPDisplay }
    private val healthFormat: Boolean by DualSetting("Health Format", "Absolute", "Percentage").withDependency { kuudraHPDisplay }
    private val scaledHealth: Boolean by BooleanSetting("Use Scaled", true, description = "Use scaled health display").withDependency { kuudraHPDisplay }
    private val hud: HudElement by HudSetting("Health Display", 10f, 10f, 1f, true) {
        if (it) {
            mcText("§a99.975M/300M", 1f, 1f, 1, Color.WHITE, center = false)
            getMCTextWidth("99.975k/100k") + 2f to 10f
        } else {
            if (LocationUtils.currentArea != Island.Kuudra) return@HudSetting 0f to 0f

            mcText(getCurrentHealthDisplay(), 1f, 1f, 1, Color.WHITE, center = false)
            getMCTextWidth("99.975k/100k") + 2f to 10f
        }
    }

    private var kuudraHP = 100000f
    @SubscribeEvent
    fun renderWorldEvent(event: RenderWorldLastEvent) {
        if (LocationUtils.currentArea != Island.Kuudra) return

        if (highlightKuudra)
            Renderer.drawBox(kuudraEntity.renderBoundingBox, kuudraColor, depth = false, fillAlpha = 0, outlineWidth = thickness)

        if (kuudraHPDisplay)
            Renderer.drawStringInWorld(getCurrentHealthDisplay(), kuudraEntity.positionVector.addVec(y = 10), Color.WHITE, depth = false, scale = healthSize, shadow = true)
    }


    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || LocationUtils.currentArea != Island.Kuudra) return

        kuudraHP = kuudraEntity.health
        val kuudraPos = kuudraEntity.positionVector
        if (kuudraSpawnAlert && kuudraHP in 24900f..25000f) {
            when {
                kuudraPos.xCoord < -128 -> "§c§lRIGHT"
                kuudraPos.xCoord > -72 -> "§2§lLEFT"
                kuudraPos.zCoord > -84 -> "§a§lFRONT"
                kuudraPos.zCoord < -132 -> "§4§lBACK"
                else -> null
            }?.let {
                PlayerUtils.alert(it, playSound = false)
            }
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
        val useScaled = kuudraHP <= 25000 && scaledHealth && LocationUtils.kuudraTier == 5

        return when {
            // Scaled
            useScaled -> "$color${(health * 12).round(2)}M§7/§a300M §c❤"
            // Percentage
            healthFormat -> "$color${health}§a% §c❤"
            // Exact
            else -> "$color${health}K§7/§a100k §c❤"
        }
    }
}