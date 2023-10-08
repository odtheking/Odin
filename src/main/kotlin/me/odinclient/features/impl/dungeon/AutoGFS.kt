package me.odinclient.features.impl.dungeon

import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.clock.Clock
import me.odinclient.utils.skyblock.ChatUtils
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.init.Items
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object AutoGFS : Module(
    name = "Auto GFS",
    description = "Automatically gets pearls from sacks if your inventory doesn't have any.",
    category = Category.DUNGEON,
    tag = TagType.NEW
) {
    private val sackCooldown = Clock(1000)

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || !DungeonUtils.inDungeons) return
        if (mc.thePlayer?.inventory?.mainInventory?.none { it?.item == Items.ender_pearl} == true && sackCooldown.hasTimePassed()) {
            ChatUtils.sendCommand("gfs ENDER_PEARL 16")
            sackCooldown.update()
        }
    }
}