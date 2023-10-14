package me.odinclient.features.impl.dungeon

import me.odinclient.utils.skyblock.PlayerUtils.shiftClickWindow
import me.odinmain.config.MiscConfig
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.ActionSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.containsOneOf
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.name
import me.odinmain.utils.skyblock.ChatUtils.modMessage
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.Slot

object AutoSell : Module(
    name = "Auto Sell",
    description = "Automatically sell items in trades and cookie menus.",
    category = Category.DUNGEON
) {
    private val delay: Long by NumberSetting("Delay", 100, 30.0, 300.0, 5.0)
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