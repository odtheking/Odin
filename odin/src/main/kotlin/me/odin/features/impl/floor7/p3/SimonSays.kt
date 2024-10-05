package me.odin.features.impl.floor7.p3

import me.odinmain.events.impl.BlockChangeEvent
import me.odinmain.events.impl.PostEntityMetadata
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.*
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
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object SimonSays : Module(
    name = "Simon Says",
    description = "Shows a solution for the Simon Says device.",
    category = Category.FLOOR7,
) {
    private val style by SelectorSetting("Style", Renderer.DEFAULT_STYLE, Renderer.styles, description = Renderer.STYLE_DESCRIPTION)
    private val lineWidth by NumberSetting("Line Width", 2f, 0.1f, 10f, 0.1f, description = "The width of the box's lines.")
    private val depthCheck by BooleanSetting("Depth check", false, description = "Boxes show through walls.")
    private val clearAfter by BooleanSetting("Clear After", false, description = "Clears the clicks when showing next, should work better with ss skip, but will be less consistent.")

    private val firstButton = BlockPos(110, 121, 91)
    private val clickInOrder = ArrayList<BlockPos>()
    private var clickNeeded = 0
    private var currentPhase = 0
    private val phaseClock = Clock(500)

    init {
        onWorldLoad {
            clickInOrder.clear()
            clickNeeded = 0
            currentPhase = 0
        }
    }

    @SubscribeEvent
    fun onBlockChange(event: BlockChangeEvent) {
        if (DungeonUtils.getF7Phase() != M7Phases.P3) return
        val pos = event.pos
        val old = event.old
        val state = event.update

        if (pos == firstButton && state.block == Blocks.stone_button && state.getValue(BlockButtonStone.POWERED)) {
            clickInOrder.clear()
            clickNeeded = 0
            currentPhase = 0
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
                if (clearAfter) clickInOrder.clear()
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
        val entity = mc.theWorld?.getEntityByID(event.packet.entityId) as? EntityItem ?: return
        if (Item.getIdFromItem(entity.entityItem.item) != 77) return
        val pos = BlockPos(entity.posX.floor().toDouble(), entity.posY.floor().toDouble(), entity.posZ.floor().toDouble()).east()
        val index = clickInOrder.indexOf(pos)
        if (index == 2 && clickInOrder.size == 3) clickInOrder.removeFirst()
        else if (index == 0 && clickInOrder.size == 2) clickInOrder.reverse()
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (clickNeeded >= clickInOrder.size) return

        for (index in clickNeeded until clickInOrder.size) {
            val position = clickInOrder[index]
            val x = position.x - .125
            val y = position.y + .3125
            val z = position.z + .25
            val color = when (index) {
                clickNeeded -> Color(0, 170, 0)
                clickNeeded + 1 -> Color(255, 170, 0)
                else -> Color(170, 0, 0)
            }.withAlpha(.5f)
            Renderer.drawStyledBox(AxisAlignedBB(x, y, z, x + .25, y + .375, z + .5), color, style, lineWidth, depthCheck)
        }
    }
}