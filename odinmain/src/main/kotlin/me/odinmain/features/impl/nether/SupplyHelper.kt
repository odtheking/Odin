package me.odinmain.features.impl.nether

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.nether.NoPre.missing
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.utils.formatTime
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.KuudraUtils
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.cos
import kotlin.math.sin

object SupplyHelper : Module(
    name = "Supply Helper",
    description = "Helps with supplies in kuudra.",
    category = Category.NETHER
) {
    private val suppliesWaypoints: Boolean by BooleanSetting("Supplies Waypoints", true, description = "Renders the supply waypoints")
    private val supplyWaypointColor: Color by ColorSetting("Supply Waypoint Color", Color.YELLOW, true, description = "Color of the supply waypoints").withDependency { suppliesWaypoints }
    private val supplyDropWaypoints: Boolean by BooleanSetting("Supply Drop Waypoints", true, description = "Renders the supply drop waypoints")
    private val sendSupplyTime: Boolean by BooleanSetting("Send Supply Time", true, description = "Sends a message when a supply is collected")

    private var startRun = 0L

    init {
        onMessage(Regex("\\[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!")) {
            startRun = System.currentTimeMillis()
        }

        onMessageCancellable(Regex("(\\[.+])? (\\w+) recovered one of Elle's supplies! \\((\\d/\\d)\\)")) {
            if (!sendSupplyTime) return@onMessageCancellable
            val matchResult = Regex("(\\[.+])? (\\w+) recovered one of Elle's supplies! \\((\\d/\\d)\\)").find(it.message) ?: return@onMessageCancellable
            modMessage("§6${matchResult.groupValues[2]}§a took ${formatTime((System.currentTimeMillis() - startRun))} to recover supply §8(${matchResult.groupValues[3]})!", false)
            it.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        if (supplyDropWaypoints && KuudraUtils.phase == 1) renderDropLocations()
        if (suppliesWaypoints && KuudraUtils.phase == 1) renderSupplyWaypoints()
    }

    private fun renderSupplyWaypoints() {
        KuudraUtils.giantZombies.forEach {
            val yaw = it.rotationYaw
            Renderer.drawCustomBeacon("Supply",
                Vec3(it.posX + (3.7 * cos((yaw + 130) * (Math.PI / 180))), 72.0, it.posZ + (3.7 * sin((yaw + 130) * (Math.PI / 180)))), supplyWaypointColor, increase = false)
        }
    }

    private fun renderDropLocations() {
        if (KuudraUtils.supplies[0])
            Renderer.drawCustomBeacon("", Vec3(-98.0, 78.0, -112.0), if (missing == "Shop") Color.GREEN else Color.RED, increase = false) // shop

        if (KuudraUtils.supplies[1])
            Renderer.drawCustomBeacon("", Vec3(-98.0, 78.0, -99.0), if (missing == "Equals") Color.GREEN else Color.RED, increase = false) // equals

        if (KuudraUtils.supplies[2])
            Renderer.drawCustomBeacon("", Vec3(-110.0, 78.0, -106.0), if (missing == "X Cannon") Color.GREEN else Color.RED, increase = false) // cannon

        if (KuudraUtils.supplies[3])
            Renderer.drawCustomBeacon("", Vec3(-106.0, 78.0, -112.0), if (missing == "X") Color.GREEN else Color.RED, increase = false) // x

        if (KuudraUtils.supplies[4])
            Renderer.drawCustomBeacon("", Vec3(-94.0, 78.0, -106.0), if (missing == "Triangle") Color.GREEN else Color.RED, increase = false) // tri

        if (KuudraUtils.supplies[5])
            Renderer.drawCustomBeacon("", Vec3(-106.0, 78.0, -99.0), if (missing == "Slash") Color.GREEN else Color.RED, increase = false) // slash
    }
}