package me.odinmain.features.impl.kuudra

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.world.RenderUtils
import me.odinmain.utils.render.world.RenderUtils.drawBoxOutline
import me.odinmain.utils.render.world.RenderUtils.renderBoundingBox
import me.odinmain.utils.skyblock.KuudraUtils.kuudraEntity
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.PlayerUtils
import net.minecraftforge.client.event.RenderWorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object KuudraHighlight : Module(
    name = "Kuudra Display",
    description = "Displays kuudra information in Kuudra.",
    category = Category.KUUDRA
) {
    private val highlightKuudra: Boolean by BooleanSetting("Highlight Kuudra", true, description = "Highlights the kuudra entity")
    private val kuudraColor: Color by ColorSetting("Kuudra Color", Color.RED, true, description = "Color of the kuudra highlight").withDependency { highlightKuudra }
    private val thickness: Float by NumberSetting("Thickness", 3f, 0.1, 8f, description = "Thickness of the kuudra highlight").withDependency { highlightKuudra }
    private val kuudraHPDisplay: Boolean by BooleanSetting("Kuudra HP", true, description = "Show the kuudra's health")
    private val kuudraSpawnAlert: Boolean by BooleanSetting("Kuudra Spawn Alert", true, description = "Alerts you when kuudra spawns")

    var kuudraHP = 100000f
    @SubscribeEvent
    fun renderWorldEvent(event: RenderWorldEvent) {
        if (LocationUtils.currentArea != LocationUtils.Island.Kuudra.name) return
        if (highlightKuudra)
            drawBoxOutline(kuudraEntity.renderBoundingBox, kuudraColor, thickness, true)

        if (kuudraHPDisplay)
            RenderUtils.drawStringInWorld(getCurrentHealthDisplay(), kuudraEntity.positionVector, increase = false, depthTest = false, renderBlackBox = false)
    }


    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || LocationUtils.currentArea != LocationUtils.Island.Kuudra.name) return

        kuudraHP = kuudraEntity.health
        val kuudraPos = kuudraEntity.positionVector
        if (kuudraSpawnAlert && kuudraHP in 24900f..25000f) {
            when {
                kuudraPos.xCoord < -128 -> "RIGHT"
                kuudraPos.xCoord > -72 -> "LEFT"
                kuudraPos.zCoord > -84 -> "FRONT"
                kuudraPos.zCoord < -132 -> "BACK"
                else -> null
            }?.let {
                PlayerUtils.alert(it)
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
        val health = (kuudraHP / 1000).toInt()
        return "$color${health}K§7/§a100K §c❤"
    }
}