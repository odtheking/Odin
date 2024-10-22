package me.odinclient.features.impl.floor7

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.profile
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.M7Phases
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object FuckDiorite : Module(
    name = "Fuck Diorite",
    description = "Replaces the pillars in the storm fight with glass.",
    category = Category.FLOOR7,
) {
    private val stainedGlass by BooleanSetting("Stained glass", default = true, description = "Swaps the diorite with stained glass.")
    private val pillarBasedColor by BooleanSetting("Pillar Based", default = true, description = "Swaps the diorite in the pillar to a corresponding color.").withDependency { stainedGlass }
    private val colorIndex by NumberSetting("Color", 0, 0.0, 15.0, 1.0, description = "Color for the stained glass.").withDependency { stainedGlass }
    private val pillars = listOf(listOf(46, 169, 41), listOf(46, 169, 65), listOf(100, 169, 65), listOf(100, 179, 41)) // Green, Yellow, Purple, Red
    private val coordinates: List<List<BlockPos>> = pillars.map { (x, y, z) ->
        (x - 3..x + 3).flatMap { dx ->
            (y..y + 37).flatMap { dy ->
                (z - 3..z + 3).map { dz ->
                    BlockPos(dx, dy, dz)
                }
            }
        }
    }

    init {
        onMessage("[BOSS] Storm: Pathetic Maxor, just like expected.", false) {
            modMessage("§3Fucking Diorite!")
            replaceDiorite()
        }
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        profile("Diorite Fucker") {
            if (event.phase == TickEvent.Phase.END && mc.theWorld != null && DungeonUtils.getF7Phase() == M7Phases.P2) replaceDiorite()
        }
    }

    private fun replaceDiorite() {
        coordinates.forEach { pillar ->
            pillar.forEach { blockPosition ->
                if (isDiorite(blockPosition)) setGlass(blockPosition)
            }
        }
    }

    private val pillarColors = listOf(5, 4, 10, 14) // Green, Yellow, Purple, Red

    private fun setGlass(pos: BlockPos) {
        Blocks.stained_glass.getStateFromMeta(if (pillarBasedColor) pillarColors[coordinates.indexOfFirst { pos in it }] else colorIndex).let {
            mc.theWorld?.setBlockState(pos, if (stainedGlass) it else Blocks.glass.defaultState, 3)
        }
    }

    private fun isDiorite(pos: BlockPos): Boolean =
        mc.theWorld?.chunkProvider?.provideChunk(pos.x shr 4, pos.z shr 4)?.getBlock(pos) == Blocks.stone
}