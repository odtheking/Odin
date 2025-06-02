package me.odinmain.features.impl.dungeon

import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.render.getMCTextWidth
import me.odinmain.utils.runIn
import me.odinmain.utils.skyblock.getBlockAt
import me.odinmain.utils.skyblock.getBlockStateAt
import me.odinmain.utils.toVec3
import me.odinmain.utils.ui.Colors
import net.minecraft.block.BlockStairs
import net.minecraft.block.properties.IProperty
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import kotlin.math.abs

object CanClip : Module(
    name = "Can Clip",
    desc = "Tells you if you are currently able to clip through a stair under you."
) {
    private val line by BooleanSetting("Line", true, desc = "Draws a line where you can clip.")
    private val hud by HudSetting("Display", 10f, 10f, 1f, true) {
        if (it) {
            RenderUtils.drawText("Can Clip", 1f, 1f, 1f, Colors.WHITE, shadow = true)
            getMCTextWidth("Can Clip").toFloat() to 12f
        } else {
            if (canClip) RenderUtils.drawText("Can Clip", 1f, 1f, 1f, Colors.WHITE, shadow = true)
            getMCTextWidth("Can Clip").toFloat() to 12f
        }
    }

    private var canClip = false

    private val ranges = listOf(0.235..0.265, 0.735..0.765)

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        val player = mc.thePlayer ?: return

        if (player.isSneaking) {
            if (canClip) canClip = false
            return
        }

        canClip = ranges.any { abs(player.posX % 1) in it || abs(player.posZ % 1) in it }
    }

    private val blocks = mutableMapOf<Vec3, String>()

    init {
        onPacket<C07PacketPlayerDigging> {
            if (it.status != C07PacketPlayerDigging.Action.START_DESTROY_BLOCK || !line || getBlockAt(it.position) !is BlockStairs) return@onPacket
            val state = getBlockStateAt(it.position)

            runIn(1) {
                // this NEEDS to get state again. the other state will still be the state for the stair, not the new block that were checking to see if is air.
                if (getBlockStateAt(it.position).block == Blocks.air) blocks[it.position.toVec3()] = getDirection(state)
            }
        }

        onWorldLoad {
            blocks.clear()
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        blocks.forEach { (pos, dir) ->
            val pos1: Vec3
            val pos2: Vec3
            when (dir) {
                "east" -> {
                    pos1 = Vec3(pos.xCoord + 0.24, pos.yCoord + 1.1, pos.zCoord)
                    pos2 = Vec3(pos.xCoord + 0.24, pos.yCoord + 1.1, pos.zCoord + 1)
                }
                "west" -> {
                    pos1 = Vec3(pos.xCoord + 0.76, pos.yCoord + 1.1, pos.zCoord)
                    pos2 = Vec3(pos.xCoord + 0.76, pos.yCoord + 1.1, pos.zCoord + 1)
                }
                "south" -> {
                    pos1 = Vec3(pos.xCoord, pos.yCoord + 1.1, pos.zCoord + 0.24)
                    pos2 = Vec3(pos.xCoord + 1, pos.yCoord + 1.1, pos.zCoord + 0.24)
                }
                "north" -> {
                    pos1 = Vec3(pos.xCoord, pos.yCoord + 1.1, pos.zCoord + 0.76)
                    pos2 = Vec3(pos.xCoord + 1, pos.yCoord + 1.1, pos.zCoord + 0.76)
                }
                else -> return
            }

            if (line) Renderer.draw3DLine(setOf(pos1, pos2), color = Colors.MINECRAFT_RED, depth = true)
        }
    }

    private fun getDirection(block: IBlockState): String {
        var dir = "block is not stairs"
        var half = "block is not stairs"
        if (block.block is BlockStairs) {
            block.properties.forEach { (key: IProperty<*>, value: Comparable<*>) ->
                if (key.name == "half") half = value.toString()
                if (key.name == "facing") dir = value.toString()
            }
        }
        return if (half == "bottom") dir else "Top stair"
    }
}