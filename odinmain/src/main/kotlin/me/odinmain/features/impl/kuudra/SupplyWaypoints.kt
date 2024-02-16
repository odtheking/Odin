package me.odinmain.features.impl.kuudra

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.world.RenderUtils
import me.odinmain.utils.skyblock.KuudraUtils
import me.odinmain.utils.skyblock.modMessage
import net.minecraftforge.client.event.RenderWorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.cos
import kotlin.math.sin

object SupplyWaypoints : Module(
    name = "Supply Waypoints",
    description = "Renders waypoints for supplys in Kuudra.",
    category = Category.KUUDRA
) {
    private val suppliesWaypoints: Boolean by BooleanSetting("Supplies Waypoints", true, description = "Renders the supply waypoints")
    private val supplyWaypointColor: Color by ColorSetting("Supply Waypoint Color", Color.YELLOW, true, description = "Color of the supply waypoints").withDependency { suppliesWaypoints }
    private val supplyDropWaypoints: Boolean by BooleanSetting("Supply Drop Waypoints", true, description = "Renders the supply drop waypoints")
    @SubscribeEvent
    fun onWorldRender(event: RenderWorldEvent) {
        modMessage("Supply Waypoints: ${KuudraUtils.supplies.size}")
        if (supplyDropWaypoints) renderSupplyDrop()
        if (suppliesWaypoints) renderGiantZombies()
    }

    private fun renderGiantZombies() {
        KuudraUtils.giantZombies.forEach {
            val yaw = it.rotationYaw
            RenderUtils.renderCustomBeacon(
                "Supply", x = it.posX + (3.7 * cos((yaw + 130) * (Math.PI / 180))),
                y = 72.0, it.posZ + (3.7 * sin((yaw + 130) * (Math.PI / 180))), supplyWaypointColor, true
            )
        }
        modMessage("Giant Zombies: ${KuudraUtils.giantZombies.size}")
    }

    private fun renderSupplyDrop() {
        if (KuudraUtils.supplies[0])
            RenderUtils.renderCustomBeacon("", -98.0, 78.0, -112.0, if (KuudraUtils.missing == "Shop") Color.GREEN else Color.RED) // shop

        if (KuudraUtils.supplies[1])
            RenderUtils.renderCustomBeacon("", -98.0, 78.0, -99.0, if (KuudraUtils.missing == "Equals") Color.GREEN else Color.RED) // equals

        if (KuudraUtils.supplies[2])
            RenderUtils.renderCustomBeacon("", -110.0, 78.0, -106.0, if (KuudraUtils.missing == "X Cannon") Color.GREEN else Color.RED) // cannon

        if (KuudraUtils.supplies[3])
            RenderUtils.renderCustomBeacon("", -106.0, 78.0, -112.0, if (KuudraUtils.missing == "X") Color.GREEN else Color.RED) // x

        if (KuudraUtils.supplies[4])
            RenderUtils.renderCustomBeacon("", -94.0, 78.0, -106.0, if (KuudraUtils.missing == "Triangle") Color.GREEN else Color.RED) // tri

        if (KuudraUtils.supplies[5])
            RenderUtils.renderCustomBeacon("", -106.0, 78.0, -99.0, if (KuudraUtils.missing == "Slash") Color.GREEN else Color.RED) // slash
    }
}