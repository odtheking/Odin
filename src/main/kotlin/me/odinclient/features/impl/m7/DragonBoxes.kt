package me.odinclient.features.impl.m7

import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.NumberSetting
import me.odinclient.utils.render.world.RenderUtils
import me.odinclient.utils.skyblock.WorldUtils
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.awt.Color

object DragonBoxes : Module(
    "Dragon Boxes",
    category = Category.M7
) {
    private val lineThickness: Float by NumberSetting("Line width", 2f, 1.0, 5.0, 0.5)

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (DungeonUtils.getPhase() != 5) return
        DragonColors.values().forEach { it.checkAlive() }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (DungeonUtils.getPhase() != 5) return
        // Blue
        if (DragonColors.Blue.alive)
            RenderUtils.drawCustomEspBox(
                71.5, 25.0,
                16.0, 10.0,
                82.5, 25.0,
                Color(0, 170, 170, 255),
                lineThickness,
                false
            )
        // Purple
        if (DragonColors.Purple.alive)
            RenderUtils.drawCustomEspBox(
                45.5, 23.0,
                13.0, 10.0,
                113.5, 23.0,
                Color(170, 0, 170, 255),
                lineThickness,
                false
            )
        // Green
        if (DragonColors.Green.alive)
            RenderUtils.drawCustomEspBox(
                7.0, 30.0,
                8.0, 20.0,
                80.0, 30.0,
                Color(85, 255, 85, 255),
                lineThickness,
                false
            )
        // Red
        if (DragonColors.Red.alive)
            RenderUtils.drawCustomEspBox(
                14.5, 25.0,
                13.0, 15.0,
                45.5, 25.0,
                Color(255, 85, 85, 255),
                lineThickness,
                false
            )
        // Orange
        if (DragonColors.Orange.alive)
            RenderUtils.drawCustomEspBox(
                72.0, 30.0,
                8.0, 20.0,
                47.0, 30.0,
                Color(255, 170, 0, 255),
                lineThickness,
                false
            )
    }

    private enum class DragonColors(
        val pos: BlockPos,
        var alive: Boolean
    ) {
        Red(BlockPos(32, 22, 59), true),
        Orange(BlockPos(80, 23, 56), true),
        Green(BlockPos(32, 23, 94), true),
        Blue(BlockPos(79, 23, 94), true),
        Purple(BlockPos(56, 22, 120), true);

        fun checkAlive() {
            this.alive = !WorldUtils.isAir(this.pos)
        }
    }
}