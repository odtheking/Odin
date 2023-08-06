package me.odinclient.features.impl.general

import cc.polyfrost.oneconfig.libs.universal.wrappers.message.UTextComponent
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.Utils.noControlCodes
import me.odinclient.utils.skyblock.ScoreboardUtils
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object StopOpeningSouls : Module(
    "Stop Opening Souls",
    category = Category.GENERAL,
    description = "Prevent you from right-clicking during Slaye combat",
) {
    @SubscribeEvent
    fun onPlayerInteract(event: PlayerInteractEvent){
        if (mc.thePlayer == null) return
        if (getScoreboardLines().size < 5) return
        if (!getScoreboardLines()[getScoreboardLines().size - 3].noControlCodes.contains("Slay the boss!")) return
        if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR || event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            val item = mc.thePlayer.heldItem
            if (item != null && mc.thePlayer.isSneaking) {
                if (item.displayName.noControlCodes.contains("Necromancer Sword") || item.displayName.noControlCodes.contains("Reaper Scythe") || item.displayName.noControlCodes.contains("Summoning Ring")){
                    event.isCanceled = true
                }
            }
        }
    }
    private fun getScoreboardLines(): List<String> {
        val lines = ScoreboardUtils.fetchScoreboardLines().map { it.stripControlCodes() }
        return lines
    }
    private fun String?.stripControlCodes(): String = UTextComponent.stripFormatting(this ?: "")
}