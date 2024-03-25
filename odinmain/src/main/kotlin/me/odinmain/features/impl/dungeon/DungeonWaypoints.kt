package me.odinmain.features.impl.dungeon

import me.odinmain.config.DungeonWaypointConfig
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.render.DevPlayers
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.ActionSetting
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.equal
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.rotateToNorth
import me.odinmain.utils.skyblock.devMessage
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.tiles.RoomType
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.subtractVec
import me.odinmain.utils.toAABB
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
 * Custom Waypoints for Dungeons
 * @author Bonsai
 */
object DungeonWaypoints : Module(
    name = "Dungeon Waypoints",
    description = "Shows waypoints for dungeons.",
    category = Category.DUNGEON,
    tag = TagType.NEW
) {
    private var allowEdits: Boolean by BooleanSetting("Allow Edits", false)
    private val color: Color by ColorSetting("Color", default = Color.GREEN, description = "The color of the next waypoint you place.", allowAlpha = true)
    private val filled: Boolean by BooleanSetting("Filled", false, description = "If the next waypoint you place should be 'filled'.")
    private val throughWalls: Boolean by BooleanSetting("Through walls", false, description = "If the next waypoint you place should be visible through walls.")
    private val size: Double by NumberSetting<Double>("Size", 1.0, .125, 1.0, increment = 0.125, description = "The size of the next waypoint you place.")
    private val resetButton: () -> Unit by ActionSetting("Reset Current Room") {
        val room = DungeonUtils.currentRoom ?: return@ActionSetting modMessage("Room not found!!!")

        if (room.room.data.type != RoomType.NORMAL) {
            val waypoints = DungeonWaypointConfig.waypoints.getOrPut(room.room.data.name) { mutableListOf() }
            if (!waypoints.removeAll { true }) return@ActionSetting modMessage("Current room does not have any waypoints!")
        } else {
            var changedWaypoints = false
            room.positions.forEach {
                if (DungeonWaypointConfig.waypoints[it.core.toString()]?.removeAll { true } == true) changedWaypoints = true
            }
            if (!changedWaypoints) return@ActionSetting modMessage("Current room does not have any waypoints!")
        }
        DungeonWaypointConfig.saveConfig()
        DungeonUtils.setWaypoints()
        modMessage("Successfully reset current room!")
    }
    private val debugWaypoint: Boolean by BooleanSetting("Debug Waypoint", false).withDependency { DevPlayers.isDev }

    data class DungeonWaypoint(val x: Double, val y: Double, val z: Double, val color: Color, val filled: Boolean, val depth: Boolean, val size: Double, val title: String?)

    override fun onKeybind() {
        allowEdits = !allowEdits
        modMessage("Dungeon Waypoint editing ${if (allowEdits) "§aenabled" else "§cdisabled"}§r!")
    }

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        DungeonUtils.currentRoom?.waypoints?.forEach {
            Renderer.drawBox(it.toAABB(it.size), it.color, fillAlpha = if (it.filled) .8 else 0, depth = it.depth)
            Renderer.drawStringInWorld(it.title ?: "", Vec3(it.x+0.5,it.y+0.5,it.z+0.5))
        }

        if (debugWaypoint) {
            val room = DungeonUtils.currentRoom?.room ?: return
            Renderer.drawBox(Vec3(room.x.toDouble(), 70.0, room.z.toDouble()).toAABB(), Color.GREEN, fillAlpha = 0)
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
            if (room.data.type != RoomType.NORMAL)
                DungeonWaypointConfig.waypoints.getOrPut(room.data.name) { mutableListOf() }
            else
                DungeonWaypointConfig.waypoints.getOrPut(room.core.toString()) { mutableListOf() }

        if (!waypoints.any { it.toVec3().equal(vec) }) {
            waypoints.add(DungeonWaypoint(vec.xCoord, vec.yCoord, vec.zCoord, color.copy(), filled, !throughWalls, size, " "))
            devMessage("Added waypoint at $vec")
        } else {
            waypoints.removeIf { it.toVec3().equal(vec) }
            devMessage("Removed waypoint at $vec")
        }
        DungeonWaypointConfig.saveConfig()
        DungeonUtils.setWaypoints()
    }

    fun DungeonWaypoint.toVec3() = Vec3(x, y, z)
    fun DungeonWaypoint.toBlockPos() = BlockPos(x, y, z)

    fun DungeonWaypoint.toAABB(size: Double) = AxisAlignedBB(x + .5 - (size/2), y + .5 - (size/2), z + .5 - (size/2), x + .5 + (size/2), y + .5 + (size/2), z + .5 + (size/2)).expand(.01, .01, .01)
}