package me.odinmain.features.impl.floor7

import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Renderer

object DragonTimer {

    fun updateTime() {
        WitherDragonsEnum.entries.forEachIndexed { index, dragon ->
            if (dragon.state == WitherDragonState.SPAWNING) dragon.timeToSpawn = (dragon.timeToSpawn - 1).coerceAtLeast(0)
        }
    }

    fun renderTime() {
        WitherDragonsEnum.entries.forEachIndexed { index, dragon ->
            if (dragon.state != WitherDragonState.SPAWNING) return@forEachIndexed

            Renderer.drawStringInWorld(
                "§${dragon.colorCode}${dragon.name.first()}${colorDragonTimer(dragon.timeToSpawn / 20)}", dragon.spawnPos,
                color = Color.WHITE, depth = false,
                scale = 0.16f
            )
        }
    }

    fun colorDragonTimer(spawnTime: Int): String {
        return when {
            spawnTime <= 20 -> "§c$spawnTime"
            spawnTime <= 60 -> "§e$spawnTime"
            else -> "§a$spawnTime"
        }
    }
}