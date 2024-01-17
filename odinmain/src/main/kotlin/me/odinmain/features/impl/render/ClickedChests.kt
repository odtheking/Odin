package me.odinmain.features.impl.render

import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.world.RenderUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraft.util.AxisAlignedBB as AABB

object ClickedChests : Module(
    name = "Clicked Chests",
    category = Category.RENDER,
    description = "Draws a box around all the chests you have clicked.",
    tag = TagType.NEW
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
        if (/*!DungeonUtils.inDungeons || */event.pos == null) return

        if (!DungeonUtils.isSecret(mc.theWorld?.getBlockState(event.pos) ?: return, event.pos) || event.action != RIGHT_CLICK_BLOCK || chests.any { it.pos == event.pos }) return
        chests.add(Chest(event.pos, System.currentTimeMillis()))
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (!DungeonUtils.inDungeons || chests.isEmpty()) return
        chests.removeAll { System.currentTimeMillis() - it.timeAdded >= timeToStay * 1000 }

        for (it in chests) {
            when (style) {
                0 -> {
                    val x = it.pos.x + .0625
                    val y = it.pos.y.toDouble()
                    val z = it.pos.z + .0625
                    RenderUtils.drawFilledBox(AABB(x, y, z, x + .875, y + 0.875, z + 0.875), if (it.Locked) lockedColor else color, phase)
                }
                1 -> {
                    RenderUtils.drawCustomBox(
                        it.pos.x + .0625, .875,
                        it.pos.y.toDouble(), .875,
                        it.pos.z + .0625, .875,
                        if (it.Locked) lockedColor else color,
                        3f,
                        phase
                    )
                }
                2 -> {
                    val x = it.pos.x + .0625
                    val y = it.pos.y.toDouble()
                    val z = it.pos.z + .0625
                    RenderUtils.drawBoxWithOutline(AABB(x, y, z, x + .875, y + 0.875, z + 0.875), if (it.Locked) lockedColor else color, phase)
                }
            }

        }
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        chests.clear()
    }

    fun onChat(event: ChatPacketEvent) {
        if (!event.message.contains("This chest is locked")) return
        if (chests.isEmpty()) return

        chests.forEach { if (it.pos == mc.objectMouseOver.blockPos) it.Locked = true }
    }
}
