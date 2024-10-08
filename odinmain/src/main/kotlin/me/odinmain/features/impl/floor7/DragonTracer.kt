package me.odinmain.features.impl.floor7

import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.floor7.WitherDragons.tracerThickness
import me.odinmain.utils.addVec
import me.odinmain.utils.fastEyeHeight
import me.odinmain.utils.render.RenderUtils.renderVec
import me.odinmain.utils.render.Renderer

object DragonTracer {
    fun renderTracers(dragon: WitherDragonsEnum) {
        if (dragon.state == WitherDragonState.SPAWNING)
            Renderer.draw3DLine(mc.thePlayer.renderVec.addVec(y = fastEyeHeight()), dragon.spawnPos.addVec(0.5, 3.5, 0.5), color = dragon.color, lineWidth = tracerThickness)
    }
}