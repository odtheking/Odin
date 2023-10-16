package me.odinclient.features.impl.dungeon

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.skyblock.ChatUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
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
        if (mc.thePlayer?.inventory?.mainInventory?.all { it?.item != Items.ender_pearl} == true && sackCooldown.hasTimePassed()) {
            ChatUtils.sendCommand("gfs ENDER_PEARL 16")
            sackCooldown.update()
        }
    }
}