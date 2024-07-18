package me.odinmain.features.impl.floor7

import com.github.stivais.ui.color.Color
import me.odinmain.features.impl.floor7.WitherDragons.textScale
import me.odinmain.utils.render.Renderer

object DragonTimer {

    var toRender: MutableList<Triple<String, Int, WitherDragonsEnum>> = ArrayList()

    private fun updateTime() {
        toRender = ArrayList()

        WitherDragonsEnum.entries.forEachIndexed { index, dragon ->
            if (dragon.state == WitherDragonState.SPAWNING && dragon.spawnTime() > 0)
                toRender.add(Triple("§${dragon.colorCode}${dragon} spawn: ${colorTime(dragon.spawnTime())}ms", index, dragon))
        }
    }

    fun renderTime() {
        updateTime()
        if (toRender.isEmpty()) return
        toRender.forEach {
            Renderer.drawStringInWorld(
                it.first, it.third.spawnPos,
                color = Color.WHITE, depth = false,
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