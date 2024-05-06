package me.odinmain.features.impl.dungeon

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.events.impl.PacketSentEvent
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ListSetting
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.skyblock.partyMessage
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*
import kotlin.concurrent.schedule

object PosMessages : Module(
    name = "Positional Messages",
    category = Category.DUNGEON,
    description = "Sends a message when you're near a certain position. /posmsg"
) {
    private val onlyDungeons: Boolean by BooleanSetting("Only in Dungeons", true, description = "Only sends messages when you're in a dungeon.")

    data class PosMessage(val x: Double, val y: Double, val z: Double, val delay: Long, val message: String)
    val posMessageStrings: MutableList<String> by ListSetting("Pos Messages Strings", mutableListOf())
    private var sending = false

    @SubscribeEvent
    fun posMessageSend(event: PacketSentEvent) {
        if (event.packet !is C04PacketPlayerPosition || (onlyDungeons && DungeonUtils.inDungeons) || !LocationUtils.inSkyblock) return
        if (posMessageStrings.any {
            val msg = parsePosString(it) ?: return
            mc.thePlayer.getDistance(msg.x, msg.y, msg.z) <= 1
        }) {
            posMessageStrings.filter {
                val msg = parsePosString(it) ?: return
                mc.thePlayer.getDistance(msg.x, msg.y, msg.z) <= 1
            }.forEach {
                val msg = parsePosString(it) ?: return
                if (!sending) Timer().schedule(msg.delay) {
                    if (mc.thePlayer.getDistance(msg.x, msg.y, msg.z) <= 1) partyMessage(msg.message)
                }
            }
            sending = true
        } else sending = false
    }


    private fun parsePosString(posMessageString: String): PosMessage? {
        val map = posMessageString.split(",").map { it.trim().split(":") }
        val finalMap = map.associate { it[0] to it[1] }
        val x = finalMap["x"]?.toDoubleOrNull() ?: return null.also { modMessage("Failed to parse x: ${finalMap["x"]}")}
        val y = finalMap["y"]?.toDoubleOrNull() ?: return null.also { modMessage("Failed to parse y: ${finalMap["y"]}")}
        val z = finalMap["z"]?.toDoubleOrNull() ?: return null.also { modMessage("Failed to parse z: ${finalMap["z"]}")}
        val delay = finalMap["delay"]?.trim()?.toLongOrNull() ?: return null.also { modMessage("Failed to parse delay: ${finalMap["delay"]}")}
        val message = finalMap["message"]?.replace("\"", "") ?: return null.also { modMessage("Failed to parse message: ${finalMap["message"]}")}
        return PosMessage(x, y, z, delay, message)
    }

}