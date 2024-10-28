package me.odinmain.features.impl.dungeon

import me.odinmain.events.impl.PacketSentEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ListSetting
import me.odinmain.utils.isVecInXZ
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
    private val onlyDungeons by BooleanSetting("Only in Dungeons", true, description = "Only sends messages when you're in a dungeon.")

    data class PosMessage(val x: Double, val y: Double, val z: Double, val x2: Double?, val y2: Double?, val z2: Double?, val delay: Long, val distance: Double?, val message: String)
    val posMessageStrings: MutableList<String> by ListSetting("Pos Messages Strings", mutableListOf())
    private val sentMessages = mutableMapOf<PosMessage, Boolean>()

    val parsedStrings: MutableList<PosMessage> = mutableListOf()

    @SubscribeEvent
    fun posMessageSend(event: PacketSentEvent) {
        if (event.packet !is C04PacketPlayerPosition || (onlyDungeons && !DungeonUtils.inDungeons)) return
        parsedStrings.forEach { message ->
            message.x2?.let { handleInString(message) } ?: handleAtString(message)
        }
    }

    private var parsed = false

    init {
        onWorldLoad {
            if (parsed) return@onWorldLoad
            posMessageStrings.forEach { findParser(it, true) }
            parsed = true
        }
    }

    private val atRegex = Regex("x: (.*), y: (.*), z: (.*), delay: (.*), distance: (.*), message: \"(.*)\"")

    fun findParser(posMessageString: String, addToList: Boolean): PosMessage? {
        val message = if (posMessageString.matches(atRegex)) parseAtString(posMessageString) else parseInString(posMessageString)
        if (addToList) message?.let { parsedStrings.add(it) }
        return message
    }

    private fun handleAtString(posMessage: PosMessage) {
        val msgSent = sentMessages.getOrDefault(posMessage, false)
        if (mc.thePlayer != null && mc.thePlayer.getDistance(posMessage.x, posMessage.y, posMessage.z) <= (posMessage.distance ?: return)) {
            if (!msgSent) Timer().schedule(posMessage.delay) {
                if (mc.thePlayer.getDistance(posMessage.x, posMessage.y, posMessage.z) <= posMessage.distance)
                    partyMessage(posMessage.message)
            }
            sentMessages[posMessage] = true
        } else sentMessages[posMessage] = false
    }

    private fun handleInString(posMessage: PosMessage) {
        val msgSent = sentMessages.getOrDefault(posMessage, false)
        if (mc.thePlayer != null && isVecInXZ(mc.thePlayer.positionVector, AxisAlignedBB(posMessage.x, posMessage.y, posMessage.z, posMessage.x2 ?: return, posMessage.y2 ?: return, posMessage.z2 ?: return))) {
            if (!msgSent) Timer().schedule(posMessage.delay) {
                if (isVecInXZ(mc.thePlayer.positionVector, AxisAlignedBB(posMessage.x, posMessage.y, posMessage.z, posMessage.x2, posMessage.y2, posMessage.z2)))
                    partyMessage(posMessage.message)
            }
            sentMessages[posMessage] = true
        } else sentMessages[posMessage] = false
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