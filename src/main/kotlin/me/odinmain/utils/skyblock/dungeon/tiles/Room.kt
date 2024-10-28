package me.odinmain.utils.skyblock.dungeon.tiles

import me.odinmain.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.DungeonWaypoint
import me.odinmain.utils.Vec2
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3

class Room(override val x: Int, override val z: Int, var data: RoomData, var core: Int = 0) : Tile {
    var vec2 = Vec2(x, z)
    var vec3 = Vec3(x.toDouble(), 70.0, z.toDouble())
    var rotation = Rotations.NONE
    override var state: RoomState = RoomState.UNDISCOVERED
}

data class FullRoom(val room: Room, var clayPos: BlockPos, val components: ArrayList<ExtraRoom>, var waypoints: ArrayList<DungeonWaypoint>)
data class ExtraRoom(val x: Int, val z: Int, val core: Int) {
    val vec2 = Vec2(x, z)
}