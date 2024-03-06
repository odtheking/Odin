package me.odinmain.features.impl.render

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraft.util.AxisAlignedBB as AABB

object ClickedChests : Module(
    name = "Clicked Chests",
    category = Category.RENDER,
    description = "Draws a box around all the chests you have clicked."
) {
    private val color: Color by ColorSetting("Color", Color.ORANGE.withAlpha(.4f), allowAlpha = true, description = "The color of the box.")
    private val lockedColor: Color by ColorSetting("Locked Color", Color.RED.withAlpha(.4f), allowAlpha = true, description = "The color of the box when the chest is locked.")
    private val style: Int by SelectorSetting("Style", "Filled", arrayListOf("Filled", "Outline", "Filled Outline"), description = "Whether or not the box should be filled.")
    private val phase: Boolean by BooleanSetting("Phase", true, description = "Boxes show through walls.")
    private val timeToStay: Long by NumberSetting("Time To Stay (seconds)", 7L, 1L, 60L, 1L, description = "The time the chests should remain highlighted.")

    private data class Chest(val pos: BlockPos, val timeAdded: Long, var Locked: Boolean = false)
    private val chests = mutableListOf<Chest>()

    @SubscribeEvent
    fun onInteract(event: PlayerInteractEvent) {

        if (!DungeonUtils.inDungeons || event.pos == null) return

        if (!DungeonUtils.isSecret(mc.theWorld?.getBlockState(event.pos) ?: return, event.pos) ||
            event.action != RIGHT_CLICK_BLOCK || chests.any { it.pos == event.pos }) return
     
        chests.add(Chest(event.pos, System.currentTimeMillis()))
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!DungeonUtils.inDungeons || chests.isEmpty()) return
        chests.removeAll { System.currentTimeMillis() - it.timeAdded >= timeToStay * 1000 }

        for (it in chests) {
            val finalColor = if (it.Locked) lockedColor else color
            val aabb = AABB(it.pos.x + .0625, it.pos.y.toDouble(), it.pos.z + .0625, it.pos.x + .875, it.pos.y + 0.875, it.pos.z + 0.875)
            when (style) {
                0 -> Renderer.drawBox(aabb, finalColor, depth = phase, outlineAlpha = 0)
                1 -> Renderer.drawBox(aabb, finalColor, 0f, depth = phase)
                2 -> Renderer.drawBox(aabb, finalColor, depth = phase)

            }
        }
    }

    init {
        onWorldLoad {
            chests.clear()
        }
        onMessage("This chest is locked", true) {
            if (chests.isEmpty()) return@onMessage
            chests.lastOrNull()?.let { it.Locked = true }
        }
    }
}
