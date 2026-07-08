package com.odtheking.odin.features.impl.nether
import com.odtheking.odin.utils.center

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ColorSetting
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.ChatManager.hideMessage
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.formatTime
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.render.drawCustomBeacon
import com.odtheking.odin.utils.render.drawText
import com.odtheking.odin.utils.skyblock.KuudraUtils
import com.odtheking.odin.utils.skyblock.Supply
import net.minecraft.core.BlockPos
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
    private val renderArea by BooleanSetting("Render Area", true, desc = "Renders the area where supplies can be collected.").withDependency { supplyDropWaypoints }

    private val supplyPickUpRegex =
        Regex("(?:\\[[^]]*])? ?(\\w{1,16}) recovered one of Elle's supplies! \\((\\d)/(\\d)\\)") // https://regex101.com/r/xsDImP/1
    private val runStartRegex = Regex("^\\[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!$")
    private var startRun = 0L

    init {
        on<ChatPacketEvent> {
            if (!KuudraUtils.inKuudra || !sendSupplyTime) return@on

            when {
                runStartRegex.matches(value) -> startRun = System.currentTimeMillis()

                supplyPickUpRegex.matches(value) -> {
                    if (KuudraUtils.phase != 1) return@on
                    val (name, current, total) = supplyPickUpRegex.find(value)?.destructured ?: return@on
                    modMessage("§6$name §a§lrecovered a supply in ${formatTime(System.currentTimeMillis() - startRun)}! §r§8($current/$total)", "")
                    hideMessage()
                }
            }
        }

        on<RenderEvent.Extract> {
            if (!KuudraUtils.inKuudra || KuudraUtils.phase != 1) return@on
            if (supplyDropWaypoints) {
                Supply.entries.forEach { type ->
                    if (type.equalsOneOf(Supply.None, Supply.Square) || !type.isActive) return@forEach
                    drawCustomBeacon(
                        "§ePlace Here!",
                        type.dropOffSpot,
                        if (NoPre.missing == type) Colors.MINECRAFT_GREEN else Colors.MINECRAFT_RED,
                        increase = false, distance = false
                    )
                }
            }

            if (suppliesWaypoints) {
                KuudraUtils.giantZombies.forEach {
                    drawCustomBeacon(
                        "Pick Up!",
                        BlockPos((it.x + (3.7 * cos((it.yRot + 130) * (Math.PI / 180)))).toInt(), 73, ((it.z + (3.7 * sin((it.yRot + 130) * (Math.PI / 180)))).toInt())),
                        supplyWaypointColor, increase = false
                    )
                }
            }

            if (renderArea) {
                Supply.entries.forEach { type ->
                    drawText(
                        "§e${type.name}",
                        type.pickUpSpot.center, 2f, true
                    )
                }
            }
        }
    }
}