package me.odinclient.features.impl.dungeon

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.OdinClient.Companion.miscConfig
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.ActionSetting
import me.odinclient.features.settings.impl.NumberSetting
import me.odinclient.utils.Utils.containsOneOf
import me.odinclient.utils.Utils.name
import me.odinclient.utils.skyblock.ChatUtils.modMessage
import me.odinclient.utils.skyblock.PlayerUtils.shiftClickWindow
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.Slot

object AutoSell : Module(
    name = "Auto Sell",
    description = "Automatically sell items in trades and cookie menus.",
    category = Category.DUNGEON
) {
    private val delay: Long by NumberSetting("Delay", 100, 10.0, 300.0, 5.0)
    private val addDefaults: () -> Unit by ActionSetting("Add defaults") {
        defaultItems.forEach {
            if (!miscConfig.autoSell.contains(it)) {
                miscConfig.autoSell.add(it)
            }
        }
        modMessage("Added default items to auto sell list")
        miscConfig.saveAllConfigs()
    }

    init {
        execute(delay = { delay }) {
            val container = mc.thePlayer.openContainer ?: return@execute
            if (container !is ContainerChest) return@execute

            val chestName = container.name
            if (chestName == "Trades" || chestName == "Booster Cookie") {
                val slot = container.inventorySlots.subList(54, 90).firstOrNull { doSell(it) }?.slotNumber ?: return@execute
                shiftClickWindow(container.windowId, slot)
            }
        }
    }

    private fun doSell(slot: Slot): Boolean {
        return slot.stack?.displayName?.containsOneOf(miscConfig.autoSell, true) == true
    }

    private val defaultItems = arrayOf(
        "Enchanted Ice", "Health Potion", "Superboom TNT", "Rotten", "Skeleton Master", "Skeleton Grunt",
        "Skeleton Lord", "Skeleton Soldier", "Zombie Soldier", "Zombie Knight", "Zombie Commander", "Zombie Lord",
        "Skeletor", "Super Heavy", "Heavy", "Sniper Helmet", "Dreadlord", "Earth Shard", "Zombie Commander Whip",
        "Machine Gun", "Sniper Bow", "Soulstealer Bow", "Cutlass", "Silent Death", "Training Weight", "Health Potion VIII",
        "Health Potion 8", "Beating Heart", "Premium Flesh", "Mimic Fragment", "Enchanted Rotten Flesh", "Enchanted Bone",
        "Defuse Kit", "Optic Lens", "Tripwire Hook", "Button", "Carpet", "Lever", "Sign", "Diamond Atom"
    )
}