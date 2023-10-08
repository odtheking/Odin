package me.odinclient.features.impl.dungeon

import me.odinclient.config.MiscConfig
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.ActionSetting
import me.odinclient.features.settings.impl.NumberSetting
import me.odinclient.utils.Utils.containsOneOf
import me.odinclient.utils.Utils.equalsOneOf
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
            if (it !in MiscConfig.autoSell) {
                MiscConfig.autoSell.add(it)
            }
        }
        modMessage("Added default items to auto sell list")
        MiscConfig.saveAllConfigs()
    }

    init {
        execute(delay = { delay }) {
            if (!enabled) return@execute
            val container = mc.thePlayer.openContainer ?: return@execute
            if (container !is ContainerChest) return@execute

            val chestName = container.name
            if (chestName.equalsOneOf("Trades", "Booster Cookie", "Farm Merchant")) {
                shiftClickWindow(
                    container.inventorySlots.subList(54, 90).firstOrNull { doSell(it) }?.slotNumber ?: return@execute
                )
            }
        }
    }

    private fun doSell(slot: Slot): Boolean {
        return slot.stack?.displayName?.containsOneOf(MiscConfig.autoSell, true) == true
    }

    private val defaultItems = arrayOf(
        "Enchanted Ice", "Health Potion", "Superboom TNT", "Rotten", "Skeleton Master", "Skeleton Grunt", "Cutlass",
        "Skeleton Lord", "Skeleton Soldier", "Zombie Soldier", "Zombie Knight", "Zombie Commander", "Zombie Lord",
        "Skeletor", "Super Heavy", "Heavy", "Sniper Helmet", "Dreadlord", "Earth Shard", "Zombie Commander Whip",
        "Machine Gun", "Sniper Bow", "Soulstealer Bow", "Silent Death", "Training Weight", "Health Potion VIII",
        "Health Potion 8", "Beating Heart", "Premium Flesh", "Mimic Fragment", "Enchanted Rotten Flesh", "Sign",
        "Enchanted Bone", "Defuse Kit", "Optic Lens", "Tripwire Hook", "Button", "Carpet", "Lever", "Diamond Atom"
    )
}
