package me.odinclient.features.impl.dungeon

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.odinclient.OdinClient
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.utils.AsyncUtils
import me.odinclient.utils.Utils.noControlCodes
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.inventory.ContainerChest
import net.minecraft.item.ItemStack
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object AutoSell {

    private var toSell = mutableListOf<Int>()
    private val sellList: MutableList<String> get() = OdinClient.miscConfig.autoSell

    private var tickRamp = 0
    @SubscribeEvent
    fun onClientTick(event: TickEvent.ClientTickEvent) {
        if (!OdinClient.config.autoSell || toSell.size == 0) return
        tickRamp++
        if (tickRamp % 3 != 0) return
        val currentScreen = mc.currentScreen ?: return

        if (currentScreen !is GuiChest) return
        val container = currentScreen.inventorySlots
        if (container !is ContainerChest) return
        val chestName = container.lowerChestInventory.displayName.unformattedText

        if (chestName != "Trades" && !chestName.startsWith("Booster Cookie")) return

        mc.playerController.windowClick(
            mc.thePlayer.openContainer.windowId,
            toSell[0],
            0,
            1,
            mc.thePlayer
        )
        toSell.removeAt(0)
    }

    @OptIn(DelicateCoroutinesApi::class)
    @SubscribeEvent
    fun onGuiOpen(event: GuiOpenEvent) {
        if (event.gui !is GuiChest) return
        val container = (event.gui as GuiChest).inventorySlots
        if (container !is ContainerChest) return
        val chestName = container.lowerChestInventory.displayName.unformattedText
        if (chestName != "Trades" && !chestName.startsWith("Booster Cookie")) return

        toSell.clear()
        GlobalScope.launch {
            val deferred = AsyncUtils.waitUntilLastItem(container)
            try { deferred.await() } catch (_: Exception) {}
            val inventory = container.inventorySlots
            for (i in inventory.size - 36 .. inventory.size) {
                val itemStack: ItemStack = inventory[i]?.stack ?: continue
                val itemName = itemStack.displayName.noControlCodes
                if (sellList.any { itemName.contains(it, true) }) {
                    toSell.add(i)
                }
            }
        }
    }

    val defaultItems = listOf(
        "Enchanted Ice", "Health Potion", "Superboom TNT", "Rotten", "Skeleton Master", "Skeleton Grunt",
        "Skeleton Lord", "Skeleton Soldier", "Zombie Soldier", "Zombie Knight", "Zombie Commander", "Zombie Lord",
        "Skeletor", "Super Heavy", "Heavy", "Sniper Helmet", "Dreadlord", "Earth Shard", "Zombie Commander Whip",
        "Machine Gun", "Sniper Bow", "Soulstealer Bow", "Cutlass", "Silent Death", "Training Weight", "Health Potion VIII",
        "Health Potion 8", "Beating Heart", "Premium Flesh", "Mimic Fragment", "Enchanted Rotten Flesh", "Enchanted Bone",
        "Defuse Kit", "Optic Lens", "Tripwire Hook", "Button", "Carpet", "Lever", "Sign", "Diamond Atom"
    )
}