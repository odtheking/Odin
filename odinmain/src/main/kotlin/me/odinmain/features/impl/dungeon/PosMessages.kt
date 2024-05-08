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
    private val sentMessages = mutableMapOf<String, Boolean>()


    @SubscribeEvent
    fun onPosUpdate(event: PacketSentEvent) {
        if (event.packet !is C04PacketPlayerPosition || (onlyDungeons && !DungeonUtils.inDungeons) || !LocationUtils.inSkyblock) return
        posMessageStrings.forEach {
            val msg = parsePosString(it) ?: return@forEach
            val messageSent = sentMessages.getOrDefault(it, false)
            if (mc.thePlayer != null && mc.thePlayer.getDistance(msg.x, msg.y, msg.z) <= 1) {
                if (!messageSent) Timer().schedule(msg.delay) {
                    if (mc.thePlayer.getDistance(msg.x, msg.y, msg.z) <= 1)
                    partyMessage(msg.message)
                }
                sentMessages[it] = true
            } else sentMessages[it] = false
        }
    }


    private fun parsePosString(posMessageString: String): PosMessage? {
        val regex = Regex("x: (.*), y: (.*), z: (.*), delay: (.*), message: \"(.*)\"")
        val matchResult = regex.matchEntire(posMessageString) ?: return null
        val (x, y, z, delay, message) = matchResult.destructured
        val xDouble = x.toDoubleOrNull() ?: return null.also { modMessage("Failed to parse x: $x") }
        val yDouble = y.toDoubleOrNull() ?: return null.also { modMessage("Failed to parse y: $y") }
        val zDouble = z.toDoubleOrNull() ?: return null.also { modMessage("Failed to parse z: $z") }
        val delayLong = delay.toLongOrNull() ?: return null.also { modMessage("Failed to parse delay: $delay") }
        return PosMessage(xDouble, yDouble, zDouble, delayLong, message)
    }

}