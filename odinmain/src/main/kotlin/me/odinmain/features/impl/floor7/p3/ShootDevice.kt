package me.odinmain.features.impl.floor7.p3

import me.odinmain.events.impl.BlockChangeEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.ActionSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.M7Phases
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ShootDevice : Module(
    name = "Shoot Device",
    description = " 4th device in phase 3 of floor 7.",
    category = Category.FLOOR7
) {
    private val markedPositionColor: Color by ColorSetting("Marked Position", Color.RED, description = "Color of the marked position.")
    private val reset: () -> Unit by ActionSetting("Reset") {
        markedPositions.clear()
    }

    override fun onKeybind() {
        reset()
        super.onKeybind()
    }

    init {
        onWorldLoad {
            reset()
        }
    }

    @SubscribeEvent
    fun onBlockChange(event: BlockChangeEvent) {
        if (!DungeonUtils.inDungeons || DungeonUtils.getPhase() != M7Phases.P3 || !positions.contains(event.pos)) return
        if (event.old.block == Blocks.emerald_block && event.update.block == Blocks.stained_hardened_clay) markedPositions.add(event.pos)
    }

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        if (!DungeonUtils.inDungeons || DungeonUtils.getPhase() != M7Phases.P3 || markedPositions.isEmpty()) return
        markedPositions.forEach {
            Renderer.drawBlock(it, markedPositionColor)
        }
    }

    private val positions = listOf(
        BlockPos(68, 130, 50), BlockPos(66, 130, 50), BlockPos(64, 130, 50),
        BlockPos(68, 128, 50), BlockPos(66, 128, 50), BlockPos(64, 128, 50),
        BlockPos(68, 126, 50), BlockPos(66, 126, 50), BlockPos(64, 126, 50)
    )

    private val markedPositions = mutableSetOf<BlockPos>()
}