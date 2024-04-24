package me.odinmain.features.impl.floor7

import me.odinmain.utils.addVec
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.RenderUtils.renderVec
import me.odinmain.utils.render.Renderer
import net.minecraft.entity.boss.EntityDragon
import net.minecraftforge.client.event.RenderLivingEvent

object DragonHealth{
    fun renderHP(event: RenderLivingEvent.Post<*>) {
        if (event.entity !is EntityDragon || event.entity.health <= 0) return

        Renderer.drawStringInWorld(colorHealth(event.entity.health), event.entity.renderVec.addVec(y = 1.5), Color.WHITE, depth = false, scale = 0.2f, shadow = true)
    }

    private fun colorHealth(health: Float): String {
        return when {
            health >= 0.75 -> "§a${formatHealth(health)}"
            health >= 0.5 -> "§e${formatHealth(health)}"
            health >= 0.25 -> "§6${formatHealth(health)}"
            else -> "§c${formatHealth(health)}"
        }
    }

    private fun formatHealth(health: Float): String {
        return when {
            health >= 1_000_000_000 -> "${String.format("%.2f", health / 1_000_000_000)}b"
            health >= 1_000_000 -> "${(health.toInt() / 1_000_000)}m"
            health >= 1_000 -> "${(health.toInt() / 1_000)}k"
            else -> "${health.toInt()}"
        }
    }
}