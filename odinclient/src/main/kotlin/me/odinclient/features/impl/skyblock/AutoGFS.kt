package me.odinclient.features.impl.skyblock

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.skyblock.KuudraUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.sendCommand
import net.minecraft.init.Items

object AutoGFS : Module(
    name = "Auto Gfs",
    description = "Automatically gets pearls from sacks if your inventory doesn't have any.",
    category = Category.DUNGEON
) {
    private val inKuudra: Boolean by BooleanSetting("In Kuudra", true, description = "Only get pearls in Kuudra.")
    private val inDungeon: Boolean by BooleanSetting("In Dungeon", true, description = "Only get pearls in dungeons.")
    private val sackCooldown = Clock(4000)

    init {
        execute(500) {
            if (
                !DungeonUtils.isGhost && mc.currentScreen == null &&
                ((inKuudra && KuudraUtils.inKuudra) || (inDungeon && DungeonUtils.inDungeons))
            ) {

                if (mc.thePlayer?.inventory?.mainInventory?.all { it?.item != Items.ender_pearl } == true && sackCooldown.hasTimePassed()) {
                    sendCommand("gfs ENDER_PEARL 16")
                    sackCooldown.update()
                }
            }
        }
    }
}