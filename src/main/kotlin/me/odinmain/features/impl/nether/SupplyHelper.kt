package me.odinmain.features.impl.nether

import me.odinmain.clickgui.settings.Setting.Companion.withDependency
import me.odinmain.clickgui.settings.impl.BooleanSetting
import me.odinmain.clickgui.settings.impl.ColorSetting
import me.odinmain.features.Module
import me.odinmain.features.impl.nether.NoPre.missing
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.formatTime
import me.odinmain.utils.render.Colors
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.KuudraUtils
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.toVec3
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.cos
import kotlin.math.sin

object SupplyHelper : Module(
    name = "Supply Helper",
    description = "Provides visual aid for supply drops in Kuudra."
) {
    private val suppliesWaypoints by BooleanSetting("Supplies Waypoints", true, desc = "Renders the supply waypoints.")
    private val supplyWaypointColor by ColorSetting("Supply Waypoint Color", Colors.MINECRAFT_YELLOW, true, desc = "Color of the supply waypoints.").withDependency { suppliesWaypoints }
    private val supplyDropWaypoints by BooleanSetting("Supply Drop Waypoints", true, desc = "Renders the supply drop waypoints.")
    private val sendSupplyTime by BooleanSetting("Send Supply Time", true, desc = "Sends a message when a supply is collected.")

    private var startRun = 0L
    private val supplyPickUpRegex = Regex("(?:\\[[^]]*])? ?(\\w{1,16}) recovered one of Elle's supplies! \\((\\d)/(\\d)\\)") // https://regex101.com/r/xsDImP/1

    init {
        onMessage(Regex("\\[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!")) {
            startRun = System.currentTimeMillis()
        }

        onMessage(supplyPickUpRegex) {
            if (!sendSupplyTime || !KuudraUtils.inKuudra || KuudraUtils.phase != 1) return@onMessage
            val (name, current, total) = it.destructured
            modMessage("§6$name §a§lrecovered a supply in ${formatTime((System.currentTimeMillis() - startRun))}! §r§8($current/$total)", "")
        }
    }

    @SubscribeEvent
    fun onChatMessage(event: ClientChatReceivedEvent) {
        if (sendSupplyTime && KuudraUtils.inKuudra && KuudraUtils.phase == 1 && supplyPickUpRegex.matches(event.message.unformattedText))
            event.isCanceled = true
    }

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        //if (!KuudraUtils.inKuudra || KuudraUtils.phase != 1) return
        if (supplyDropWaypoints) {
            Supply.entries.forEach { type ->
                if (type.equalsOneOf(Supply.None, Supply.Square) || !type.isActive) return@forEach
                Renderer.drawCustomBeacon("", type.dropOffSpot.toVec3(), if (missing == type) Colors.MINECRAFT_GREEN else Colors.MINECRAFT_RED, increase = false, distance = false)
            }
        }

        if (suppliesWaypoints) {
            KuudraUtils.giantZombies.forEach {
                Renderer.drawCustomBeacon("Supply",
                    Vec3(it.posX + (3.7 * cos((it.rotationYaw + 130) * (Math.PI / 180))), 73.0, it.posZ + (3.7 * sin((it.rotationYaw + 130) * (Math.PI / 180)))), supplyWaypointColor, increase = false)
            }
        }
    }
}