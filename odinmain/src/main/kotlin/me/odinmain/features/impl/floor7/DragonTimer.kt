package me.odinmain.features.impl.floor7

import me.odinmain.features.impl.floor7.WitherDragons.textScale
import me.odinmain.utils.render.world.RenderUtils

object DragonTimer {

    var toRender: MutableList<Triple<String, Int, WitherDragonsEnum>> = ArrayList()

    fun updateTime() {
        toRender = ArrayList()

        WitherDragonsEnum.entries.forEachIndexed { index, dragon ->
            if (!dragon.spawning) return@forEachIndexed

            toRender.add(Triple(
                "§${dragon.colorCode}${dragon} spawn: ${colorTime(dragon.spawnTime())}ms", index, dragon))
        }
    }

    fun renderTime() {
        if (toRender.size == 0) return
        toRender.forEach {
            RenderUtils.drawStringInWorld(
                it.first,
                it.third.spawnPos,
                depthTest = false,
                increase = false,
                renderBlackBox = false,
                scale = textScale / 5
            )
        }
    }

    private fun colorTime(spawnTime: Long): String {
        return when {
            spawnTime <= 1000 -> "§c$spawnTime"
            spawnTime <= 3000 -> "§e$spawnTime"
            else -> "§a$spawnTime"
        }
    }
}