package me.odinclient.features.impl.dungeon

import me.odinclient.utils.skyblock.PlayerUtils.useItem
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.getItemSlot
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object AutoShield : Module(
    "Auto Shield",
    category = Category.DUNGEON,
    description = "Utilizes Wither shield every 5 seconds when health is not full (works with all Wither swords)."
) {
    private val witherSwords = arrayOf("Astraea", "Hyperion", "Valkyrie", "Scylla")
    private val onlyBoss: Boolean by BooleanSetting("Only Boss")
    private val inGUIs: Boolean by BooleanSetting("In GUIs")

    private val clock = Clock(5000)

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (clock.hasTimePassed() || mc.thePlayer == null) return
        if (mc.currentScreen != null && !inGUIs) return
        if (onlyBoss && !DungeonUtils.inBoss) return
        witherSwords.forEach {
            if (getItemSlot(it) == null) return@forEach
            useItem(it)
            clock.update()
            return
        }
    }
}