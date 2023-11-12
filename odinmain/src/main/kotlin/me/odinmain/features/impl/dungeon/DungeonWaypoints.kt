package me.odinmain.features.impl.dungeon

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.utils.VecUtils.addVec
import me.odinmain.utils.VecUtils.equal
import me.odinmain.utils.VecUtils.rotateAroundNorth
import me.odinmain.utils.VecUtils.rotateToNorth
import me.odinmain.utils.VecUtils.subtractVec
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.world.RenderUtils
import me.odinmain.utils.skyblock.ChatUtils.devMessage
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.util.EnumFacing
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
    val waypoints = mutableSetOf<Vec3>(
    )

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        val rotation = DungeonUtils.currentRoom?.room?.rotation ?: return
        waypoints.forEach {
            val rotatedVec = it.rotateAroundNorth(rotation).addVec(x = DungeonUtils.currentRoom?.room?.x ?: 0, z = DungeonUtils.currentRoom?.room?.z ?: 0)
            RenderUtils.drawCustomBox(rotatedVec.xCoord, rotatedVec.yCoord, rotatedVec.zCoord, 1.0, Color.GREEN, 3f, true)
        }
    }

    @SubscribeEvent
    fun onInteract(event: PlayerInteractEvent) {
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK || event.world != mc.theWorld) return
        val vec = Vec3(event.pos)
            .subtractVec(x = DungeonUtils.currentRoom?.room?.x ?: 0, z = DungeonUtils.currentRoom?.room?.z ?: 0)
            .rotateToNorth(DungeonUtils.currentRoom?.room?.rotation ?: EnumFacing.NORTH)
        if (!waypoints.any { it.equal(vec) }) {
            waypoints.add(vec)
            devMessage("Added waypoint at $vec")
        } else {
            waypoints.removeIf { it.equal(vec) }
            devMessage("Removed waypoint at $vec")
        }
    }
}