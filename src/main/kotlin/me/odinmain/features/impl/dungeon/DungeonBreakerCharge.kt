package me.odinmain.features.impl.dungeon

import me.odinmain.events.impl.PacketEvent
import me.odinmain.features.Module
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.Colors
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.lore
import me.odinmain.utils.skyblock.skyblockID
import me.odinmain.utils.ui.getTextWidth
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object DungeonBreakerCharge : Module(
    name = "Stonk Charge Display",
    description = "Displays the amount of charges left in your Dungeon Breaker"
) {
    private val regex = "Charges: (\\d+)/(\\d+)⸕".toRegex()
    private var charges = 0
    private var max = 0

    private val hud by HUD("Display", "Shows the amount of charges left in your Dungeon Breaker.") {
        if (it || (max != 0 && DungeonUtils.inDungeons)) RenderUtils.drawText("§cCharges: §e${if (it) 17 else charges}§7/§e${if (it) 20 else max}§c⸕", 1f, 1f, Colors.WHITE)
        getTextWidth("Charges: 17/20⸕") + 2f to 10f
    }

    @SubscribeEvent
    fun onPacketReceive(event: PacketEvent.Receive) {
        if (event.packet !is S2FPacketSetSlot || !DungeonUtils.inDungeons) return
        mc.thePlayer?.inventory?.mainInventory
            ?.find { it.skyblockID.equals("DUNGEONBREAKER", true) }
            ?.lore?.firstOrNull { regex.containsMatchIn(it.noControlCodes) }
            ?.let { lore ->
                regex.find(lore.noControlCodes)?.let { match ->
                    charges = match.groupValues[1].toInt()
                    max = match.groupValues[2].toInt()
                }
            }
    }
}
