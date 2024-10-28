package me.odinmain.features.impl.floor7

import me.odinmain.features.impl.floor7.WitherDragons.tracerThickness
import me.odinmain.utils.addVec
import me.odinmain.utils.render.Renderer

object DragonTracer {
    fun renderTracers(dragon: WitherDragonsEnum) {
        if (dragon.state == WitherDragonState.SPAWNING)
            Renderer.drawTracer(dragon.spawnPos.addVec(0.5, 3.5, 0.5), color = dragon.color, lineWidth = tracerThickness)
    }
}