package me.odinclient.features.impl.floor7

import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.ActionSetting
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.profile
import me.odinmain.utils.skyblock.Island
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.isFloor
import me.odinmain.utils.skyblock.dungeon.M7Phases
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase

object FuckDiorite : Module(
    name = "Fuck Diorite",
    description = "Replaces the pillars in the storm fight with glass."
) {
    private val GLASS_STATE = Blocks.glass.defaultState
    private val STAINED_GLASS = Blocks.stained_glass
    private val GLASS = Blocks.glass
    private val STONE = Blocks.stone
    private val glassStates = Array(16) { STAINED_GLASS.getStateFromMeta(it) }

    private val pillarBasedColor by BooleanSetting("Pillar Based", true, description = "Swaps the diorite in the pillar to a corresponding color.").withDependency { !schitzo }
    private val colorIndex by SelectorSetting("Color", "None", arrayListOf("NONE", "WHITE", "ORANGE", "MAGENTA", "LIGHT_BLUE", "YELLOW", "LIME", "PINK",
            "GRAY", "LIGHT_GRAY", "CYAN", "PURPLE", "BLUE", "BROWN", "GREEN", "RED", "BLACK"), description = "Color for the stained glass.").withDependency { !pillarBasedColor && !schitzo }
    private val schitzo by BooleanSetting("Schitzo mode", false, description = "Schtizoing.")
    private val action by ActionSetting("Force Glass", description = "Replaces all pillars with glass.") {
        if ((DungeonUtils.inBoss && isFloor(7)) || LocationUtils.currentArea.isArea(Island.SinglePlayer)) replaceDiorite(true)
        else modMessage("§cYou must be in F7/M7 boss room to use this feature.")
    }

    private val pillars = arrayOf(BlockPos(46, 169, 41), BlockPos(46, 169, 65), BlockPos(100, 169, 65), BlockPos(100, 169, 41))
    private val pillarColors = intArrayOf(5, 4, 10, 14)

    private val coordinates: Array<Set<BlockPos>> = Array(4) { pillarIndex ->
        val pillar = pillars[pillarIndex]
        buildSet {
            for (dx in (pillar.x - 3)..(pillar.x + 3))
                for (dy in pillar.y..(pillar.y + 37))
                    for (dz in (pillar.z - 3)..(pillar.z + 3))
                        add(BlockPos(dx, dy, dz))
        }
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase == Phase.END && DungeonUtils.getF7Phase() == M7Phases.P2) profile("Diorite Fucker") { replaceDiorite() }
    }

    private fun replaceDiorite(force: Boolean = false) {
        val chunkProvider = mc.theWorld?.chunkProvider ?: return
        val chunks = mutableMapOf<Long, Chunk>()

        for (coordinate in coordinates) {
            for (pos in coordinate) {
                val chunkX = pos.x shr 4
                val chunkZ = pos.z shr 4

                val chunk = chunks.getOrPut((chunkX.toLong() shl 32) or chunkZ.toLong()) { chunkProvider.provideChunk(chunkX, chunkZ) }
                if (chunk.getBlock(pos) == STONE || (force && chunk.getBlock(pos).equalsOneOf(STONE, GLASS, STAINED_GLASS))) setGlass(pos, coordinate)
            }
        }
    }

    private fun setGlass(pos: BlockPos, coordinate: Set<BlockPos>) {
        mc.theWorld?.setBlockState(pos, when { // cant use chunk.setBlockState due to it not updating the block
            schitzo -> glassStates.random()
            pillarBasedColor -> glassStates[pillarColors[coordinates.indexOfFirst { coordinate === it }]]
            colorIndex != 0 -> glassStates[colorIndex - 1]
            else -> GLASS_STATE
        }, 3)
    }
}