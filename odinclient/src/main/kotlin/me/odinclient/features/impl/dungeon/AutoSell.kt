package me.odinclient.features.impl.dungeon

import me.odinmain.config.Config
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.*
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.PlayerUtils.windowClick
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.inventory.ContainerChest
import net.minecraft.inventory.Slot

object AutoSell : Module(
    name = "Auto Sell",
    description = "Automatically sell items in trades and cookie menus.",
    category = Category.DUNGEON
) {
    val sellList: MutableSet<String> by ListSetting("Sell list", mutableSetOf())
    private val delay: Long by NumberSetting("Delay", 100, 30.0, 300.0, 5.0)
    private val clickType: Int by SelectorSetting("Click Type", "Shift", arrayListOf("Shift", "Middle", "Left"))
    private val addDefaults: () -> Unit by ActionSetting("Add defaults") {
        sellList.addAll(defaultItems)
        modMessage("Added default items to auto sell list")
        Config.save()
    }

    init {
        execute(delay = { delay }) {
            if (!enabled) return@execute
            val container = mc.thePlayer.openContainer ?: return@execute
            if (container !is ContainerChest) return@execute

            val chestName = container.name
            if (chestName.equalsOneOf("Trades", "Booster Cookie", "Farm Merchant")) {
                val index = container.inventorySlots.subList(54, 90).firstOrNull { doSell(it) }?.slotNumber ?: return@execute
                when (clickType) {
                    0 -> windowClick(index, PlayerUtils.ClickType.Shift)
                    1 -> windowClick(index, PlayerUtils.ClickType.Middle)
                    2 -> windowClick(index, PlayerUtils.ClickType.Left)
                }
            }
        }
    }

    private fun doSell(slot: Slot): Boolean {
        return slot.stack?.displayName?.containsOneOf(sellList, true) == true
    }

    private val defaultItems = arrayOf(
        "Enchanted Ice", "Health Potion", "Superboom TNT", "Rotten", "Skeleton Master", "Skeleton Grunt", "Cutlass",
        "Skeleton Lord", "Skeleton Soldier", "Zombie Soldier", "Zombie Knight", "Zombie Commander", "Zombie Lord",
        "Skeletor", "Super Heavy", "Heavy", "Sniper Helmet", "Dreadlord", "Earth Shard", "Zombie Commander Whip",
        "Machine Gun", "Sniper Bow", "Soulstealer Bow", "Silent Death", "Training Weight", "Health Potion VIII",
        "Health Potion 8", "Beating Heart", "Premium Flesh", "Mimic Fragment", "Enchanted Rotten Flesh", "Sign",
        "Enchanted Bone", "Defuse Kit", "Optical Lens", "Tripwire Hook", "Button", "Carpet", "Lever", "Diamond Atom"
    )
}
