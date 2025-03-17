package me.odinclient.features.impl.skyblock

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.utils.name
import me.odinmain.utils.skyblock.ClickType
import me.odinmain.utils.skyblock.LocationUtils.isInSkyblock
import me.odinmain.utils.skyblock.PlayerUtils.windowClick
import net.minecraft.init.Blocks
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.ItemBlock
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

/**
 * Module to automatically do the Melody's Harp minigame.
 *
 * Modified from: https://github.com/FloppaCoding/FloppaClient/blob/master/src/main/kotlin/floppaclient/module/impl/misc/AutoHarp.kt
 *
 * @author Aton, X45k
 */
object AutoHarp : Module(
    name = "Auto Harp",
    category = Category.SKYBLOCK,
    description = "Automatically Completes Melody's Harp."
){
    private var lastInv = 0

    @SubscribeEvent
    fun onClientTick(event: TickEvent.ClientTickEvent) {
        if (!isInSkyblock) return
        val container = mc.thePlayer?.openContainer as? ContainerChest ?: return
        if (!container.name.startsWith("Harp -") || container.inventorySlots.size < 54) return
        lastInv = container.inventorySlots.subList(0,36).joinToString("") { it?.stack?.displayName ?: "" }.hashCode().takeIf { lastInv != it } ?: return
        repeat(7) {
            val slot = container.inventorySlots[37 + it]
            if ((slot.stack?.item as? ItemBlock)?.block === Blocks.quartz_block) windowClick(slot.slotNumber, ClickType.Middle)
        }
    }
}