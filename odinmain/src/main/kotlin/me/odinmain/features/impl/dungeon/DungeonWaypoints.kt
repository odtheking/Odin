package me.odinmain.features.impl.dungeon

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.VecUtils.addVec
import me.odinmain.utils.VecUtils.rotateAroundNorth
import me.odinmain.utils.skyblock.ChatUtils.modMessage
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object DungeonWaypoints : Module(
    name = "Dungeon Waypoints",
    description = "Shows waypoints for dungeons",
    category = Category.DUNGEON,
    tag = TagType.NEW
) {
    private val direction: Int by NumberSetting("Direction", 0, 0, 3, 1)
    private val waypoint = Vec3(-15.0, 80.0, -15.0)

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        val rotation = when (direction) {
            0 -> EnumFacing.NORTH
            1 -> EnumFacing.EAST
            2 -> EnumFacing.SOUTH
            3 -> EnumFacing.WEST
            else -> EnumFacing.NORTH
        }
        modMessage(rotation)
        val rotatedVec = waypoint.rotateAroundNorth(rotation).addVec(-185, 0, -185)
        //modMessage(rotatedVec)
        //RenderUtils.drawCustomESPBox(rotatedVec.xCoord, rotatedVec.yCoord, rotatedVec.zCoord, 1.0, Color.GREEN, 3f, true)
    }
}