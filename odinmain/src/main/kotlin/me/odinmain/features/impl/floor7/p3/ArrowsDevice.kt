package me.odinmain.features.impl.floor7.p3

import com.github.stivais.ui.color.Color
import me.odinmain.events.impl.BlockChangeEvent
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.M7Phases
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard

object ArrowsDevice : Module(
    name = "ArrowsDevice",
    description = "Marks already shot positions in the Arrows Device puzzle.",
    key = null
) {
    private val markedPositionColor by ColorSetting("Marked Position", Color.RED, description = "Color of the marked position.")
    private val resetKey by KeybindSetting("Reset", Keyboard.KEY_NONE, description = "Resets the solver.").onPress { markedPositions.clear() }
    private val reset by ActionSetting("Reset") { markedPositions.clear() }

    init {
        onWorldLoad {
            markedPositions.clear()
        }
    }

    @SubscribeEvent
    fun onBlockChange(event: BlockChangeEvent) {
        if (!DungeonUtils.inDungeons || DungeonUtils.getPhase() != M7Phases.P3 || !positions.contains(event.pos)) return
        if (event.old.block == Blocks.emerald_block && event.update.block == Blocks.stained_hardened_clay) markedPositions.add(event.pos)
    }

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        if (PlayerUtils.posZ > 55 || (PlayerUtils.posX > 95 || PlayerUtils.posX < 15)) markedPositions.clear()
        if (!DungeonUtils.inDungeons || DungeonUtils.getPhase() != M7Phases.P3 || markedPositions.isEmpty()) return
        markedPositions.forEach {
            Renderer.drawBlock(it, markedPositionColor, depth = true)
        }
    }

    private val positions = listOf(
        BlockPos(68, 130, 50), BlockPos(66, 130, 50), BlockPos(64, 130, 50),
        BlockPos(68, 128, 50), BlockPos(66, 128, 50), BlockPos(64, 128, 50),
        BlockPos(68, 126, 50), BlockPos(66, 126, 50), BlockPos(64, 126, 50)
    )

    private val markedPositions = mutableSetOf<BlockPos>()
}