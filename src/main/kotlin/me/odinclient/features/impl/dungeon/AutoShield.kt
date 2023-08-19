package me.odinclient.features.impl.dungeon

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.utils.clock.Clock
import me.odinclient.utils.skyblock.ItemUtils
import me.odinclient.utils.skyblock.PlayerUtils
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object AutoShield : Module(
    "Auto Shield",
    category = Category.DUNGEON
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
            if (ItemUtils.getItemSlot(it) == null) return@forEach
            PlayerUtils.useItem(it)
            clock.update()
            return
        }
    }
}