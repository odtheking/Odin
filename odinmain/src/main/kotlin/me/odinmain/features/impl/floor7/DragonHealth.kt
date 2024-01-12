package me.odinmain.features.impl.floor7

import me.odinmain.OdinMain.mc
import me.odinmain.utils.addVec
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.world.RenderUtils
import me.odinmain.utils.render.world.RenderUtils.renderVec
import net.minecraft.entity.boss.EntityDragon

object DragonHealth{

    fun renderHP() {
        mc.theWorld.loadedEntityList.filterIsInstance<EntityDragon>().forEach { dragon ->
            val percentage = dragon.health / dragon.maxHealth
            if (percentage <= 0) return@forEach
            val color = Color(((1 - percentage) * 255).toInt(), (percentage * 255).toInt(), 0, 1f)
            RenderUtils.drawStringInWorld(formatHealth(dragon.health.toInt()), dragon.renderVec.addVec(y = .5), color.rgba, false, false, false, 0.2f, true)
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