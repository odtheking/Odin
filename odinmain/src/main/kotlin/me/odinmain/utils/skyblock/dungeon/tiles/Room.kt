package me.odinmain.utils.skyblock.dungeon.tiles

import me.odinmain.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.DungeonWaypoint
import me.odinmain.utils.Vec2
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3

data class Room(
    var rotation: Rotations = Rotations.NONE,
    var data: RoomData,
    var clayPos: BlockPos = BlockPos(0, 0, 0),
    val roomComponents: MutableSet<RoomComponent>,
    var waypoints: MutableSet<DungeonWaypoint> = mutableSetOf()
)

data class RoomComponent(val x: Int, val z: Int, val core: Int = 0) {
    val vec2 = Vec2(x, z)
    val vec3 = Vec3(x.toDouble(), 70.0, z.toDouble())
}