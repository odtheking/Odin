package me.odinmain.features.impl.floor7

import me.odinmain.features.impl.floor7.WitherDragons.addUselessDecimal
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer
import java.util.Locale

object DragonTimer {

    fun renderTime() {
        WitherDragonsEnum.entries.forEachIndexed { index, dragon ->
            if (dragon.state != WitherDragonState.SPAWNING) return@forEachIndexed

            Renderer.drawStringInWorld(
                "§${dragon.colorCode}${dragon.name.first()}: ${colorDragonTimer(dragon.timeToSpawn)}${String.format(Locale.US, "%.2f", dragon.timeToSpawn / 20.0)}${if (addUselessDecimal) "0" else ""}", dragon.spawnPos,
                color = Color.WHITE, depth = false,
                scale = 0.16f
            )
        }
    }

    fun colorDragonTimer(spawnTime: Int): String {
        return when {
            spawnTime <= 20 -> "§c"
            spawnTime <= 60 -> "§e"
            else -> "§a"
        }
    }
}