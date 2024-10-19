package me.odinmain.features.impl.dungeon

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.font.OdinFont
import me.odinmain.ui.clickgui.animations.impl.EaseInOut
import me.odinmain.utils.render.*
import me.odinmain.utils.runIn
import me.odinmain.utils.skyblock.getBlockAt
import me.odinmain.utils.skyblock.isAir
import me.odinmain.utils.toVec3
import net.minecraft.block.BlockStairs
import net.minecraft.block.properties.IProperty
import net.minecraft.block.state.IBlockState
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import kotlin.math.abs

object CanClip : Module(
    name = "Can Clip",
    description = "Tells you if you are currently able to clip through a stair under you.",
    category = Category.DUNGEON
) {
    private val line by BooleanSetting("Line", true, description = "Draws a line where you can clip.")
    private val hud by HudSetting("Display", 10f, 10f, 1f, true) {
        if (it) {
            text("Can Clip", 1f, 9f, Color.WHITE, 12f, OdinFont.REGULAR)
            getTextWidth("Can Clip", 12f) to 12f
        } else {
            text("Can Clip", 1f, 9f, Color(0, 255, 0, animation.get(0f, 1f, !canClip)), 12f, OdinFont.REGULAR)
            getTextWidth("Can Clip", 12f) to 12f
        }
    }

    private val animation = EaseInOut(300)
    private var canClip = false

    private val ranges = listOf(0.235..0.265, 0.735..0.765)

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        val player = mc.thePlayer ?: return

        if (player.isSneaking) {
            if (canClip) {
                animation.start()
                canClip = false
            }
            return
        }

        val prev = canClip
        canClip = ranges.any { abs(player.posX % 1) in it || abs(player.posZ % 1) in it }
        if (prev != canClip) animation.start()
    }

    private val Blocks = mutableMapOf<Vec3, String>()

    init {
        onPacket(C07PacketPlayerDigging::class.java) {
            if (it.status != C07PacketPlayerDigging.Action.START_DESTROY_BLOCK || !line || getBlockAt(it.position) !is BlockStairs) return@onPacket
            val state = mc.theWorld?.getBlockState(it.position) ?: return@onPacket

            runIn(1) {
                if (isAir(it.position)) Blocks[it.position.toVec3()] = getDirection(state)
            }
        }

        onWorldLoad {
            Blocks.clear()
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        Blocks.forEach { (pos, dir) ->
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

            if (line) Renderer.draw3DLine(listOf(pos1, pos2), color = Color.RED, depth = true)
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