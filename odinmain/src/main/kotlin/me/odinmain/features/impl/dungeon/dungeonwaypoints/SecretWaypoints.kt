package me.odinmain.features.impl.dungeon.dungeonwaypoints

import me.odinmain.config.DungeonWaypointConfig
import me.odinmain.events.impl.SecretPickupEvent
import me.odinmain.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.WaypointType
import me.odinmain.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.getWaypoints
import me.odinmain.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.glList
import me.odinmain.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.lastEtherPos
import me.odinmain.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.lastEtherTime
import me.odinmain.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.setWaypoints
import me.odinmain.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.toVec3
import me.odinmain.utils.*
import me.odinmain.utils.skyblock.devMessage
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.block.BlockChest
import net.minecraft.block.state.IBlockState
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3

object SecretWaypoints {

    private var lastClicked: BlockPos? = null

    fun onLocked() {
        val room = DungeonUtils.currentFullRoom ?: return
        val vec = Vec3(lastClicked ?: return).subtractVec(x = room.clayPos.x, z = room.clayPos.z).rotateToNorth(room.room.rotation)
        getWaypoints(room).find { wp -> wp.toVec3().equal(vec) && wp.secret && wp.clicked }?.let {
            it.clicked = false
            setWaypoints(room)
            devMessage("unclicked ${it.toVec3()}")
            glList = -1
            lastClicked = null
        }
    }

    fun onSecret(event: SecretPickupEvent) {
        when (event) {
            is SecretPickupEvent.Interact -> clickSecret(Vec3(event.blockPos), 0, event.blockState)
            is SecretPickupEvent.Bat -> clickSecret(event.packet.positionVector, 5)
            is SecretPickupEvent.Item -> clickSecret(event.entity.positionVector, 3)
        }
    }

    fun onEtherwarp(packet: S08PacketPlayerPosLook) {
        if (!DungeonUtils.inDungeons) return
        val etherpos = lastEtherPos?.pos?.toVec3() ?: return
        if (System.currentTimeMillis() - lastEtherTime > 1000) return
        val pos = Vec3(packet.x, packet.y, packet.z)
        if (pos.distanceTo(etherpos) > 3) return
        val room = DungeonUtils.currentFullRoom ?: return
        val vec = etherpos.subtractVec(x = room.clayPos.x, z = room.clayPos.z).rotateToNorth(room.room.rotation)
        getWaypoints(room).find { wp -> wp.toVec3().equal(vec) && wp.type == WaypointType.ETHERWARP }?.let {
            it.clicked = true
            setWaypoints(room)
            glList = -1
            lastEtherTime = 0L
            lastEtherPos = null
        }
    }

    private fun clickSecret(pos: Vec3, distance: Int, block: IBlockState? = null) {
        val room = DungeonUtils.currentFullRoom ?: return
        val vec = pos.subtractVec(x = room.clayPos.x, z = room.clayPos.z).rotateToNorth(room.room.rotation)

        val waypoint = if (distance == 0) getWaypoints(room).find { wp -> wp.toVec3().equal(vec) && wp.secret && !wp.clicked }
        else getWaypoints(room).filter { it.secret && !it.clicked }
            .minByOrNull { wp -> wp.toVec3().distanceTo(vec).takeIf { it <= distance } ?: Double.MAX_VALUE }

        waypoint?.let {
            if (block?.block is BlockChest) lastClicked = BlockPos(pos)
            it.clicked = true
            setWaypoints(room)
            devMessage("clicked ${it.toVec3()}")
            glList = -1
        }
    }

    fun resetSecrets() {
        DungeonWaypointConfig.waypoints.entries.forEach { (_, room) ->
            room.forEach { it.clicked = false }
        }

        DungeonUtils.currentFullRoom?.let { setWaypoints(it) }
        glList = -1
    }
}