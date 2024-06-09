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

    init {
        execute(4000) {
            if (!DungeonUtils.isGhost && mc.currentScreen == null && ((inKuudra && KuudraUtils.inKuudra) || (inDungeon && DungeonUtils.inDungeons))) {
                val enderPearlStackSize = mc.thePlayer?.inventory?.mainInventory?.find { it?.itemID == "ENDER_PEARL" }?.stackSize ?: return@execute
                if (enderPearlStackSize < 16) sendCommand("od ep", true)
            }
        }
    }
}