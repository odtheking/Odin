package me.odin.features.impl.floor7.p3

import me.odinmain.events.impl.BlockChangeEvent
import me.odinmain.events.impl.PostEntityMetadata
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.floor
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.devMessage
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.M7Phases
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
    description = "Shows a solution for the Simon Says device.",
    category = Category.FLOOR7,
) {
    private val firstColor by ColorSetting("First Color", Color.GREEN.withAlpha(0.5f), allowAlpha = true, description = "The color of the first button.")
    private val secondColor by ColorSetting("Second Color", Color.ORANGE.withAlpha(0.5f), allowAlpha = true, description = "The color of the second button.")
    private val thirdColor by ColorSetting("Third Color", Color.RED.withAlpha(0.5f), allowAlpha = true, description = "The color of the buttons after the second.")
    private val style by SelectorSetting("Style", Renderer.DEFAULT_STYLE, Renderer.styles, description = Renderer.STYLE_DESCRIPTION)
    private val lineWidth by NumberSetting("Line Width", 2f, 0.1f, 10f, 0.1f, description = "The width of the box's lines.")
    private val depthCheck by BooleanSetting("Depth check", false, description = "Boxes show through walls.")
    private val blockWrong by BooleanSetting("Block Wrong Clicks", false, description = "Blocks wrong clicks, shift will override this.")

    private val firstButton = BlockPos(110, 121, 91)
    private val clickInOrder = ArrayList<BlockPos>()
    private val phaseClock = Clock(500)
    private var currentPhase = 0
    private var clickNeeded = 0

    init {
        onWorldLoad {
            clickInOrder.clear()
            currentPhase = 0
            clickNeeded = 0
        }
    }

    @SubscribeEvent
    fun onBlockChange(event: BlockChangeEvent) {
        if (DungeonUtils.getF7Phase() != M7Phases.P3) return
        val state = event.update
        val pos = event.pos
        val old = event.old

        if (pos == firstButton && state.block == Blocks.stone_button && state.getValue(BlockButtonStone.POWERED)) {
            clickInOrder.clear()
            currentPhase = 0
            clickNeeded = 0
            return
        }

        if (pos.y !in 120..123 || pos.z !in 92..95) return

        if (pos.x == 111 && state.block == Blocks.sea_lantern && pos !in clickInOrder) clickInOrder.add(pos)
        else if (pos.x == 110) {
            if (state.block == Blocks.air) {
                clickNeeded = 0
                if (phaseClock.hasTimePassed()) {
                    currentPhase++
                    phaseClock.update()
                }
            } else if (state.block == Blocks.stone_button) {
                if (old.block == Blocks.air && clickInOrder.size > currentPhase + 1) devMessage("was skipped!?!?!")
                if (old.block == Blocks.stone_button && state.getValue(BlockButtonStone.POWERED)) {
                    val index = clickInOrder.indexOf(pos.add(1, 0, 0)) + 1
                    clickNeeded = if (index >= clickInOrder.size) 0 else index
                }
            }
        }
    }

    @SubscribeEvent
    fun onPostMetadata(event: PostEntityMetadata) {
        if (DungeonUtils.getF7Phase() != M7Phases.P3) return
        val entity = (mc.theWorld?.getEntityByID(event.packet.entityId) as? EntityItem)?.takeIf { Item.getIdFromItem(it.entityItem?.item) == 77 } ?: return
        val index = clickInOrder.indexOf(BlockPos(entity.posX.floor(), entity.posY.floor(), entity.posZ.floor()).east())
        if (index == 2 && clickInOrder.size == 3) clickInOrder.removeFirst()
        else if (index == 0 && clickInOrder.size == 2) clickInOrder.reverse()
    }

    @SubscribeEvent
    fun onInteract(event: PlayerInteractEvent) {
        if (
            event.pos == null ||
            event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK ||
            event.world != mc.theWorld ||
            !blockWrong ||
            mc.thePlayer?.isSneaking == true ||
            event.pos.x != 110 || event.pos.y !in 120..123 || event.pos.z !in 92..95
        ) return

        if (event.pos.east() != clickInOrder.getOrNull(clickNeeded)) event.isCanceled = true
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