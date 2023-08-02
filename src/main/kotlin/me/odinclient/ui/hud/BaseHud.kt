package me.odinclient.ui.hud

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.utils.skyblock.ChatUtils.modMessage
import net.minecraft.client.renderer.GlStateManager

open class BaseHud(var x: Float = 0f, var y: Float = 0f, var scale: Float = 1f) {
    var width = 0f
    var height = 0f

    fun render(example: Boolean = false) {
        GlStateManager.pushMatrix()
        if (!example) {
            if (mc.gameSettings.guiScale == 0) // if you use auto you are a psychopath
                GlStateManager.scale(.5f * scale, .5f * scale, 1f)
            else
                GlStateManager.scale(2 / mc.gameSettings.guiScale.toFloat() * scale, 2 / mc.gameSettings.guiScale.toFloat() * scale, 1f)
        }

        //GlStateManager.scale(scale, scale, 1f)
        val (width, height) = draw(example)
        this.width = width
        this.height = height
        GlStateManager.popMatrix()
    }

    open fun draw(example: Boolean = false): Pair<Float, Float> {
        // made to overwrite
        return Pair(0f, 0f)
    }

}