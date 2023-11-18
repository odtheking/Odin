package me.odinmain.features.impl.dungeon

import me.odinmain.config.DungeonWaypointConfig
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.equal
import me.odinmain.utils.rotateToNorth
import me.odinmain.utils.subtractVec
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.world.RenderUtils
import me.odinmain.utils.skyblock.ChatUtils.devMessage
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.RoomType
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object DungeonWaypoints : Module(
    name = "Dungeon Waypoints",
    description = "Shows waypoints for dungeons",
    category = Category.DUNGEON,
    tag = TagType.NEW
) {
    private val debugWaypoint: Boolean by BooleanSetting("Debug Waypoint", false)
    private var allowEdits: Boolean by BooleanSetting("Allow Edits", false)

    data class DungeonWaypoint(val x: Double, val y: Double, val z: Double, val color: Color)

    override fun onKeybind() {
        allowEdits = !allowEdits
    }

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        DungeonUtils.currentRoom?.waypoints?.forEach {
            RenderUtils.drawCustomBox(it.x, it.y, it.z, 1.0, it.color, 3f, true)
        }

        if (debugWaypoint) {
            val room = DungeonUtils.currentRoom?.room ?: return
            RenderUtils.drawCustomBox(room.x, 70, room.z - 4, 1.0, Color.GREEN, 3f, true)
        }
    }

    @SubscribeEvent
    fun onInteract(event: PlayerInteractEvent) {
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK || event.world != mc.theWorld || !allowEdits) return
        val room = DungeonUtils.currentRoom?.room ?: return
        val vec = Vec3(event.pos)
            .subtractVec(x = room.x, z = room.z)
            .rotateToNorth(room.rotation)

        val waypoints =
            if (room.data.type == RoomType.PUZZLE)
                DungeonWaypointConfig.waypoints.getOrPut(room.data.name) { mutableListOf() }
            else
                DungeonWaypointConfig.waypoints.getOrPut(room.core.toString()) { mutableListOf() }

        if (!waypoints.any { it.toVec3().equal(vec) }) {
            waypoints.add(DungeonWaypoint(vec.xCoord, vec.yCoord, vec.zCoord, Color.GREEN))
            devMessage("Added waypoint at $vec")
        } else {
            waypoints.removeIf { it.toVec3().equal(vec) }
            devMessage("Removed waypoint at $vec")
        }
        DungeonWaypointConfig.saveConfig()
        DungeonUtils.setWaypoints()
    }

    fun DungeonWaypoint.toVec3() = Vec3(x, y, z)
}