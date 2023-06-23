package me.odinclient.features.dungeon

import me.odinclient.OdinClient.Companion.config
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.utils.skyblock.LocationUtils
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object GhostBlock {

    private val blacklist = setOf(
        Blocks.acacia_door,
        Blocks.anvil,
        Blocks.beacon,
        Blocks.bed,
        Blocks.birch_door,
        Blocks.brewing_stand,
        Blocks.brown_mushroom,
        Blocks.chest,
        Blocks.command_block,
        Blocks.crafting_table,
        Blocks.dark_oak_door,
        Blocks.daylight_detector,
        Blocks.daylight_detector_inverted,
        Blocks.dispenser,
        Blocks.dropper,
        Blocks.enchanting_table,
        Blocks.ender_chest,
        Blocks.furnace,
        Blocks.hopper,
        Blocks.jungle_door,
        Blocks.lever,
        Blocks.noteblock,
        Blocks.oak_door,
        Blocks.powered_comparator,
        Blocks.powered_repeater,
        Blocks.red_mushroom,
        Blocks.skull,
        Blocks.standing_sign,
        Blocks.stone_button,
        Blocks.trapdoor,
        Blocks.trapped_chest,
        Blocks.unpowered_comparator,
        Blocks.unpowered_repeater,
        Blocks.wall_sign,
        Blocks.wooden_button
    )

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || !LocationUtils.inSkyblock || !config.ghostBlockBind.isActive || mc.thePlayer == null || mc.currentScreen != null) return
        toAir(mc.objectMouseOver.blockPos)
    }

    private fun toAir(blockPos: BlockPos?): Boolean {
        if (blockPos != null) {
            val block = mc.theWorld.getBlockState(mc.objectMouseOver.blockPos).block
            if (!blacklist.contains(block)) {
                mc.theWorld.setBlockToAir(mc.objectMouseOver.blockPos)
                return true
            }
        }
        return false
    }

    @SubscribeEvent
    fun ticks(event: TickEvent.ClientTickEvent) {
        if (!DungeonUtils.isFloor(7) || !DungeonUtils.inBoss || !config.preGhostBlock) return
        for (b in airBlocks) {
            mc.theWorld?.setBlockToAir(b)
        }
    }

    private val airBlocks = setOf(
        //phase 1
        BlockPos(88, 220, 61),
        BlockPos(88, 219, 61),
        BlockPos(88, 218, 61),
        BlockPos(88, 217, 61),
        BlockPos(88, 216, 61),
        BlockPos(88, 215, 61),
        BlockPos(88, 214, 61),
        BlockPos(88, 213, 61),
        BlockPos(88, 212, 61),
        BlockPos(88, 211, 61),
        BlockPos(88, 210, 61),

        //phase 2
        BlockPos(88, 167, 41),
        BlockPos(89, 167, 41),
        BlockPos(90, 167, 41),
        BlockPos(91, 167, 41),
        BlockPos(92, 167, 41),
        BlockPos(93, 167, 41),
        BlockPos(94, 167, 41),
        BlockPos(95, 167, 41),
        BlockPos(88, 166, 41),
        BlockPos(89, 166, 41),
        BlockPos(90, 166, 41),
        BlockPos(91, 166, 41),
        BlockPos(92, 166, 41),
        BlockPos(93, 166, 41),
        BlockPos(94, 166, 41),
        BlockPos(95, 166, 41),
        BlockPos(88, 165, 41),
        BlockPos(89, 165, 41),
        BlockPos(90, 165, 41),
        BlockPos(91, 165, 41),
        BlockPos(92, 165, 41),
        BlockPos(93, 165, 41),
        BlockPos(94, 165, 41),
        BlockPos(95, 165, 41),
        BlockPos(88, 167, 40),
        BlockPos(89, 167, 40),
        BlockPos(90, 167, 40),
        BlockPos(91, 167, 40),
        BlockPos(92, 167, 40),
        BlockPos(93, 167, 40),
        BlockPos(94, 167, 40),
        BlockPos(95, 167, 40),
        BlockPos(88, 166, 40),
        BlockPos(89, 166, 40),
        BlockPos(90, 166, 40),
        BlockPos(91, 166, 40),
        BlockPos(92, 166, 40),
        BlockPos(93, 166, 40),
        BlockPos(94, 166, 40),
        BlockPos(95, 166, 40),
        BlockPos(88, 165, 40),
        BlockPos(89, 165, 40),
        BlockPos(90, 165, 40),
        BlockPos(91, 165, 40),
        BlockPos(92, 165, 40),
        BlockPos(93, 165, 40),
        BlockPos(94, 165, 40),
        BlockPos(95, 165, 40),

        //phase 3
        BlockPos(51, 114, 52),
        BlockPos(51, 114, 53),
        BlockPos(51, 114, 54),
        BlockPos(51, 114, 55),
        BlockPos(51, 114, 56),
        BlockPos(51, 114, 57),
        BlockPos(51, 114, 58),
        BlockPos(51, 115, 52),
        BlockPos(51, 115, 53),
        BlockPos(51, 115, 54),
        BlockPos(51, 115, 55),
        BlockPos(51, 115, 56),
        BlockPos(51, 115, 57),
        BlockPos(51, 115, 58),

        //phase 4
        BlockPos(54, 64, 72),
        BlockPos(54, 64, 73),
        BlockPos(54, 63, 73),
        BlockPos(54, 64, 74),
        BlockPos(54, 63, 74)
    )
}