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
import me.odinmain.utils.skyblock.KuudraUtils.PreSpot
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
    private val suppliesWaypoints by BooleanSetting("Supplies Waypoints", true, description = "Renders the supply waypoints.")
    private val supplyWaypointColor by ColorSetting("Supply Waypoint Color", Color.YELLOW, true, description = "Color of the supply waypoints.").withDependency { suppliesWaypoints }
    private val supplyDropWaypoints by BooleanSetting("Supply Drop Waypoints", true, description = "Renders the supply drop waypoints.")
    private val sendSupplyTime by BooleanSetting("Send Supply Time", true, description = "Sends a message when a supply is collected.")

    private var startRun = 0L

    init {
        onMessage(Regex("\\[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!")) {
            startRun = System.currentTimeMillis()
        }

        onMessageCancellable(Regex("(\\[\\w+])?(\\w+) recovered one of Elle's supplies! \\((\\d)/(\\d)\\)")) {
            if (!sendSupplyTime) return@onMessageCancellable
            val (name, current, total) = Regex("(\\[\\w+])?(\\w+) recovered one of Elle's supplies! \\((\\d)/(\\d)\\)").find(it.message)?.destructured ?: return@onMessageCancellable
            modMessage("$name, §a§lrecovered a supply at ${formatTime((System.currentTimeMillis() - startRun))}!, §r§8($current/$total)", "")
            it.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        if (!KuudraUtils.inKuudra || KuudraUtils.phase != 1) return
        if (supplyDropWaypoints) renderDropLocations()
        if (suppliesWaypoints) renderSupplyWaypoints()
    }

    private fun renderSupplyWaypoints() {
        KuudraUtils.giantZombies.forEach {
            Renderer.drawCustomBeacon("Supply",
                Vec3(it.posX + (3.7 * cos((it.rotationYaw + 130) * (Math.PI / 180))), 72.0, it.posZ + (3.7 * sin((it.rotationYaw + 130) * (Math.PI / 180)))), supplyWaypointColor, increase = false)
        }
    }

    private val locations = listOf(
        Pair(Vec3(-98.0, 78.0, -112.0), PreSpot.Shop),
        Pair(Vec3(-98.0, 78.0, -99.0), PreSpot.Equals),
        Pair(Vec3(-110.0, 78.0, -106.0), PreSpot.xCannon),
        Pair(Vec3(-106.0, 78.0, -112.0), PreSpot.X ),
        Pair(Vec3(-94.0, 78.0, -106.0), PreSpot.Triangle),
        Pair(Vec3(-106.0, 78.0, -99.0), PreSpot.Slash),
    )

    private fun renderDropLocations() {
        locations.forEachIndexed { index, (position, name) ->
            if (!KuudraUtils.supplies[index]) return@forEachIndexed
            Renderer.drawCustomBeacon("", position, if (missing == name) Color.GREEN else Color.RED, increase = false)
        }
    }
}