package me.odinmain.features.impl.floor7

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.world.RenderUtils
import me.odinmain.utils.skyblock.WorldUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object DragonBoxes : Module(
    "Dragon Boxes",
    description = "Shows semi-accurate kill boxes for M7 dragons.",
    category = Category.FLOOR7,
) {
    private val lineThickness: Float by NumberSetting("Line width", 2f, 1.0, 5.0, 0.5)

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (DungeonUtils.getPhase() != 5) return
        DragonColors.entries.forEach { it.checkAlive() }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (DungeonUtils.getPhase() != 5) return
        DragonColors.entries.forEach {
            if (!it.alive) return@forEach
            RenderUtils.drawCustomESPBox(it.aabb, it.color.withAlpha(0.5f), lineThickness, phase = false)
        }
    }

    private enum class DragonColors(
        val pos: BlockPos,
        val aabb: AxisAlignedBB,
        val color: Color,
        var alive: Boolean
    ) {
        Red   (BlockPos(32, 22, 59),  AxisAlignedBB(14.5, 13.0, 45.5, 39.5, 28.0, 70.5),  Color(255,85, 85), true),
        Orange(BlockPos(80, 23, 56),  AxisAlignedBB(72.0, 8.0,  47.0, 102.0,28.0, 77.0),  Color(255,170,0),  true),
        Green (BlockPos(32, 23, 94),  AxisAlignedBB(7.0,  8.0,  80.0, 37.0, 28.0, 110.0), Color(85, 255,85), true),
        Blue  (BlockPos(79, 23, 94),  AxisAlignedBB(71.5, 16.0, 82.5, 96.5, 26.0, 107.5), Color(0,  170,170),true),
        Purple(BlockPos(56, 22, 120), AxisAlignedBB(45.5, 13.0, 113.5,68.5, 23.0, 136.5), Color(170,0,  170),true);

        fun checkAlive() {
            this.alive = !WorldUtils.isAir(this.pos)
        }
    }
}