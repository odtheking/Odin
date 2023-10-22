package me.odinmain.features.impl.dungeon

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.VecUtils
import me.odinmain.utils.VecUtils.toAABB
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.world.RenderUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object TPMaze : Module(
    name = "TP Maze Solver (WIP)",
    description = "Automatically solves the TP maze.",
    category = Category.DUNGEON,
    tag = TagType.NEW
) {
    var portals = setOf<BlockPos>()
    var correctPortals = listOf<BlockPos>()

    init {
        execute(200) {
            if (!enabled || portals.size >= 30/* || DungeonUtils.currenRoomName != "Teleport Maze"*/) return@execute
            val pos = mc.thePlayer?.position ?: return@execute
            portals = portals.plus(BlockPos.getAllInBox(BlockPos(pos.x + 22, 70, pos.z + 22), BlockPos(pos.x - 22, 69, pos.z - 22)).filter {
                mc.theWorld.getBlockState(it).block == Blocks.end_portal_frame
            })
        }

        onPacket(S08PacketPlayerPosLook::class.java) {
            if (DungeonUtils.currenRoomName != "Teleport Maze") return@onPacket
            getCorrectPortals(Vec3(it.x, it.y, it.z), it.yaw, it.pitch)
        }
    }

    fun getCorrectPortals(pos: Vec3, yaw: Float, pitch: Float) {
        if (correctPortals.isEmpty()) correctPortals = correctPortals.plus(portals)


        correctPortals = correctPortals.filter {
            VecUtils.isXZInterceptable(
                AxisAlignedBB(it.x.toDouble(), it.y.toDouble(), it.z.toDouble(), it.x + 1.0, it.y + 4.0, it.z + 1.0).expand(0.5, 0.0, 0.5),
                40f,
                pos,
                yaw,
                pitch
            )
        }
    }

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        //if (DungeonUtils.currenRoomName != "Teleport Maze") return
        correctPortals.forEach {
            RenderUtils.drawFilledBox(it.toAABB(), Color.GREEN.withAlpha(.5f), phase = true)
        }
    }
}