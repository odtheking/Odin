package me.odinmain.features.impl.render

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.item.EntityFallingBlock
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object RenderOptimizer : Module(
    name = "Render Optimizer",
    category = Category.RENDER,
    description = "Disables certain render function when they are not necessary, resulting in a decrease in gpu usage."
) {

    private val fallingBlocks: Boolean by BooleanSetting(name = "Remove Falling Blocks", default = true)
    private val p5Mobs: Boolean by BooleanSetting(name = "Remove P5 Armor Stands", default = true)

    @SubscribeEvent
    fun onFallingBlock(event: EntityJoinWorldEvent) {
        if (
            event.entity is EntityArmorStand &&
            p5Mobs &&
            DungeonUtils.getPhase() == 5 &&
            event.entity.posY < 15 && // don't kill dragon tags
            event.entity.posX !in 47.0..61.0 && event.entity.posZ !in 70.0..84.0 && // chest positions
            !event.entity.name.contains("«relic»", true)
        )
            event.entity.setDead()

        if (event.entity is EntityFallingBlock && fallingBlocks) event.entity.setDead()
    }

}