package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.OdinMod
import com.odtheking.odin.OdinMod.scope
import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.ActionSetting
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.GuiEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.*
import com.odtheking.odin.utils.network.WebUtils.fetchJson
import com.odtheking.odin.utils.render.getStringWidth
import com.odtheking.odin.utils.render.text
import com.odtheking.odin.utils.render.textDim
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import kotlinx.coroutines.launch
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

object Croesus : Module(
    name = "Croesus",
    description = "Enhances the Croesus chest menu with profit calculations and highlights."
) {
    private val highlightState by BooleanSetting("Highlight State", true, desc = "Highlights chests in the Croesus menu based on their claim status.")
    private val highlightProfitable by BooleanSetting("Highlight Profitable", true, desc = "Highlights the most and 2nd most profitable chests.")
    private val includeEssence by BooleanSetting("Include Essence", true, desc = "Includes essence value in profit calculations.")
    private val hideClaimed by BooleanSetting("Hide Claimed", true, desc = "Hides chests that have already been claimed.")
    private val includeKey by BooleanSetting("Include Key", desc = "Count Dungeon Chest Key as unclaimed.").withDependency { hideClaimed }
    private val minimized by BooleanSetting("Minimized", false, desc = "Only display profit for each chest instead of all items.")

    private val croesusHud by HUD("Croesus Chest HUD", "Displays all chest contents with prices, sorted by profit.") {
        if (!it) return@HUD 0 to 0
        drawOverlay(true)
    }

    private val chestCount by HUD("Chest Count HUD", "Displays the number of chests opened in the current Croesus session.") {
        if (DungeonUtils.inDungeons || it) textDim("§6Chests: §a${currentChestCount}", 0, 0)
        else 0 to 0
    }

    private val chestWarning by NumberSetting("Chest Warning Threshold", 55, 0, 60, desc = "Displays a warning in the chest profit HUD if the profit is below this amount.")
    private val refresh by ActionSetting("Refresh Prices", desc = "Manually refresh the cached prices used for profit calculations.") {
        scope.launch {
            cachedPrices = fetchJson<Map<String, Double>>("https://api.odtheking.com/lb/lowestbins").getOrElse { OdinMod.logger.error("Failed to fetch lowest bin prices for Croesus module.", it); emptyMap() }
            modMessage("§aCroesus prices refreshed.")
        }
    }

    var cachedPrices = emptyMap<String, Double>()
    private var currentChestCount = 0

    private val chestNameRegex = Regex("^(Wood|Iron|Gold|Diamond|Emerald|Obsidian|Bedrock) Chest$")
    private val previewEnchantedBookRegex = Regex("^Enchanted Book \\(?([\\w ]+) (\\w+)\\)$")
    private val chestPreviewScreenRegex = Regex("^(?:Master )?Catacombs - ([FloorVI\\d ]*)$")
    private val chestStatusRegex = Regex("^Opened Chest: (.+)$|^No more chests to open!$")
    private val chestOpenedRegex = Regex("^Opened Chest: (.+)$")
    private val unclaimedChestsRegex = Regex("^ Unclaimed chests: (\\d+)$")
    private val chestEnchantsRegex = Regex("^\\{([a-zA-Z0-9_]+):(\\d+)}$")
    private val previewEssenceRegex = Regex("^(\\w+) Essence x(\\d+)$")
    private val previewShardRegex = Regex("^([A-Za-z ]+) Shard x1$")
    private val extraStatsRegex = Regex(" {29}> EXTRA STATS <")
    private val chestCostRegex = Regex("^([\\d,]+) Coins$")
    private val shardRegex = Regex("^([A-Za-z ]+) Shard$")

    private val ultimateEnchants = setOf(
        "Soul Eater", "Combo", "Legion", "One For All", "Rend",
        "Bank", "Swarm", "Last Stand", "Wisdom", "No Pain No Gain"
    )

    private data class ChestItem(val name: String, val price: Double)
    private data class ChestData(val name: Component, val items: List<ChestItem>, val profit: Double, val slotIndex: Int)

    private var chestData = listOf<ChestData>()
    private var currentChestProfit: Double? = null
    private var mostProfitableSlots = setOf<Int>()

    init {
        scope.launch {
            cachedPrices = fetchJson<Map<String, Double>>("https://api.odtheking.com/lb/lowestbins").getOrElse { OdinMod.logger.error("Failed to fetch lowest bin prices for Croesus module.", it); emptyMap() }
        }

        on<GuiEvent.DrawTooltip> {
            val title = screen.title?.string ?: return@on
            if (croesusHud.enabled && (title.matches(chestNameRegex) || title.matches(chestPreviewScreenRegex))) {
                guiGraphics.pose().pushMatrix()
                val sf = mc.window.guiScale
                guiGraphics.pose().scale(1f / sf, 1f / sf)
                guiGraphics.pose().translate(croesusHud.x.toFloat(), croesusHud.y.toFloat())
                guiGraphics.pose().scale(croesusHud.scale)

                guiGraphics.drawOverlay(false)

                guiGraphics.pose().popMatrix()
            }
        }

        on<GuiEvent.DrawSlot> {
            if (screen.title?.string == "Croesus" && slot.item?.hoverName?.string.equalsOneOf("The Catacombs", "Master Mode The Catacombs")) {
                val lore = slot.item?.lore ?: return@on
                val loreString = slot.item?.loreString ?: return@on

                if (hideClaimed && loreString.any { it.matches(chestStatusRegex) } && (!includeKey || hasStrikeThrough("Dungeon Chest Key", lore))) cancel()
                else if (highlightState)
                    guiGraphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16,
                        if (loreString.any { it.matches(chestOpenedRegex) }) Colors.MINECRAFT_GOLD.rgba else Colors.MINECRAFT_GREEN.rgba)

            } else if (highlightProfitable && screen.title?.string?.matches(chestPreviewScreenRegex) == true && slot.index in mostProfitableSlots) {
                val color = when (mostProfitableSlots.indexOf(slot.index)) {
                    0 -> Colors.MINECRAFT_DARK_GREEN.rgba
                    1 -> Colors.MINECRAFT_YELLOW.rgba
                    else -> return@on
                }
                guiGraphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, color)
            }
        }

        on<GuiEvent.SlotUpdate> {
            val screenTitle = screen.title.string ?: return@on

            when {
                screenTitle.matches(chestNameRegex) -> handleChestContents(menu.items)
                screenTitle.matches(chestPreviewScreenRegex) -> handleCroesusScreen(menu.items)
            }
        }

        on<GuiEvent.Open> {
            mostProfitableSlots = emptySet()
            currentChestProfit = null
            chestData = emptyList()
        }

        onReceive<ClientboundPlayerInfoUpdatePacket> {
            if (actions().none { it.equalsOneOf(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER) }) return@onReceive
            val tabListEntries = entries()?.mapNotNull { it.displayName?.string }?.ifEmpty { return@onReceive } ?: return@onReceive
            tabListEntries.forEach { tabListEntry ->
                unclaimedChestsRegex.find(tabListEntry)?.groupValues?.get(1)?.toIntOrNull()?.let { unclaimedChests ->
                    currentChestCount = unclaimedChests
                    if (currentChestCount > chestWarning) alert("§cChest limit reached!")
                }
            }
        }

        on<ChatPacketEvent> {
            if (DungeonUtils.inBoss && value.matches(extraStatsRegex)) {
                currentChestCount++
                if (currentChestCount > chestWarning) alert("§cChest limit reached!")
            }
        }
    }

    private fun handleCroesusScreen(items: List<ItemStack>) {
        val chests = mutableListOf<ChestData>()

        items.forEachIndexed { index, stack ->
            if (stack.isEmpty || index > 16 || stack.item != Items.PLAYER_HEAD) return@forEachIndexed

            val lore = stack.loreString
            val loreStartIndex = lore.indexOfFirst { it == "Contents" } + 1
            if (loreStartIndex == 0) return@forEachIndexed

            val loreEndIndex = lore.indexOfFirst { it.isEmpty() }

            val chestCost = lore.getOrNull(loreEndIndex + 2)?.let { costLine ->
                chestCostRegex.find(costLine)?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull()
            } ?: 0.0

            val items = mutableListOf<ChestItem>()
            var totalValue = 0.0

            lore.subList(loreStartIndex, loreEndIndex).forEach { item ->
                val price = parseItemValue(item) ?: 0.0
                totalValue += price
                items.add(ChestItem(item, price))
            }

            chests.add(ChestData(stack.hoverName, items, totalValue - chestCost, index))
        }

        chestData = chests.sortedByDescending { it.profit }
        mostProfitableSlots = chestData.filter { it.profit > 0 }.take(2).map { it.slotIndex }.toSet()
    }

    private fun parseItemValue(item: String): Double? {
        previewEnchantedBookRegex.find(item)?.destructured?.let { (name, level) ->
            val ult = if (name in ultimateEnchants) "ULTIMATE_" else ""
            return cachedPrices["ENCHANTED_BOOK-$ult${name.uppercase().replace(" ", "_")}-${romanToInt(level)}"]
        }

        previewEssenceRegex.find(item)?.destructured?.let { (name, quantity) ->
            if (!includeEssence) return null
            val price = cachedPrices["ESSENCE_${name.uppercase()}"] ?: return null
            return price * quantity.toDouble()
        }

        shardRegex.find(item)?.groupValues?.get(1)?.let { shardName ->
            return cachedPrices["SHARD_${shardName.uppercase().replace(" ", "_")}"]
        }

        itemReplacements[item]?.let { itemId -> return cachedPrices[itemId] }

        return cachedPrices[item.uppercase().replace(" ", "_")]
    }

    private fun handleChestContents(items: List<ItemStack>) {
        val chestItems = mutableListOf<ChestItem>()
        var chestCost = 0.0
        var profit = 0.0

        items.forEachIndexed { index, stack ->
            if (stack.isEmpty || index > 40) return@forEachIndexed

            when (stack.item) {
                Items.CHEST -> {
                    stack.loreString.forEach { loreLine ->
                        chestCostRegex.find(loreLine)?.groupValues?.get(1)?.let { cost ->
                            chestCost = cost.replace(",", "").toDouble()
                        }
                    }
                }

                Items.ENCHANTED_BOOK -> {
                    chestEnchantsRegex.find(stack.customData.get("enchantments").toString())?.destructured?.let { (name, level) ->
                        cachedPrices["ENCHANTED_BOOK-${name.uppercase()}-$level"]?.let {
                            chestItems += ChestItem(stack.hoverName.string, it)
                            profit += it
                        }
                    }
                }
                else -> {
                    previewEssenceRegex.find(stack.hoverName.string)?.destructured?.let { (name, quantity) ->
                        if (!includeEssence) return@forEachIndexed
                        val price = cachedPrices["ESSENCE_${name.uppercase()}"] ?: return@forEachIndexed
                        chestItems += ChestItem(stack.hoverName.string, price * quantity.toDouble())
                        profit += price * quantity.toDouble()
                    } ?: previewShardRegex.find(stack.hoverName.string)?.destructured?.let { (shardName) ->
                        cachedPrices["SHARD_${shardName.uppercase().replace(" ", "_").replace("'s", "")}"]?.let {
                            chestItems += ChestItem(stack.hoverName.string, it)
                            profit += it
                        }
                    } ?: cachedPrices[stack.itemId]?.let {
                        chestItems += ChestItem(stack.hoverName.string, it)
                        profit += it
                    }
                }
            }
        }
        currentChestProfit = profit - chestCost
        chestData = listOf(
            ChestData(
                Component.literal("§eChest"),
                chestItems,
                currentChestProfit ?: 0.0,
                -1
            )
        )
    }

    private fun hasStrikeThrough(itemName: String, loreComponents: List<Component>): Boolean =
        loreComponents.any { line ->
            line.siblings.any { sibling ->
                sibling.string == itemName && sibling.style.isStrikethrough
            }
        }

    private fun GuiGraphics.drawOverlay(isEditing: Boolean): Pair<Int, Int> {
        val dataToDisplay = if (isEditing) sampleChestData else chestData
        var yOffset = 0
        var maxWidth = 0

        val maxNameWidth = getStringWidth("Diamond ")

        dataToDisplay.forEach { chest ->
            val profitColor = if (chest.profit >= 0) "§2" else "§c"
            val profitText = "§8- §6Profit: $profitColor${"%,.0f".format(chest.profit)}"

            text(chest.name.visualOrderText, 0, yOffset)
            val profitDim = textDim(profitText, maxNameWidth, yOffset)

            maxWidth = maxOf(maxWidth, maxNameWidth + profitDim.first)
            yOffset += 9

            if (!minimized) {
                chest.items.forEach { item ->
                    val itemText = "  §7${item.name}: §a${"%,.0f".format(item.price)}"
                    val dim = textDim(itemText, 0, yOffset)
                    maxWidth = maxOf(maxWidth, dim.first)
                    yOffset += dim.second
                }
            }

            yOffset += 2
        }

        return maxWidth to yOffset
    }

    private val itemReplacements = mapOf(
        "Shiny Wither Chestplate" to "WITHER_CHESTPLATE",
        "Shiny Wither Leggings" to "WITHER_LEGGINGS",
        "Shiny Necron's Handle" to "NECRON_HANDLE",
        "Necron's Handle" to "NECRON_HANDLE",
        "Shiny Wither Helmet" to "WITHER_HELMET",
        "Shiny Wither Boots" to "WITHER_BOOTS",
        "Wither Shield" to "WITHER_SHIELD_SCROLL",
        "Implosion" to "IMPLOSION_SCROLL",
        "Shadow Warp" to "SHADOW_WARP_SCROLL",
        "Necron Dye" to "DYE_NECRON",
        "Livid Dye" to "DYE_LIVID",
        "Giant's Sword" to "GIANTS_SWORD",
    )

    private val sampleChestData = listOf(
        ChestData(
            Component.literal("Bedrock"),
            listOf(
                ChestItem("Enchanted Book (One For All V)", 85000000.0),
                ChestItem("Wither Catalyst", 12000000.0),
                ChestItem("Necron's Handle", 550000000.0)
            ),
            632000000.0,
            0
        ),
        ChestData(
            Component.literal("Obsidian"),
            listOf(
                ChestItem("Enchanted Book (Legion V)", 45000000.0),
                ChestItem("Wither Blood", 8500000.0)
            ),
            46500000.0,
            1
        ),
        ChestData(
            Component.literal("Diamond"),
            listOf(
                ChestItem("Wither Essence x10", 2000000.0),
                ChestItem("Recombobulator 3000", 6500000.0)
            ),
            5000000.0,
            2
        )
    )
}