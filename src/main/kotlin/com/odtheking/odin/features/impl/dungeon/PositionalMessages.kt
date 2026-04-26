package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ListSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onSend
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.render.drawCylinder
import com.odtheking.odin.utils.render.drawText
import com.odtheking.odin.utils.render.drawWireFrameBox
import com.odtheking.odin.utils.sendCommand
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import java.util.*
import kotlin.concurrent.schedule

object PositionalMessages : Module(
    name = "Positional Messages",
    description = "Sends a message when you're near a certain position. /posmsg"
) {
    private val onlyDungeons by BooleanSetting("Only in Dungeons", true, desc = "Only sends messages when you're in a dungeon.")
    private val oncePerWorld by BooleanSetting("Once Per World", false, desc = "Whether or not to only send each message once per world.")
    private val showPositions by BooleanSetting("Show Positions", true, desc = "Draws boxes/lines around the positions.")
    private val cylinderHeight by NumberSetting("Height", 0.2, 0.1, 5.0, 0.1, desc = "Height of the cylinder for in messages.").withDependency { showPositions }
    private val depthCheck by BooleanSetting("Depth Check", true, desc = "Whether or not the boxes should be seen through walls. False = Through walls.").withDependency { showPositions }
    private val displayMessage by BooleanSetting("Show Message", true, desc = "Whether or not to display the message in the box.").withDependency { showPositions }
    private val messageSize by NumberSetting("Message Size", 1f, 0.1f, 4f, 0.1f, desc = "Whether or not to display the message size in the box.").withDependency { showPositions && displayMessage }

    data class PosMessage(val x: Double, val y: Double, val z: Double, val x2: Double?, val y2: Double?, val z2: Double?, val delay: Long, val distance: Double?, val color: Color, val message: String)
    val posMessageStrings by ListSetting("Pos Messages", mutableListOf<PosMessage>())
    private val sentMessages = mutableMapOf<PosMessage, Boolean>()

    init {
        onSend<ServerboundMovePlayerPacket> {
            posMessageSend()
        }

        on<RenderEvent.Extract> {
            if (!showPositions || (onlyDungeons && !DungeonUtils.inDungeons)) return@on
            val player = mc.player ?: return@on
            posMessageStrings.forEach { message ->
                val distanceToMessage = if (message.distance != null)
                    player.distanceToSqr(message.x, message.y, message.z)
                else {
                    val centerX = (message.x + (message.x2 ?: message.x)) / 2
                    val centerY = (message.y + (message.y2 ?: message.y)) / 2
                    val centerZ = (message.z + (message.z2 ?: message.z)) / 2
                    player.distanceToSqr(centerX, centerY, centerZ)
                }
                if (distanceToMessage > 1024) return@forEach

                if (message.distance != null) {
                    drawCylinder(Vec3(message.x, message.y, message.z), message.distance.toFloat(), cylinderHeight.toFloat(), color = message.color, depth = depthCheck)
                    if (displayMessage) drawText(message.message, Vec3(message.x, message.y + 1, message.z), messageSize, depthCheck)
                } else {
                    val box = AABB(message.x, message.y, message.z, message.x2 ?: return@forEach, message.y2 ?: return@forEach,message.z2  ?: return@forEach)
                    drawWireFrameBox(box, message.color, depth = depthCheck)
                    if (!displayMessage) return@forEach
                    val center = Vec3((message.x + message.x2) / 2, (message.y + message.y2) / 2, (message.z + message.z2) / 2)
                    drawText(message.message, center.add(0.0, 1.0, 0.0), messageSize, depthCheck)
                }
            }
        }

        on<LevelEvent.Load> {
            if (oncePerWorld) sentMessages.forEach { (message) -> sentMessages[message] = false }
        }
    }

    private fun posMessageSend() {
        if (onlyDungeons && !DungeonUtils.inDungeons) return
        posMessageStrings.forEach { message ->
            message.x2?.let { handleInString(message) } ?: handleAtString(message)
        }
    }

    private fun handleAtString(posMessage: PosMessage) {
        val msgSent = sentMessages.getOrDefault(posMessage, false)
        val player = mc.player ?: return
        if (player.distanceToSqr(posMessage.x, posMessage.y, posMessage.z) <= (posMessage.distance ?: return)) {
            if (!msgSent) Timer().schedule(posMessage.delay) {
                if (player.distanceToSqr(posMessage.x, posMessage.y, posMessage.z) <= posMessage.distance)
                    sendCommand("pc ${posMessage.message}")
            }
            sentMessages[posMessage] = true
        } else if (!oncePerWorld) sentMessages[posMessage] = false
    }

    private fun handleInString(posMessage: PosMessage) {
        val position = mc.player?.position() ?: return
        val msgSent = sentMessages.getOrDefault(posMessage, false)
        if (mc.player != null && AABB(posMessage.x, posMessage.y, posMessage.z, posMessage.x2 ?: return, posMessage.y2 ?: return, posMessage.z2 ?: return).contains(position)) {
            if (!msgSent) Timer().schedule(posMessage.delay) {
                if (AABB(posMessage.x, posMessage.y, posMessage.z, posMessage.x2, posMessage.y2, posMessage.z2).contains(position))
                    sendCommand("pc ${posMessage.message}")
            }
            sentMessages[posMessage] = true
        } else if (!oncePerWorld) sentMessages[posMessage] = false
    }
}