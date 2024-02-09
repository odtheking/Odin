package me.odinmain.features.impl.floor7

import me.odinmain.utils.addVec
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.world.RenderUtils
import me.odinmain.utils.render.world.RenderUtils.renderVec
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.boss.EntityDragon
import net.minecraftforge.client.event.RenderLivingEvent

object DragonHealth{

    fun renderHP(event: RenderLivingEvent.Post<*>) {
        if (event.entity !is EntityDragon) return
        val dragon = event.entity as EntityDragon
        val percentage = event.entity.health / event.entity.getEntityAttribute(SharedMonsterAttributes.maxHealth).baseValue
        if (percentage <= 0) return
        val color = when {
            dragon.health >= 0.75 -> Color.GREEN
            dragon.health >= 0.5 -> Color.YELLOW
            percentage >= 0.25 -> Color.ORANGE
            else -> Color.RED
        }
        RenderUtils.drawStringInWorld(formatHealth(dragon.health.toInt()), dragon.renderVec.addVec(y = 1.5), color.rgba, false, false, false, 0.2f, true)
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