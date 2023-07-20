package me.odinclient.features.impl.dungeon

import me.odinclient.OdinClient.Companion.config
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.features.Category
import me.odinclient.features.Module
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
    private var delay = System.currentTimeMillis()

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (delay > System.currentTimeMillis() || mc.thePlayer == null) return

        if (config.inBoss && !DungeonUtils.inBoss) return
        witherSwords.forEach {
            if (ItemUtils.getItemSlot(it) == -1) return@forEach
            PlayerUtils.useItem(it)
            delay = System.currentTimeMillis() + 5000
            return
        }
    }
}