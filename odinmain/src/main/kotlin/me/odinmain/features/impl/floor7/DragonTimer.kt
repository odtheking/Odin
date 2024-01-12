package me.odinmain.features.impl.floor7

import me.odinmain.features.impl.floor7.WitherDragons.textScale
import me.odinmain.utils.render.world.RenderUtils

object DragonTimer {

    var toRender: MutableList<Triple<String, Int, WitherDragonsEnum>> = ArrayList()

    fun updateTime() {
        toRender = ArrayList()

        WitherDragonsEnum.entries.forEachIndexed { index, dragon ->
            if (dragon.particleSpawnTime == 0L || !dragon.alive) return@forEachIndexed

            when {
                dragon.spawnTime() > 0 -> {
                    toRender.add(
                        Triple(
                            "§${dragon.colorCode}${dragon} spawning in ${colorTime(dragon.spawnTime())}${dragon.spawnTime()} ms",
                            index,
                            dragon
                        )
                    )
                }
                else -> dragon.particleSpawnTime = 0L
            }
        }
    }

    fun renderTime() {
        if (toRender.size == 0) return
        toRender.forEach {
            RenderUtils.drawStringInWorld(
                it.first,
                it.third.textPos,
                depthTest = false,
                increase = true,
                renderBlackBox = true,
                scale = textScale
            )
        }
    }

    private fun colorTime(spawnTime: Long): String {
        return when {
            spawnTime <= 1000 -> "§c"
            spawnTime <= 3000 -> "§e"
            else -> "§a"
        }
    }
}