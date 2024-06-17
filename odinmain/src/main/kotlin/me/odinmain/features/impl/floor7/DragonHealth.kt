package me.odinmain.features.impl.floor7

import me.odinmain.utils.addVec
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.RenderUtils.renderVec
import me.odinmain.utils.render.Renderer
import net.minecraft.entity.boss.EntityDragon

object DragonHealth{
    fun renderHP() {
        WitherDragonsEnum.entries.forEach {
            val dragon = it.entity as? EntityDragon ?: return
            if (dragon.health > 0)
                Renderer.drawStringInWorld(colorHealth(dragon.health), dragon.renderVec.addVec(y = 1.5), Color.WHITE, depth = false, scale = 0.2f, shadow = true)
        }
    }

    private fun colorHealth(health: Float): String {
        return when {
            health >= 750_000_000 -> "§a${formatHealth(health)}"
            health >= 500_000_000 -> "§e${formatHealth(health)}"
            health >= 250_000_000 -> "§6${formatHealth(health)}"
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
