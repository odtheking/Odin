package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.ListSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.RenderExtractEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onSend
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color
import com.odtheking.odin.utils.handlers.schedule
import com.odtheking.odin.utils.render.drawCylinder
import com.odtheking.odin.utils.render.drawText
import com.odtheking.odin.utils.render.drawWireFrameBox
import com.odtheking.odin.utils.sendCommand
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

object PositionalMessages : Module(
    name = "Positional Messages",
    description = "Sends a message when you're near a certain position. /posmsg"
) {
    private val onlyDungeons by BooleanSetting("Only in Dungeons", true, desc = "Only sends messages when you're in a dungeon.")
    private val showPositions by BooleanSetting("Show Positions", true, desc = "Draws boxes/lines around the positions.")
    private val cylinderHeight by NumberSetting("Height", 0.2f, 0.1..5.0, 0.1, desc = "Height of the cylinder for in messages.").withDependency { showPositions }
    private val displayMessage by BooleanSetting("Show Message", true, desc = "Whether or not to display the message in the box.").withDependency { showPositions }
    private val messageSize by NumberSetting("Message Size", 1f, 0.1..4.0, 0.1f, desc = "The size at which to display the message in the box.").withDependency { showPositions && displayMessage }

    data class PosMessage(val x: Double, val y: Double, val z: Double, val x2: Double?, val y2: Double?, val z2: Double?, val delay: Int, val distance: Double?, val color: Color, val message: String, val send: Boolean) {
        @Transient
        private var _center: Vec3? = null
        val center: Vec3
            get() = _center ?: Vec3((x + (x2 ?: x)) / 2, (y + (y2 ?: y)) / 2, (z + (z2 ?: z)) / 2).also { _center = it }

        @Transient
        private var _box: AABB? = null
        val box: AABB?
            get() {
                if (_box == null && x2 != null && y2 != null && z2 != null) _box = AABB(x, y, z, x2, y2, z2)
                return _box
            }

        @Transient
        private var _radiusSquared: Double? = null
        val radiusSquared: Double?
            get() {
                if (_radiusSquared == null) distance?.let { _radiusSquared = it * it }
                return _radiusSquared
            }
    }
    val posMessageStrings by ListSetting("Pos Messages", mutableListOf<PosMessage>())
    private val sentMessages = mutableSetOf<PosMessage>()

    init {
        onSend<ServerboundMovePlayerPacket> {
            if (onlyDungeons && !DungeonUtils.inBoss) return@onSend
            posMessageStrings.forEach { posMessage ->
                if (posMessage.send && posMessage !in sentMessages) posMessage.x2?.let { handleInString(posMessage) } ?: handleAtString(posMessage)
            }
        }

        on<RenderExtractEvent> {
            if (!showPositions || (onlyDungeons && !DungeonUtils.inBoss)) return@on
            posMessageStrings.forEach { posMessage ->
                if (posMessage.distance != null) {
                    drawCylinder(posMessage.center, posMessage.distance.toFloat(), cylinderHeight, color = posMessage.color, depth = true)
                    if (displayMessage) drawText(posMessage.message, Vec3(posMessage.x, posMessage.y + 1, posMessage.z), messageSize, true)
                } else {
                    drawWireFrameBox(posMessage.box ?: return@forEach, posMessage.color, depth = true)
                    if (displayMessage) drawText(posMessage.message, posMessage.center.add(0.0, 1.0, 0.0), messageSize, true)
                }
            }
        }

        on<LevelEvent.Load> {
            sentMessages.clear()
        }
    }

    private fun handleAtString(posMessage: PosMessage) {
        val player = mc.player ?: return
        val radiusSquared = posMessage.radiusSquared ?: return
        if (player.distanceToSqr(posMessage.x, posMessage.y, posMessage.z) > radiusSquared) return
        sentMessages.add(posMessage)
        schedule(posMessage.delay) { sendCommand("pc ${posMessage.message}") }
    }

    private fun handleInString(posMessage: PosMessage) {
        val aabb = posMessage.box ?: return
        val position = mc.player?.position() ?: return
        if (!aabb.contains(position)) return
        sentMessages.add(posMessage)
        schedule(posMessage.delay) { sendCommand("pc ${posMessage.message}") }
    }
}