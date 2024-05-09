package me.odinmain.features.impl.dungeon

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.events.impl.PacketSentEvent
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ListSetting
import me.odinmain.utils.isVecInXZ
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.skyblock.partyMessage
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.util.AxisAlignedBB
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*
import kotlin.concurrent.schedule

object PosMessages : Module(
    name = "Positional Messages",
    category = Category.DUNGEON,
    description = "Sends a message when you're near a certain position. /posmsg"
) {
    private val onlyDungeons: Boolean by BooleanSetting("Only in Dungeons", true, description = "Only sends messages when you're in a dungeon.")

    data class PosMessage(val x: Double, val y: Double, val z: Double, val x2: Double?, val y2: Double?, val z2: Double?, val delay: Long, val distance: Double?, val message: String)
    val posMessageStrings: MutableList<String> by ListSetting("Pos Messages Strings", mutableListOf())
    private val sentMessages = mutableMapOf<String, Boolean>()


    @SubscribeEvent
    fun posMessageSend(event: PacketSentEvent) {
        if (event.packet !is C04PacketPlayerPosition || (onlyDungeons && !DungeonUtils.inDungeons) || !LocationUtils.inSkyblock) return
        posMessageStrings.forEach {
            findParser(it)
        }
    }

    private fun findParser(posMessageString: String) {
        val atRegex = Regex("x: (.*), y: (.*), z: (.*), delay: (.*), distance: (.*), message: \"(.*)\"")
        val inRegex = Regex("x: (.*), y: (.*), z: (.*), x2: (.*), y2: (.*), z2: (.*), delay: (.*), message: \"(.*)\"")
        if (posMessageString.matches(atRegex)) handleAtString(posMessageString) else handleInString(posMessageString)
    }

    private fun handleAtString(posMessageString: String) {
        val parsedmsg = parseAtString(posMessageString) ?: return
        val msgSent = sentMessages.getOrDefault(posMessageString, false)
        if (mc.thePlayer != null && mc.thePlayer.getDistance(parsedmsg.x, parsedmsg.y, parsedmsg.z) <= (parsedmsg.distance ?: return)) {
            if (!msgSent) Timer().schedule(parsedmsg.delay) {
                if (mc.thePlayer.getDistance(parsedmsg.x, parsedmsg.y, parsedmsg.z) <= parsedmsg.distance)
                    partyMessage(parsedmsg.message)
            }
            sentMessages[posMessageString] = true
        } else sentMessages[posMessageString] = false
    }

    private fun handleInString(posMessageString: String) {
        val parsedmsg = parseInString(posMessageString) ?: return
        val msgSent = sentMessages.getOrDefault(posMessageString, false)
        if (mc.thePlayer != null && isVecInXZ(mc.thePlayer.positionVector, AxisAlignedBB(parsedmsg.x, parsedmsg.y, parsedmsg.z, parsedmsg.x2 ?: return, parsedmsg.y2 ?: return, parsedmsg.z2 ?: return))) {
            if (!msgSent) Timer().schedule(parsedmsg.delay) {
                if (isVecInXZ(mc.thePlayer.positionVector, AxisAlignedBB(parsedmsg.x, parsedmsg.y, parsedmsg.z, parsedmsg.x2, parsedmsg.y2, parsedmsg.z2))) {
                    partyMessage(parsedmsg.message)
                }
            }
            sentMessages[posMessageString] = true
        } else sentMessages[posMessageString] = false
    }

    private fun parseAtString(posMessageString: String): PosMessage? {
        val regex = Regex("x: (.*), y: (.*), z: (.*), delay: (.*), distance: (.*), message: \"(.*)\"")
        val matchResult = regex.matchEntire(posMessageString) ?: return null
        val (x, y, z, delay, distance, message) = matchResult.destructured
        val xDouble = x.toDoubleOrNull() ?: return null.also { modMessage("Failed to parse x: $x") }
        val yDouble = y.toDoubleOrNull() ?: return null.also { modMessage("Failed to parse y: $y") }
        val zDouble = z.toDoubleOrNull() ?: return null.also { modMessage("Failed to parse z: $z") }
        val delayLong = delay.toLongOrNull() ?: return null.also { modMessage("Failed to parse delay: $delay") }
        val distanceDouble = distance.toDoubleOrNull() ?: return null.also { modMessage("Failed to parse distance: $distance")}
        return PosMessage(xDouble, yDouble, zDouble, null, null, null, delayLong, distanceDouble, message)
    }

    private fun parseInString(posMessageString: String): PosMessage? {
        val inRegex = Regex("x: (.*), y: (.*), z: (.*), x2: (.*), y2: (.*), z2: (.*), delay: (.*), message: \"(.*)\"")
        val matchResult = inRegex.matchEntire(posMessageString) ?: return null
        val (x, y, z, x2, y2, z2, delay, message) = matchResult.destructured
        val xDouble = x.toDoubleOrNull() ?: return null.also { modMessage("Failed to parse x: $x") }
        val yDouble = y.toDoubleOrNull() ?: return null.also { modMessage("Failed to parse y: $y") }
        val zDouble = z.toDoubleOrNull() ?: return null.also { modMessage("Failed to parse z: $z") }
        val x2Double = x2.toDoubleOrNull() ?: return null.also { modMessage("Failed to parse x2: $x2") }
        val y2Double = y2.toDoubleOrNull() ?: return null.also { modMessage("Failed to parse y2: $y2") }
        val z2Double = z2.toDoubleOrNull() ?: return null.also { modMessage("Failed to parse z2: $z2") }
        val delayLong = delay.toLongOrNull() ?: return null.also { modMessage("Failed to parse delay: $delay") }
        return PosMessage(xDouble, yDouble, zDouble, x2Double, y2Double, z2Double, delayLong, null, message)
    }

}