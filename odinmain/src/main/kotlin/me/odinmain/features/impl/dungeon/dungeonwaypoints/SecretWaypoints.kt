package me.odinmain.features.impl.dungeon.dungeonwaypoints

import me.odinmain.config.DungeonWaypointConfigCLAY
import me.odinmain.events.impl.SecretPickupEvent
import me.odinmain.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.glList
import me.odinmain.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.toVec3
import me.odinmain.utils.equal
import me.odinmain.utils.rotateToNorth
import me.odinmain.utils.skyblock.devMessage
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.ScanUtils
import me.odinmain.utils.subtractVec
import net.minecraft.util.Vec3

object SecretWaypoints {

    fun onSecret(event: SecretPickupEvent) {
        when (event) {
            is SecretPickupEvent.Interact -> clickSecret(Vec3(event.blockPos), 0)
            is SecretPickupEvent.Bat -> clickSecret(event.entity.positionVector, 5)
            is SecretPickupEvent.Item -> clickSecret(event.entity.positionVector, 3)
        }
    }

    private fun clickSecret(pos: Vec3, distance: Int) {
        val room = DungeonUtils.currentRoom ?: return
        val vec = Vec3(pos.xCoord, pos.yCoord, pos.zCoord).subtractVec(x = room.clayPos.x, z = room.clayPos.z).rotateToNorth(room.room.rotation)
        val waypoints = DungeonWaypointConfigCLAY.waypoints.getOrPut(room.room.data.name) { mutableListOf() }
        waypoints.find { wp -> (if (distance == 0) wp.toVec3().equal(vec) else wp.toVec3().distanceTo(vec) <= distance) && wp.secret && !wp.clicked}?.let {
            it.clicked = true
            ScanUtils.setWaypoints(room)
            devMessage("clicked $vec")
            glList = -1
        }
    }

    fun resetSecrets() {
        val room = DungeonUtils.currentRoom
        for ((_, waypointsList) in DungeonWaypointConfigCLAY.waypoints.filter { waypoints -> waypoints.value.any { it.clicked } }) {
            waypointsList.filter { it.clicked }.forEach { it.clicked = false }
        }

        if (room != null) ScanUtils.setWaypoints(room)
        glList = -1
    }

    fun clearSecrets() {
        val room = DungeonUtils.currentRoom ?: return
        val waypoints = DungeonWaypointConfigCLAY.waypoints.getOrPut(room.room.data.name) { mutableListOf() }
        if (waypoints.any { it.secret && !it.clicked}) {
            for (wp in waypoints.filter { it.secret && !it.clicked }) { wp.clicked = true }
            ScanUtils.setWaypoints(room)
            glList = -1
        }
    }

}