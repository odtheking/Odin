package me.odinclient.features.impl.dungeon

import cc.polyfrost.oneconfig.renderer.font.Fonts
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.Hud
import me.odinclient.ui.hud.HudElement
import me.odinclient.utils.render.Color
import me.odinclient.utils.render.gui.animations.impl.EaseInOut
import me.odinclient.utils.render.gui.nvg.*
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object CanClip : Module(
    name = "Can Clip",
    description = "Tells you if you are currently able to clip through a stair under you",
    category = Category.DUNGEON
) {
    //private val hud: HudData by HudSetting("Can Clip Hud", CanClipHud)
    private val animation = EaseInOut(100) // this is me testing stuff

    var canClip = false

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (mc.thePlayer == null || !mc.thePlayer.isSneaking)//!DungeonUtils.inBoss) {
        {
            canClip = false
            return
        }// return

        val x = mc.thePlayer.posX % 1
        val z = mc.thePlayer.posZ % 1

        canClip = if (x in 0.235..0.265 || x in 0.735..0.765 || z in 0.235..0.265 || z in 0.735..0.765) {
            if (!canClip) animation.start()
            true
        } else {
            if (canClip) animation.start()
            false
        }
    }

    @Hud("Can Clip", false)
    object CanClipHud : HudElement() {
        override fun render(vg: NVG, example: Boolean): Pair<Float, Float> {
            val string = "Can Clip"
            vg.text(string, 0f, 1f, Color(0, 255, 0, animation.get(0f, 1f, !canClip)), 16f, Fonts.REGULAR, TextAlign.Left, TextPos.Top)
            return vg.getTextWidth(string, 16f, Fonts.REGULAR) to 16f
        }
    }
}