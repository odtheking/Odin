package me.odinclient.features.impl.dungeon

import me.odinmain.clickgui.settings.impl.ActionSetting
import me.odinmain.clickgui.settings.impl.ListSetting
import me.odinmain.clickgui.settings.impl.NumberSetting
import me.odinmain.clickgui.settings.impl.SelectorSetting
import me.odinmain.config.Config
import me.odinmain.features.Module
import me.odinmain.utils.containsOneOf
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.name
import me.odinmain.utils.skyblock.ClickType
import me.odinmain.utils.skyblock.PlayerUtils.windowClick
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.inventory.ContainerChest

object AutoSell : Module(
    name = "Auto Sell",
    description = "Automatically sell items in trades and cookie menus. (/autosell)"
) {
    val sellList by ListSetting("Sell list", mutableSetOf<String>())
    private val delay by NumberSetting("Delay", 100L, 75L, 300L, 5L, desc = "The delay between each sell action.", unit = "ms")
    private val clickType by SelectorSetting("Click Type", "Shift", arrayListOf("Shift", "Middle", "Left"), desc = "The type of click to use when selling items.")
    private val addDefaults by ActionSetting("Add defaults", desc = "Add default dungeon items to the auto sell list.") {
        sellList.addAll(defaultItems)
        modMessage("§aAdded default items to auto sell list")
        Config.save()
    }

    init {
        execute(delay = { delay }) {
            if (!enabled || sellList.isEmpty()) return@execute
            val container = mc.thePlayer?.openContainer as? ContainerChest ?: return@execute

            if (!container.name.equalsOneOf("Trades", "Booster Cookie", "Farm Merchant", "Ophelia")) return@execute
            val index = container.inventorySlots?.subList(54, 90)?.firstOrNull { it.stack?.displayName?.containsOneOf(sellList, true) == true }?.slotNumber ?: return@execute
            when (clickType) {
                0 -> windowClick(index, ClickType.Shift)
                1 -> windowClick(index, ClickType.Middle)
                2 -> windowClick(index, ClickType.Left)
            }
        }
    }

    private val defaultItems = arrayOf(
        "enchanted ice", "superboom tnt", "rotten", "skeleton master", "skeleton grunt", "cutlass",
        "skeleton lord", "skeleton soldier", "zombie soldier", "zombie knight", "zombie commander", "zombie lord",
        "skeletor", "super heavy", "heavy", "sniper helmet", "dreadlord", "earth shard", "zombie commander whip",
        "machine gun", "sniper bow", "soulstealer bow", "silent death", "training weight",
        "beating heart", "premium flesh", "mimic fragment", "enchanted rotten flesh", "sign",
        "enchanted bone", "defuse kit", "optical lens", "tripwire hook", "button", "carpet", "lever", "diamond atom",
        "healing viii splash potion", "healing 8 splash potion", "candycomb"
    )
}
