package me.odinclient.features.impl.floor7

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.profile
import me.odinmain.utils.skyblock.Island
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

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

    init {
        onMessage("[BOSS] Storm: I'd be happy to show you what that's like!", false) {
            replaceDiorite()
        }
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        profile("Diorite Fucker") {
            if (DungeonUtils.getPhase() == Island.M7P2 && event.phase == TickEvent.Phase.END && mc.theWorld != null) replaceDiorite()
        }
    }

    private fun replaceDiorite() {
        coordinates.forEach {
            if (isDiorite(it)) setGlass(it)
        }
    }

    private fun setGlass(pos: BlockPos) =
        mc.theWorld.setBlockState(pos, if (stainedGlass) Blocks.stained_glass.getStateFromMeta(color) else Blocks.glass.defaultState, 3)


    private fun isDiorite(pos: BlockPos): Boolean =
        mc.theWorld?.chunkProvider?.provideChunk(pos.x shr 4, pos.z shr 4)?.getBlock(pos) == Blocks.stone
}