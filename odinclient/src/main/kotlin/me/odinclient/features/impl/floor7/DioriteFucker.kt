package me.odinclient.features.impl.floor7

import me.odinmain.events.impl.*
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.profile
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S22PacketMultiBlockChange
import net.minecraft.network.play.server.S23PacketBlockChange
import net.minecraft.util.BlockPos
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import kotlin.math.abs

object DioriteFucker : Module(
    "Fuck Diorite",
    description = "Replaces the pillars in the F7 & M7 boss-fight with glass.",
    category = Category.FLOOR7,
) {
    private val stainedGlass: Boolean by BooleanSetting("Stained glass", default = false, description = "Swaps the diorite with stained glass" )
    private val color: Int by NumberSetting("Color", 0, 0.0, 15.0, 1.0).withDependency { stainedGlass }
    private val pillars = listOf(listOf(46, 169, 41), listOf(46, 169, 65), listOf(100, 169, 65), listOf(100, 179, 41))
    private val coordinates: MutableList<BlockPos> = mutableListOf<BlockPos>().apply {
        pillars.forEach { (x, y, z) ->
            (-3..3).forEach { dx ->
                (0..37).forEach { dy ->
                    (-3..3).forEach { dz ->
                        add(BlockPos(x + dx, y + dy, z + dz))
                    }
                }
            }
        }
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        profile("Diorite Fucker") {
            if (DungeonUtils.getPhase() == 2 && event.phase == TickEvent.Phase.END && mc.theWorld != null) replaceDiorite()
        }
    }

    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {
        if (event.message == "[BOSS] Storm: I'd be happy to show you what that's like!")
            replaceDiorite()
    }

    fun replaceDiorite() {
        coordinates.forEach {
            if (isDiorite(it))
                setGlass(it)
        }
    }

    private fun isWithinPillar(pos: BlockPos): Boolean {
        for (pillar in pillars) {
            val (x0, y0, z0) = pillar
            if (manhattanDistance(x0, z0, pos.x, pos.z) > 4 || pos.y > 205 || pos.y < y0) continue
            return true
        }
        return false
    }

    private fun manhattanDistance(x0: Int, y0: Int, x1: Int, y1: Int): Int =
        abs(x1 - x0) + abs(y1 - y0)

    private fun setGlass(pos: BlockPos) {
        if (stainedGlass) mc.theWorld.setBlockState(pos, Blocks.stained_glass.getStateFromMeta(color), 3)
        else mc.theWorld.setBlockState(pos, Blocks.glass.defaultState, 3)
    }

    private fun isDiorite(pos: BlockPos): Boolean =
        mc.theWorld?.chunkProvider?.provideChunk(pos.x shr 4, pos.z shr 4)?.getBlock(pos) == Blocks.stone
}