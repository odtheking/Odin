package me.odinclient.features.impl.dungeon

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.fillItemFromSack
import me.odinmain.utils.skyblock.KuudraUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.skyblockID

object AutoGFS : Module(
    name = "Auto GFS",
    description = "Automatically refills certain items from sack.",
    category = Category.DUNGEON
) {
    private val inKuudra by BooleanSetting("In Kuudra", true, description = "Only gfs in Kuudra.")
    private val inDungeon by BooleanSetting("In Dungeon", true, description = "Only gfs in Dungeons.")
    private val refillOnDungeonStart by BooleanSetting("Refill on Dungeon Start", true, description = "Refill when a dungeon starts.")
    private val refillPearl by BooleanSetting("Refill Pearl", true, description = "Refill ender pearls.")
    private val refillJerry by BooleanSetting("Refill Jerry", true, description = "Refill inflatable jerrys.")
    private val refillTNT by BooleanSetting("Refill TNT", true, description = "Refill superboom tnt.")
    private val refillOnTimer by BooleanSetting("Refill on Timer", true, description = "Refill on a 5s intervals.")
    private val timerIncrements by NumberSetting("Timer Increments", 5L, 1, 60, description = "The interval in which to refill.", unit = "s")

    init {
        execute({ timerIncrements * 1000 }) {
            if (refillOnTimer) refill()
        }

        onMessage(Regex("\\[NPC] Mort: Here, I found this map when I first entered the dungeon\\.")) {
            if (refillOnDungeonStart) refill()
        }
    }

    private fun refill() {
        if (DungeonUtils.isGhost || mc.currentScreen != null || !(inKuudra && KuudraUtils.inKuudra) && !(inDungeon && DungeonUtils.inDungeons)) return
        val inventory = mc.thePlayer?.inventory?.mainInventory ?: return

        inventory.find { it?.skyblockID == "ENDER_PEARL" }?.takeIf { refillPearl }?.also { fillItemFromSack(16, "ENDER_PEARL", "ender_pearl", false) }

        inventory.find { it?.skyblockID == "INFLATABLE_JERRY" }?.takeIf { refillJerry }?.also { fillItemFromSack(64, "INFLATABLE_JERRY", "inflatable_jerry", false) }

        inventory.find { it?.skyblockID == "SUPERBOOM_TNT" }.takeIf { refillTNT }?.also { fillItemFromSack(1, "SUPERBOOM_TNT", "superboom_tnt", false) }
    }
}