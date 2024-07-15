package me.odinclient.features.impl.dungeon

import me.odinmain.config.Config
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.name
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.PlayerUtils.windowClick
import net.minecraft.inventory.ContainerChest

object AutoSell : Module(
    name = "Auto Sell",
    description = "Automatically sell items in trades and cookie menus.",
    category = Category.DUNGEON
) {
    val sellList: MutableSet<String> by ListSetting("Sell list", mutableSetOf())
    private val delay: Long by NumberSetting("Delay", 100, 30.0, 300.0, 5.0)
    private val clickType: Int by SelectorSetting("Click Type", "Shift", arrayListOf("Shift", "Middle", "Left"))
    private val addDefaults by ActionSetting("Add defaults") {
        sellList.addAll(defaultItems)
        modMessage("§aAdded default items to auto sell list")
        Config.save()
    }

    init {
        execute(delay = { delay }) {
            if (!enabled) return@execute
            val container = mc.thePlayer?.openContainer as? ContainerChest ?: return@execute

            if (!container.name.equalsOneOf("Trades", "Booster Cookie", "Farm Merchant")) return@execute
            val index = getItemIndexInContainerChest(container, sellList, 54..90) ?: return@execute
            when (clickType) {
                0 -> windowClick(index, PlayerUtils.ClickType.Shift)
                1 -> windowClick(index, PlayerUtils.ClickType.Middle)
                2 -> windowClick(index, PlayerUtils.ClickType.Left)
            }
        }
    }

    private val defaultItems = arrayOf(
        "Enchanted Ice", "Health Potion", "Superboom TNT", "Rotten", "Skeleton Master", "Skeleton Grunt", "Cutlass",
        "Skeleton Lord", "Skeleton Soldier", "Zombie Soldier", "Zombie Knight", "Zombie Commander", "Zombie Lord",
        "Skeletor", "Super Heavy", "Heavy", "Sniper Helmet", "Dreadlord", "Earth Shard", "Zombie Commander Whip",
        "Machine Gun", "Sniper Bow", "Soulstealer Bow", "Silent Death", "Training Weight", "Health Potion VIII",
        "Health Potion 8", "Beating Heart", "Premium Flesh", "Mimic Fragment", "Enchanted Rotten Flesh", "Sign",
        "Enchanted Bone", "Defuse Kit", "Optical Lens", "Tripwire Hook", "Button", "Carpet", "Lever", "Diamond Atom",
        "Health Potion VIII Splash Potion", "Healing Potion 8 Splash Potion", "Healing Potion VIII Splash Potion",
        "Healing VIII Splash Potion", "Healing 8 Splash Potion", "Ancient Claw"
    )
}
