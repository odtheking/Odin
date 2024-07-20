package me.odinclient.features.impl.skyblock

import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.dungeon.DungeonUtils

object AutoGFS : Module(
    name = "Auto Gfs",
    description = "Automatically gets pearls from sacks if your inventory doesn't have any."
) {
    private val inKuudra by BooleanSetting("In Kuudra", true, description = "Only gfs in Kuudra.")
    private val inDungeon by BooleanSetting("In Dungeon", true, description = "Only gfs in Dungeons.")
    private val refillOnDungeonStart by BooleanSetting("Refill on Dungeon Start", true, description = "Refill when a dungeon starts.")
    private val refillPearl by BooleanSetting("Refill Pearl", true, description = "Refill ender pearls.")
    private val refillJerry by BooleanSetting("Refill Jerry", true, description = "Refill inflatable jerrys.")
    private val refillOnTimer by BooleanSetting("Refill on Timer", true, description = "Refill on a 5s intervals.")

    init {
        execute(5000) {
            if (refillOnTimer) refill()
        }

        onMessage(Regex("\\[NPC] Mort: Here, I found this map when I first entered the dungeon\\.")) {
            if (refillOnDungeonStart) refill()
        }
    }

    private fun refill() {
        if (DungeonUtils.isGhost || mc.currentScreen != null || !(inKuudra && KuudraUtils.inKuudra) && !(inDungeon && DungeonUtils.inDungeons)) return
        val inventory = mc.thePlayer?.inventory?.mainInventory ?: return

        inventory.find { it?.itemID == "ENDER_PEARL" }?.takeIf { refillPearl }?.also { sendCommand("od ep", true) }

        inventory.find { it?.itemID == "INFLATABLE_JERRY" }?.takeIf { refillJerry }?.also { sendCommand("od ij", true) }
    }
}