package me.odinmain.features.impl.render

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.world.RenderUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.init.Blocks
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object ClickedChests : Module(
    name = "Clicked Chests",
    category = Category.RENDER,
    description = "Draws a box around all the chests you have clicked.",
    tag = TagType.NEW
) {

    val onlyDungeon: Boolean by BooleanSetting("Only in dungeon", true, description = "Toggles if it renders everywhere or only in dungeon")

    private val chests = mutableSetOf<BlockPos>()
    private val color: Color by ColorSetting(
        "Color",
        Color.ORANGE.withAlpha(.4f),
        allowAlpha = true,
        description = "The color of the box."
    )
    private val filled: Boolean by BooleanSetting(
        "Filled",
        true,
        description = "Whether or not the box should be filled."
    )
    private val phase: Boolean by BooleanSetting("Phase", true, description = "Boxes show through walls.")

    @SubscribeEvent
    fun onInteract(event: PlayerInteractEvent) {
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK || !mc.theWorld.getBlockState(event.pos).block.equalsOneOf(
                Blocks.chest,
                Blocks.trapped_chest
            )
        ) return
        chests.add(event.pos)
    }

    init {
        onWorldLoad { chests.clear() }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (onlyDungeon && DungeonUtils.inDungeons) return
        if (chests.isEmpty()) return
        chests.forEach {
            if (filled) {
                val x = it.x + .0625
                val y = it.y.toDouble()
                val z = it.z + .0625
                RenderUtils.drawFilledBox(AxisAlignedBB(x, y, z, x + .875, y + 0.875, z + 0.875), color, phase)
            } else
                RenderUtils.drawCustomBox(
                    it.x + .0625, .875,
                    it.y.toDouble(), .875,
                    it.z + .0625, .875,
                    color,
                    3f,
                    phase
                )
        }
    }
}