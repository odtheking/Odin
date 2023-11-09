package me.odinmain.features.impl.dungeon

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.ReceivePacketEvent
import me.odinmain.features.impl.dungeon.PuzzleSolvers.mazeColorMultiple
import me.odinmain.features.impl.dungeon.PuzzleSolvers.mazeColorOne
import me.odinmain.features.impl.dungeon.PuzzleSolvers.tpMaze
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.VecUtils
import me.odinmain.utils.VecUtils.toAABB
import me.odinmain.utils.clock.Executor
import me.odinmain.utils.clock.Executor.Companion.register
import me.odinmain.utils.render.world.RenderUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
object TPMaze {
    var portals = setOf<BlockPos>()
    var correctPortals = listOf<BlockPos>()

    init {
        Executor(200) {
            if (!tpMaze || portals.size >= 30 || DungeonUtils.currentRoomName != "Teleport Maze") return@Executor
            val pos = mc.thePlayer?.position ?: return@Executor
            portals = portals.plus(BlockPos.getAllInBox(BlockPos(pos.x + 22, 70, pos.z + 22), BlockPos(pos.x - 22, 69, pos.z - 22)).filter {
                mc.theWorld.getBlockState(it).block == Blocks.end_portal_frame
            })
        }.register()

    }
    @SubscribeEvent
    fun onPacket(event: ReceivePacketEvent) {
        if (event.packet !is S08PacketPlayerPosLook) return
        if (DungeonUtils.currentRoomName != "Teleport Maze") return
        getCorrectPortals(Vec3(event.packet.x, event.packet.y, event.packet.z), event.packet.yaw, event.packet.pitch)
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        portals = setOf()
        correctPortals = listOf()
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
        if (DungeonUtils.currentRoomName != "Teleport Maze" || !tpMaze) return
        val color = if (correctPortals.size == 1) mazeColorOne else mazeColorMultiple
        correctPortals.forEach {
            RenderUtils.drawFilledBox(it.toAABB(), color.withAlpha(.5f), phase = true)
        }
    }
}