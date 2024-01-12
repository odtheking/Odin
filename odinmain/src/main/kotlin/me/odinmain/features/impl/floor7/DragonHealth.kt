package me.odinmain.features.impl.floor7

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.utils.addVec
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.world.RenderUtils
import me.odinmain.utils.render.world.RenderUtils.renderVec
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.entity.boss.EntityDragon
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object DragonHealth : Module(
    name = "Dragon Health",
    description = "Displays health of the wither king dragons",
    category = Category.FLOOR7
) {

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        if (DungeonUtils.getPhase() != 5 || mc.theWorld == null) return
        mc.theWorld.loadedEntityList.filterIsInstance<EntityDragon>().forEach { dragon ->
            val percentage = dragon.health / dragon.maxHealth
            val color = Color((percentage * 255).toInt(), ((1 - percentage) * 255).toInt(), 0, 1f)
            RenderUtils.drawStringInWorld(formatHealth(dragon.health.toInt()), dragon.renderVec.addVec(y = .5), color.javaColor.rgb, false, false, false, 0.2f, true)
        }
    }

    private fun formatHealth(health: Int): String {
        return when {
            health >= 1_000_000_000 -> "${(health / 1_000_000_000)}b"
            health >= 1_000_000 -> "${(health / 1_000_000)}m"
            health >= 1_000 -> "${(health / 1_000)}k"
            else -> "$health"
        }
    }

}