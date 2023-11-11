package me.odinmain.features.impl.render

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityFallingBlock
import net.minecraft.util.AxisAlignedBB
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object RenderOptimizer : Module(
    name = "Render Optimizer",
    category = Category.RENDER,
    description = "Disables certain render function when they are not necessary, resulting in a decrease in gpu usage."
) {

    private val fallingBlocks: Boolean by BooleanSetting(name = "Remove Falling Blocks", default = true)
    private val p5Mobs: Boolean by BooleanSetting(name = "Remove P5 Armor Stands", default = true)
    private val p5Boxes = listOf(
        AxisAlignedBB(47.0, 4.0, 70.0, 61.0, 7.0, 84.0),
        AxisAlignedBB(18.0, 4.0, 50.0, 30.0, 22.0, 64.0),
        AxisAlignedBB(18.0, 4.0, 86.0, 30.0, 22.0, 99.0),
        AxisAlignedBB(42.0, 4.0, 134.0, 70.0, 22.0, 119.0),
        AxisAlignedBB(93.0, 4.0, 85.0, 79.0, 24.0, 100.0),
        AxisAlignedBB(95.0, 4.0, 48.0, 80.0, 24.0, 64.0),
    )

    @SubscribeEvent
    fun onFallingBlock(event: EntityJoinWorldEvent) {
        if (
            event.entity is EntityArmorStand &&
            p5Mobs &&
            DungeonUtils.getPhase() == 5 &&
            p5Boxes.none { it.isVecInside(event.entity.positionVector) }
        )
            event.entity.setDead()

        if (event.entity is EntityFallingBlock && fallingBlocks) event.entity.setDead()
    }

}