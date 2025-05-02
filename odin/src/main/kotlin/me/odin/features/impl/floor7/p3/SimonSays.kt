package me.odin.features.impl.floor7.p3

import me.odinmain.events.impl.BlockChangeEvent
import me.odinmain.events.impl.PostEntityMetadata
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.utils.component1
import me.odinmain.utils.component2
import me.odinmain.utils.component3
import me.odinmain.utils.floorVec
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.M7Phases
import me.odinmain.utils.ui.Colors
import me.odinmain.utils.ui.clickgui.util.ColorUtil.withAlpha
import net.minecraft.block.BlockButtonStone
import net.minecraft.entity.item.EntityItem
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object SimonSays : Module(
    name = "Simon Says",
    desc = "Shows a solution for the Simon Says device."
) {
    private val firstColor by ColorSetting("First Color", Colors.MINECRAFT_GREEN.withAlpha(0.5f), allowAlpha = true, desc = "The color of the first button.")
    private val secondColor by ColorSetting("Second Color", Colors.MINECRAFT_GOLD.withAlpha(0.5f), allowAlpha = true, desc = "The color of the second button.")
    private val thirdColor by ColorSetting("Third Color", Colors.MINECRAFT_RED.withAlpha(0.5f), allowAlpha = true, desc = "The color of the buttons after the second.")
    private val style by SelectorSetting("Style", Renderer.DEFAULT_STYLE, Renderer.styles, desc = Renderer.STYLE_DESCRIPTION)
    private val lineWidth by NumberSetting("Line Width", 2f, 0.1f, 10f, 0.1f, desc = "The width of the box's lines.")
    private val depthCheck by BooleanSetting("Depth check", false, desc = "Boxes show through walls.")
    private val blockWrong by BooleanSetting("Block Wrong Clicks", false, desc = "Blocks wrong clicks, shift will override this.")
    private val optimizeSolution by BooleanSetting("Optimize Solution", false, desc = "Use optimized solution, might fix ss-skip")

    private val startButton = BlockPos(110, 121, 91)
    private val clickInOrder = ArrayList<BlockPos>()
    private var clickNeeded = 0

    private fun resetSolution() {
        clickInOrder.clear()
        clickNeeded = 0
    }

    init {
        onWorldLoad {
            clickInOrder.clear()
            clickNeeded = 0
        }
    }

    @SubscribeEvent
    fun onBlockChange(event: BlockChangeEvent) = with (event) {
        if (DungeonUtils.getF7Phase() != M7Phases.P3) return

        if (pos == startButton && updated.block == Blocks.stone_button && updated.getValue(BlockButtonStone.POWERED)) {
            if (!optimizeSolution) resetSolution()
            return
        }

        if (pos.y !in 120..123 || pos.z !in 92..95) return

        when (pos.x) {
            111 ->
               if (((optimizeSolution && updated.block == Blocks.sea_lantern && old.block == Blocks.obsidian) ||
     (!optimizeSolution && updated.block == Blocks.obsidian && old.block == Blocks.sea_lantern)) &&
     pos !in clickInOrder) {
    clickInOrder.add(pos)
}


            110 ->
                if (updated.block == Blocks.air) {
                    if (!optimizeSolution) resetSolution()
                } else if (old.block == Blocks.stone_button && updated.getValue(BlockButtonStone.POWERED)) {
                    clickNeeded = clickInOrder.indexOf(pos.add(1, 0, 0)) + 1
                    if (clickNeeded >= clickInOrder.size) if (optimizeSolution) resetSolution() else clickNeeded = 0
                }
        }
    }

    @SubscribeEvent
    fun onPostMetadata(event: PostEntityMetadata) {
        if (DungeonUtils.getF7Phase() != M7Phases.P3) return
        val (x, y, z) = (mc.theWorld?.getEntityByID(event.packet.entityId) as? EntityItem)?.takeIf { Item.getIdFromItem(it.entityItem?.item) == 77 }?.positionVector?.floorVec() ?: return
        val index = clickInOrder.indexOf(BlockPos(x, y, z).east())
        if (index == 2 && clickInOrder.size == 3) clickInOrder.removeFirst()
        else if (index == 0 && clickInOrder.size == 2) clickInOrder.reverse()
    }

    @SubscribeEvent
    fun onInteract(event: PlayerInteractEvent) {
        if (
            event.pos == null ||
            event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK ||
            event.world != mc.theWorld
        ) return

        if (event.pos == startButton) {
            if (optimizeSolution) resetSolution()
            return
        }

        if (
            blockWrong &&
            mc.thePlayer?.isSneaking == false &&
            event.pos.x == 110 && event.pos.y in 120..123 && event.pos.z in 92..95 &&
            event.pos.east() != clickInOrder.getOrNull(clickNeeded)
        ) event.isCanceled = true
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (DungeonUtils.getF7Phase() != M7Phases.P3 || clickNeeded >= clickInOrder.size) return

        for (index in clickNeeded until clickInOrder.size) {
            with(clickInOrder[index]) {
                Renderer.drawStyledBox(AxisAlignedBB(x + 0.05, y + 0.37, z + 0.3, x - 0.15, y + 0.63, z + 0.7), when (index) {
                    clickNeeded -> firstColor
                    clickNeeded + 1 -> secondColor
                    else -> thirdColor
                }, style, lineWidth, depthCheck)
            }
        }
    }
}