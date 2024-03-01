package me.odinclient.features.impl.floor7

import me.odinmain.events.impl.*
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S22PacketMultiBlockChange
import net.minecraft.network.play.server.S23PacketBlockChange
import net.minecraft.util.BlockPos
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.abs

object DioriteFucker : Module(
    "Fuck Diorite",
    description = "Replaces the pillars in the F7 & M7 boss-fight with glass.",
    category = Category.FLOOR7,
) {
    private val stainedGlass: Boolean by BooleanSetting("Stained glass", default = false, description = "Swaps the diorite with stained glass" )
    private val color: Int by NumberSetting("Color", 0, 0.0, 15.0, 1.0).withDependency { stainedGlass }

    @SubscribeEvent
    fun onBlockChange(event: BlockChangeEvent) {
        if (!isWithinPillar(event.pos) || event.update.block.equalsOneOf(Blocks.glass, Blocks.stained_glass, Blocks.air) || event.world != mc.theWorld) return
        event.isCanceled = true
        setGlass(event.pos)
    }

    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {
        if (event.message == "[BOSS] Storm: I'd be happy to show you what that's like!")
            replaceDiorite()
    }

    fun replaceDiorite() {
        pillars.forEach { (x, y, z) ->
            val coordinates = (-3..3).flatMap { dx ->
                (0..37).flatMap { dy ->
                    (-3..3).map { dz -> Triple(x + dx, y + dy, z + dz) }
                }
            }
            coordinates.filter { (cx, cy, cz) -> isDiorite(cx, cy, cz) }
                .forEach { (cx, cy, cz) -> setGlass(BlockPos(cx, cy, cz)) }
        }
    }

    private val pillars = listOf(
        listOf(46, 169, 41),
        listOf(46, 169, 65),
        listOf(100, 169, 65),
        listOf(100, 179, 41)
    )

    private fun isWithinPillar(pos: BlockPos): Boolean {
        for (pillar in pillars) {
            val (x0, y0, z0) = pillar
            if (manhattanDistance(x0, z0, pos.x, pos.z) > 4) continue
            if (pos.y > 205 || pos.y < y0) continue
            return true
        }
        return false
    }

    private fun manhattanDistance(x0: Int, y0: Int, x1: Int, y1: Int): Int {
        return abs(x1 - x0) + abs(y1 - y0)
    }

    private fun setGlass(pos: BlockPos) {
        if (stainedGlass) mc.theWorld.setBlockState(pos, Blocks.stained_glass.getStateFromMeta(color), 3)
        else mc.theWorld.setBlockState(pos, Blocks.glass.defaultState, 3)
    }

    private fun isDiorite(x: Int, y: Int, z: Int): Boolean {
        val block = mc.theWorld.chunkProvider.provideChunk(x shr 4, z shr 4).getBlock(x, y, z)
        return block == Blocks.stone
    }
}