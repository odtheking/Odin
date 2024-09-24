package me.odinmain.features.impl.dungeon.dungeonwaypoints

import me.odinmain.config.DungeonWaypointConfigCLAY
import me.odinmain.events.impl.SecretPickupEvent
import me.odinmain.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.getWaypoints
import me.odinmain.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.glList
import me.odinmain.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.setWaypoints
import me.odinmain.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.toVec3
import me.odinmain.utils.*
import me.odinmain.utils.skyblock.devMessage
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.block.BlockChest
import net.minecraft.block.state.IBlockState
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

    private fun clickSecret(pos: Vec3, distance: Int, block: IBlockState? = null) {
        val room = DungeonUtils.currentFullRoom ?: return
        val vec = pos.subtractVec(x = room.clayPos.x, z = room.clayPos.z).rotateToNorth(room.room.rotation)

        getWaypoints(room).find { wp -> (if (distance == 0) wp.toVec3().equal(vec) else wp.toVec3().distanceTo(vec) <= distance) && wp.secret && !wp.clicked}?.let {
            if (block?.block is BlockChest) lastClicked = BlockPos(pos)
            it.clicked = true
            setWaypoints(room)
            devMessage("clicked ${it.toVec3()}")
            glList = -1
        }
    }

    fun resetSecrets() {
        val room = DungeonUtils.currentFullRoom
        for (waypointsList in DungeonWaypointConfigCLAY.waypoints.filter { waypoints -> waypoints.value.any { it.clicked } }.values) {
            waypointsList.filter { it.clicked }.forEach { it.clicked = false }
        }

        room?.let { setWaypoints(it) }
        glList = -1
    }
}