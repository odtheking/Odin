package me.odinmain.features.impl.dungeon

import me.odinmain.events.impl.PacketEvent
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ListSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.isVecInAABB
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.partyMessage
import me.odinmain.utils.ui.Colors
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*
import kotlin.concurrent.schedule

object PositionalMessages : Module(
    name = "Positional Messages",
    desc = "Sends a message when you're near a certain position. /posmsg"
) {
    private val onlyDungeons by BooleanSetting("Only in Dungeons", true, desc = "Only sends messages when you're in a dungeon.")
    private val showPositions by BooleanSetting("Show Positions", true, desc = "Draws boxes/lines around the positions.")
    private val cylinderHeight by NumberSetting("Height", 0.2, 0.1, 5.0, 0.1, desc = "Height of the cylinder for in messages.").withDependency { showPositions }
    private val boxThickness by NumberSetting("Box line width", 1f, 0.1f, 5f, 0.1f, desc = "Line width of the box for at messages.").withDependency { showPositions }
    private val depthCheck by BooleanSetting("Depth Check", true, desc = "Whether or not the boxes should be seen through walls. False = Through walls.").withDependency { showPositions }
    private val displayMessage by BooleanSetting("Show Message", true, desc = "Whether or not to display the message in the box.").withDependency { showPositions }
    private val messageSize by NumberSetting("Message Size", 1f, 0.1f, 4f, 0.1f, desc = "Whether or not to display the message size in the box.").withDependency { showPositions && displayMessage }

    data class PosMessage(val x: Double, val y: Double, val z: Double, val x2: Double?, val y2: Double?, val z2: Double?, val delay: Long, val distance: Double?, val color: Color, val message: String)
    val posMessageStrings by ListSetting("Pos Messages", mutableListOf<PosMessage>())
    private val sentMessages = mutableMapOf<PosMessage, Boolean>()

    @SubscribeEvent
    fun posMessageSend(event: PacketEvent.Send) {
        if (event.packet !is C04PacketPlayerPosition || (onlyDungeons && !DungeonUtils.inDungeons)) return
        posMessageStrings.forEach {  message ->
            message.x2?.let { handleInString(message) } ?: handleAtString(message)
        }
    }

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        if (!showPositions || (onlyDungeons && !DungeonUtils.inDungeons)) return
        posMessageStrings.forEach { message ->
            if (message.distance != null) {
                Renderer.drawCylinder(Vec3(message.x, message.y, message.z), message.distance, message.distance, cylinderHeight, 40f, 1f, 0f, 90f, 90f, message.color, depthCheck)
                if (displayMessage) Renderer.drawStringInWorld(message.message, Vec3(message.x, message.y + 0.5, message.z), Colors.WHITE, depthCheck, 0.03f * messageSize)
            } else {
                val aabb = AxisAlignedBB(message.x, message.y, message.z, message.x2 ?: return@forEach, message.y2 ?: return@forEach,message.z2  ?: return@forEach)
                Renderer.drawBox(aabb, message.color, boxThickness, fillAlpha = 0f, depth = depthCheck)
                if (!displayMessage) return@forEach
                val center = Vec3(
                    (message.x + message.x2) / 2,
                    (message.y + message.y2) / 2,
                    (message.z + message.z2) / 2
                )
                Renderer.drawStringInWorld(message.message, center, Colors.WHITE, depthCheck, 0.03f * messageSize)
            }
        }
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
        if (mc.thePlayer != null && isVecInAABB(mc.thePlayer.positionVector, AxisAlignedBB(posMessage.x, posMessage.y, posMessage.z, posMessage.x2 ?: return, posMessage.y2 ?: return, posMessage.z2 ?: return))) {
            if (!msgSent) Timer().schedule(posMessage.delay) {
                if (isVecInAABB(mc.thePlayer.positionVector, AxisAlignedBB(posMessage.x, posMessage.y, posMessage.z, posMessage.x2, posMessage.y2, posMessage.z2)))
                    partyMessage(posMessage.message)
            }
            sentMessages[posMessage] = true
        } else sentMessages[posMessage] = false
    }
}