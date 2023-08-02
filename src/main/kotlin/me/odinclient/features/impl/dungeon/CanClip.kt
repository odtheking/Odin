package me.odinclient.features.impl.dungeon

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.ui.hud.TextHud
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object CanClip : Module(
    name = "Can Clip",
    description = "Tells you if you are currently able to clip through a stair under you",
    category = Category.DUNGEON
) {
    //private val hud: HudData by HudSetting("Can Clip Hud", CanClipHud)

    var canClip = false

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.END || mc.thePlayer == null || !mc.thePlayer.isSneaking || !DungeonUtils.inBoss) {
            canClip = false
            return
        }
        val x = mc.thePlayer.posX % 1
        val z = mc.thePlayer.posZ % 1
        canClip = x in 0.24..0.26 || x in 0.74..0.76 || z in 0.24..0.26 || z in 0.74..0.76
    }

    object CanClipHud: TextHud() {
        override fun getLines(example: Boolean): MutableList<String> {
            return mutableListOf(if (example) "Can Clip" else if (canClip) "Can Clip" else "")
        }
    }
}