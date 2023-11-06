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
    name = "TP Maze Solver",
    description = "Shows you the teleport pad which takes you to the chest in tp maze.",
    category = Category.DUNGEON,
    tag = TagType.NEW
) {
    var portals = setOf<BlockPos>()
    var correctPortals = listOf<BlockPos>()

    init {
        execute(200) {
            if (!enabled || portals.size >= 30 || DungeonUtils.currentRoomName != "Teleport Maze") return@execute
            val pos = mc.thePlayer?.position ?: return@execute
            portals = portals.plus(BlockPos.getAllInBox(BlockPos(pos.x + 22, 70, pos.z + 22), BlockPos(pos.x - 22, 69, pos.z - 22)).filter {
                mc.theWorld.getBlockState(it).block == Blocks.end_portal_frame
            })
        }

        onPacket(S08PacketPlayerPosLook::class.java) {
            if (DungeonUtils.currentRoomName != "Teleport Maze") return@onPacket
            getCorrectPortals(Vec3(it.x, it.y, it.z), it.yaw, it.pitch)
        }

        onWorldLoad {
            portals = setOf()
            correctPortals = listOf()
        }
    }

    fun getCorrectPortals(pos: Vec3, yaw: Float, pitch: Float) {
        if (correctPortals.isEmpty()) correctPortals = correctPortals.plus(portals)


        correctPortals = correctPortals.filter {
            VecUtils.isXZInterceptable(
                AxisAlignedBB(it.x.toDouble(), it.y.toDouble(), it.z.toDouble(), it.x + 1.0, it.y + 4.0, it.z + 1.0).expand(0.75, 0.0, 0.75),
                80f,
                pos,
                yaw,
                pitch
            ) && !it.toAABB().expand(.5, .0, .5).isVecInside(mc.thePlayer.positionVector)
        }
    }

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        if (DungeonUtils.currentRoomName != "Teleport Maze") return
        val color = if (correctPortals.size == 1) Color.GREEN else Color.ORANGE
        correctPortals.forEach {
            RenderUtils.drawFilledBox(it.toAABB(), color.withAlpha(.5f), phase = true)
        }
    }
}