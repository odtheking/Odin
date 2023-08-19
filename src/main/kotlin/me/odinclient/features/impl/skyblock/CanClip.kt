package me.odinclient.features.impl.skyblock

import cc.polyfrost.oneconfig.renderer.font.Fonts
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.HudSetting
import me.odinclient.ui.hud.HudElement
import me.odinclient.utils.render.Color
import me.odinclient.utils.render.gui.animations.impl.EaseInOut
import me.odinclient.utils.render.gui.nvg.getTextWidth
import me.odinclient.utils.render.gui.nvg.text
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import kotlin.math.abs

object CanClip : Module(
    name = "Can Clip",
    description = "Tells you if you are currently able to clip through a stair under you",
    category = Category.SKYBLOCK
) {
    private val hud: HudElement by HudSetting("Display", 10f, 10f, 1f, false) {
        if (it) {
            text("Can Clip", 1f, 9f, Color.WHITE, 16f, Fonts.REGULAR)
            getTextWidth("Can Clip", 16f, Fonts.REGULAR) to 16f
        } else {
            text("Can Clip", 1f, 9f, Color(0, 255, 0, animation.get(0f, 1f, !canClip)), 16f, Fonts.REGULAR)
            getTextWidth("Can Clip", 16f, Fonts.REGULAR) to 16f
        }
    }

    private val animation = EaseInOut(300)
    private var canClip = false

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (mc.thePlayer == null || !mc.thePlayer.isSneaking || !DungeonUtils.inBoss) {
            if (canClip) {
                animation.start()
                canClip = false
            }
            return
        }

        val x = abs(mc.thePlayer.posX % 1)
        val z = abs(mc.thePlayer.posZ % 1)

        canClip = if (x in 0.235..0.265 || x in 0.735..0.765 || z in 0.235..0.265 || z in 0.735..0.765) {
            if (!canClip) animation.start()
            true
        } else {
            if (canClip) animation.start()
            false
        }
    }
}