package me.odinmain.features.impl.floor7

import me.odinmain.OdinMain
import me.odinmain.utils.addVec
import me.odinmain.utils.fastEyeHeight
import me.odinmain.utils.render.RenderUtils.renderVec
import me.odinmain.utils.render.Renderer


object DragonTracer {
    fun renderTracers(dragon: WitherDragonsEnum) {
        if (dragon.spawnTime() > 0) Renderer.draw3DLine(OdinMain.mc.thePlayer.renderVec.addVec(y = fastEyeHeight()), dragon.spawnPos.addVec(0.5, 3.5, 0.5), dragon.color)
    }
}