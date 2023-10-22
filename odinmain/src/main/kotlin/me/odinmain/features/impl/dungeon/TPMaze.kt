package me.odinmain.features.impl.dungeon

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.world.RenderUtils
import me.odinmain.utils.skyblock.ChatUtils.devMessage
import me.odinmain.utils.skyblock.ChatUtils.modMessage
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.init.Blocks
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object TPMaze : Module(
    name = "TP Maze Solver (WIP)",
    description = "Automatically solves the TP maze.",
    category = Category.DUNGEON,
    tag = TagType.NEW
) {
    private var portals = setOf<BlockPos>()

    init {
        execute(1000) {
            if (!enabled || portals.size >= 30/* || DungeonUtils.currenRoomName != "Teleport Maze"*/) return@execute
            val pos = mc.thePlayer?.position ?: return@execute
            portals = portals.plus(BlockPos.getAllInBox(BlockPos(pos.x + 22, 70, pos.z + 22), BlockPos(pos.x - 22, 69, pos.z - 22)).filter {
                mc.theWorld.getBlockState(it).block == Blocks.end_portal_frame
            })
        }
    }

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        //if (/*|| DungeonUtils.currenRoomName != "Teleport Maze"*/) return
        portals.forEach {
            RenderUtils.drawCustomESPBox(it.x, it.y, it.z, 1, Color.GOLD, phase = false)
        }
    }
}