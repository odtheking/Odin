package me.odinclient.features.impl.skyblock

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.dungeon.DungeonUtils

object AutoGFS : Module(
    name = "Auto Gfs",
    description = "Automatically gets pearls from sacks if your inventory doesn't have any.",
    category = Category.DUNGEON
) {
    private val inKuudra: Boolean by BooleanSetting("In Kuudra", true, description = "Only get pearls in Kuudra.")
    private val inDungeon: Boolean by BooleanSetting("In Dungeon", true, description = "Only get pearls in dungeons.")
    private val refillOnDungeonStart: Boolean by BooleanSetting("Refill on Dungeon Start", true, description = "Refill pearls when a dungeon starts.")
    private val refillOnTimer: Boolean by BooleanSetting("Refill on Timer", true, description = "Refill pearls on a timer.")

    init {
        execute(4000) {
            if (refillOnTimer) refill()
        }

        onMessage(Regex("\\[NPC] Mort: Here, I found this map when I first entered the dungeon\\.")) {
            if (refillOnDungeonStart) refill()
        }
    }

    private fun refill() {
        if (!DungeonUtils.isGhost && mc.currentScreen == null && ((inKuudra && KuudraUtils.inKuudra) || (inDungeon && DungeonUtils.inDungeons))) {
            val enderPearlStackSize = mc.thePlayer?.inventory?.mainInventory?.find { it?.itemID == "ENDER_PEARL" }?.stackSize
            val jerryStackSize = mc.thePlayer?.inventory?.mainInventory?.find { it?.itemID == "INFLATABLE_JERRY" }?.stackSize
            if (enderPearlStackSize == null || enderPearlStackSize < 16) sendCommand("od ep", true)
            if (jerryStackSize == null || jerryStackSize < 64) sendCommand("od ij", true)
        }
    }
}