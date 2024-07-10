package me.odin.features.impl.floor7.p3

import me.odinmain.events.impl.BlockChangeEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.M7Phases
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3i
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard

object ArrowsDevice : Module(
    name = "Arrows Device",
    description = "Marks already shot positions in the Arrows Device puzzle.",
    category = Category.FLOOR7,
    key = null
) {
    private val markedPositionColor: Color by ColorSetting("Marked Position", Color.RED, description = "Color of the marked position.")
    private val targetPositionColor: Color by ColorSetting("Target Position", Color.GREEN, description = "Color of the target position.")
    private val resetKey: Keybinding by KeybindSetting("Reset", Keyboard.KEY_NONE, description = "Resets the solver.").onPress { reset() }
    private val depthCheck: Boolean by BooleanSetting("Depth check", true, description = "Marked positions show through walls.")
    private val reset: () -> Unit by ActionSetting("Reset") {
        markedPositions.clear()
    }

    init {
        onWorldLoad {
            reset()
        }
    }

    @SubscribeEvent
    fun onBlockChange(event: BlockChangeEvent) {
        if (!DungeonUtils.inDungeons || DungeonUtils.getPhase() != M7Phases.P3 || !positions.contains(event.pos)) return
        if (event.old.block == Blocks.emerald_block && event.update.block == Blocks.stained_hardened_clay) {
            markedPositions.add(event.pos)
            // This condition should always be true but im never sure with Hypixel
            if (targetPosition == event.pos) targetPosition = null
        }
        if (event.old.block == Blocks.stained_hardened_clay && event.update.block == Blocks.emerald_block) {
            // Can happen with resets
            markedPositions.remove(event.pos)
            targetPosition = event.pos
        }
    }

    @SubscribeEvent
    fun onRenderWorldLast(event: RenderWorldLastEvent) {
        if (PlayerUtils.posZ > 55 || (PlayerUtils.posX > 95 || PlayerUtils.posX < 15)) reset()
        if (!DungeonUtils.inDungeons || DungeonUtils.getPhase() != M7Phases.P3) return
        markedPositions.forEach {
            Renderer.drawBlock(it, markedPositionColor, depth = depthCheck)
        }
        targetPosition?.let {
            Renderer.drawBlock(it, targetPositionColor, depth = depthCheck)
        }
    }

    private val positions = listOf(
        BlockPos(68, 130, 50), BlockPos(66, 130, 50), BlockPos(64, 130, 50),
        BlockPos(68, 128, 50), BlockPos(66, 128, 50), BlockPos(64, 128, 50),
        BlockPos(68, 126, 50), BlockPos(66, 126, 50), BlockPos(64, 126, 50)
    )

    private val markedPositions = mutableSetOf<BlockPos>()
    private var targetPosition: BlockPos? = null
}