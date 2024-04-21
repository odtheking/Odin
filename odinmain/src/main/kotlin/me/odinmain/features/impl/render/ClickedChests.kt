package me.odinmain.features.impl.render

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.block.Block
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ClickedChests : Module(
    name = "Clicked Chests",
    category = Category.RENDER,
    description = "Draws a box around all the chests you have clicked."
) {
    private val color: Color by ColorSetting("Color", Color.ORANGE.withAlpha(.4f), allowAlpha = true, description = "The color of the box.")
    private val lockedColor: Color by ColorSetting("Locked Color", Color.RED.withAlpha(.4f), allowAlpha = true, description = "The color of the box when the chest is locked.")
    private val style: Int by SelectorSetting("Style", "Filled", arrayListOf("Filled", "Outline", "Filled Outline"), description = "Whether or not the box should be filled.")
    private val phase: Boolean by BooleanSetting("Depth Check", false, description = "Boxes show through walls.")
    private val timeToStay: Long by NumberSetting("Time To Stay (seconds)", 7L, 1L, 60L, 1L, description = "The time the chests should remain highlighted.")

    private data class Chest(val block: Block, val pos: BlockPos, val timeAdded: Long, var locked: Boolean = false)
    private val chests = mutableListOf<Chest>()

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!DungeonUtils.inDungeons || chests.isEmpty() || DungeonUtils.inBoss) return
        chests.removeAll { System.currentTimeMillis() - it.timeAdded >= timeToStay * 1000 }

        chests.forEach {
            Renderer.drawBox(RenderUtils.getBlockAABB(it.block, it.pos), if (it.locked) lockedColor else color, depth = phase,
                outlineAlpha = if (style == 0) 0 else color.alpha, fillAlpha = if (style == 1) 0 else color.alpha)
        }
    }

    init {
        onWorldLoad {
            chests.clear()
        }
        onMessage("That chest is locked!", true) {
            if (chests.isEmpty()) return@onMessage
            chests.lastOrNull()?.let { it.locked = true }
        }

        onPacket(C08PacketPlayerBlockPlacement::class.java) { packet ->
            val pos = packet.position
            val blockState = mc.theWorld?.getBlockState(pos)
            val block = blockState?.block ?: return@onPacket
            if (!DungeonUtils.isSecret(blockState, pos) || chests.any{ it.pos == pos }) return@onPacket

            chests.add(Chest(block, pos, System.currentTimeMillis()))
        }
    }
}
