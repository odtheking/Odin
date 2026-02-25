package com.odtheking.odin.features.impl.nether

import com.odtheking.odin.clickgui.settings.impl.BooleanSetting

import com.odtheking.odin.events.GuiEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.features.Module
import com.odtheking.odin.features.impl.dungeon.Croesus.cachedPrices
import com.odtheking.odin.utils.*
import com.odtheking.odin.utils.render.text
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

object Vesuvius : Module(
    name = "Vesuvius",
    description = "Enhances the Vesuvius chest menu with profit calculations and highlights."
) {
    private val hideClaimed by BooleanSetting("Hide Claimed", true, desc = "Hides chests that have already been claimed.")

    private val vesuviusHud by HUD("Croesus Chest HUD", "Displays all chest contents with prices, sorted by profit.") {
        if (!it) return@HUD 0 to 0
        drawOverlay(true)
    }

    private val previewEnchantedBookRegex = Regex("^Enchanted Book \\(?([\\w ]+) (\\w+)\\)$")
    private val previewEssenceRegex = Regex("^(\\w+) Essence x(\\d+)$")
    private val shardRegex = Regex("^([A-Za-z' ]+) Shard(?: x(\\d+))?$")
    private val teethRegex = Regex("^Kuudra Teeth x(\\d+)$")
    private val pearlRegex = Regex("^Heavy Pearl x(\\d+)$")
    private val chestRegex = Regex("^((Free|Paid) Chest Chest)|(Kuudra - .+)$")
    private val uselessLinesRegex = Regex("^Contents|Cost|Click to open!|FREE|Already opened!|Can't open another chest!|Paid Chest|")

    private val ultimateEnchants = setOf(
        "Fatal Tempo", "Inferno"
    )

    private data class Key(val type: String, val coins: Int, val quantity: Int)
    private data class ChestItem(val name: Component, val price: Double)
    private data class ChestData(val items: List<ChestItem>, val cost: Double, val profit: Double)

    private var currentChest: ChestData? = null

    init {
        on<GuiEvent.DrawTooltip> {
            val title = screen.title?.string ?: return@on
            if (vesuviusHud.enabled && title.matches(chestRegex) && currentChest != null) {
                guiGraphics.pose().pushMatrix()
                val sf = mc.window.guiScale
                guiGraphics.pose().scale(1f / sf, 1f / sf)
                guiGraphics.pose().translate(vesuviusHud.x.toFloat(), vesuviusHud.y.toFloat())
                guiGraphics.pose().scale(vesuviusHud.scale)

                guiGraphics.drawOverlay(false)

                guiGraphics.pose().popMatrix()
            }
        }

        on<GuiEvent.DrawSlot> {
            if (screen.title?.string.equalsOneOf("Vesuvius", "Croesus") && slot.item?.hoverName?.string == "Kuudra's Hollow") {
                if (hideClaimed && slot.item?.loreString?.any { it == "No more chests to open!"} == true) cancel()
            }
        }

        onReceive<ClientboundContainerSetSlotPacket> {
            val title = mc.screen?.title?.string ?:return@onReceive
            if (slot == 31 && item.item == Items.CHEST && title.matches(chestRegex)) handleKuudraChest(item)
        }

        onReceive<ClientboundOpenScreenPacket> {
            currentChest = null
        }
    }

    private fun parseItemValue(item: String): Double? {
        previewEnchantedBookRegex.find(item)?.destructured?.let { (name, level) ->
            val ult = if (name in ultimateEnchants) "ULTIMATE_" else ""
            return cachedPrices["ENCHANTED_BOOK-$ult${name.uppercase().replace(" ", "_")}-${romanToInt(level)}"]
        }

        previewEssenceRegex.find(item)?.destructured?.let { (name, quantity) ->
            val price = cachedPrices["ESSENCE_${name.uppercase()}"] ?: return null
            return price * quantity.toDouble()
        }

        shardRegex.find(item)?.let { shard ->
            val price = cachedPrices["SHARD_${shard.groupValues[1].uppercase().replace(" ", "_")}"] ?: 0.0
            return price * (shard.groupValues.getOrNull(2)?.toDoubleOrNull() ?: 1.0)
        }

        teethRegex.find(item)?.destructured?.let { (quantity) ->
            val price = cachedPrices["KUUDRA_TEETH"] ?: 0.0
            return price * quantity.toDouble()
        }

        pearlRegex.find(item)?.destructured?.let { (quantity) ->
            val price = cachedPrices["HEAVY_PEARL"] ?: 0.0
            return price * quantity.toDouble()
        }

        itemReplacements[item]?.let { itemId -> return cachedPrices[itemId] }

        return cachedPrices[item.uppercase().replace(" ", "_")]
    }

    private fun getPriceOfKey(key: String): Double {
        keys.find { it.type == key }?.let {
            val material = minOf(cachedPrices["ENCHANTED_RED_SAND"] ?: 0.0, cachedPrices["ENCHANTED_MYCELIUM"] ?: 0.0)
            val star = (cachedPrices["CORRUPTED_NETHER_STAR"] ?: 0.0)

            return it.coins + material * it.quantity + star * 2
        }
        return 0.0
    }

    private fun handleKuudraChest(item: ItemStack) {
        val chestItems = mutableListOf<ChestItem>()
        var profit = 0.0
        var chestCost = 0.0

        val lore = item.lore

        lore.forEach { component ->
            val string = component.string

            if (string.contains("Kuudra Key")) {
                chestCost = getPriceOfKey(string)
                return@forEach
            }

            if (string.matches(uselessLinesRegex)) return@forEach

            val price = parseItemValue(string.replace("✪", "").trim()) ?: 0.0
                profit += price
                chestItems.add(ChestItem(component, price))
            }
        currentChest = ChestData(chestItems, chestCost, (profit - chestCost))
    }

    private fun GuiGraphics.drawOverlay(isEditing: Boolean): Pair<Int, Int> {
        val dataToDisplay = if (isEditing) sampleChestData else currentChest
        var yOffset = 0
        val maxWidth = 251

        val cost = "%,.0f".format(dataToDisplay?.cost)
        val profit = "%,.0f".format(dataToDisplay?.profit)

        dataToDisplay?.items?.forEach { item ->
            val price: String = "%,.0f".format(item.price)

            drawString(mc.font,item.name, 0, yOffset, -1)
            text(price, maxWidth - mc.font.width(price), yOffset, Colors.MINECRAFT_GRAY)

            yOffset += 9
        }

        yOffset += 6
        text("§cKey Cost:", 0, yOffset)
        text(cost, maxWidth - mc.font.width(cost), yOffset, Colors.MINECRAFT_RED)
        yOffset += 12
        text("§aProfit:", 0, yOffset)
        text(profit, maxWidth - mc.font.width(profit), yOffset, Colors.MINECRAFT_GREEN)
        yOffset += 9

        return maxWidth to yOffset
    }

    private val keys = listOf<Key>(
        Key("Kuudra Key", 155200, 2),
        Key("Hot Kuudra Key", 310400, 4),
        Key("Burning Kuudra Key", 582000, 16),
        Key("Fiery Kuudra Key", 1164000, 40),
        Key("Infernal Kuudra Key", 2328000, 80)
    )

    private val itemReplacements = mapOf(
        "Hellstorm Wand" to "HELLSTORM_STAFF",
        "Aurora Staff" to "RUNIC_STAFF",
    )

    private val sampleChestData = ChestData(
        items = listOf(
            ChestItem(
                Component.literal("Fervor Helmet")
                    .withStyle(ChatFormatting.GOLD),
                748000.0
            ),
            ChestItem(
                Component.literal("Enchanted Book (").withStyle(ChatFormatting.WHITE)
                    .append(Component.literal("Ferocious Mana V").withStyle(ChatFormatting.LIGHT_PURPLE))
                    .append(Component.literal(")").withStyle(ChatFormatting.WHITE)),
                118500000.0
            ),
            ChestItem(
                Component.literal("Crimson Essence").withStyle(ChatFormatting.LIGHT_PURPLE)
                        .append(Component.literal(" x2000").withStyle(ChatFormatting.DARK_GRAY)),
                2420000.0
            ),
            ChestItem(
                Component.literal("Kuudra Teeth").withStyle(ChatFormatting.DARK_PURPLE)
                    .append(Component.literal(" x4").withStyle(ChatFormatting.DARK_GRAY)),
                35480.0
            ),
            ChestItem(
                Component.literal("Kraken Shard").withStyle(ChatFormatting.GOLD)
                    .append(Component.literal(" x2").withStyle(ChatFormatting.DARK_GRAY)),
                821226.0
            )
        ),
        cost = 3247000.0,
        profit = 118542706.0
    )
}