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
import net.minecraft.util.Vec3

object SecretWaypoints {

    fun onSecret(event: SecretPickupEvent) {
        when (event) {
            is SecretPickupEvent.Interact -> clickSecret(Vec3(event.blockPos), 0)
            is SecretPickupEvent.Bat -> clickSecret(event.packet.pos, 5)
            is SecretPickupEvent.Item -> clickSecret(event.entity.positionVector, 3)
        }
    }

    private fun clickSecret(pos: Vec3, distance: Int) {
        val room = DungeonUtils.currentRoom ?: return
        val vec = Vec3(pos.xCoord, pos.yCoord, pos.zCoord).subtractVec(x = room.clayPos.x, z = room.clayPos.z).rotateToNorth(room.room.rotation)
        val waypoints = getWaypoints(room)
        waypoints.find { wp -> (if (distance == 0) wp.toVec3().equal(vec) else wp.toVec3().distanceTo(vec) <= distance) && wp.secret && !wp.clicked}?.let {
            it.clicked = true
            setWaypoints(room)
            devMessage("clicked ${it.toVec3()}")
            glList = -1
        }
    }

    fun resetSecrets() {
        val room = DungeonUtils.currentRoom
        for (waypointsList in DungeonWaypointConfigCLAY.waypoints.filter { waypoints -> waypoints.value.any { it.clicked } }.values) {
            waypointsList.filter { it.clicked }.forEach { it.clicked = false }
        }

        if (room != null) setWaypoints(room)
        glList = -1
    }
}