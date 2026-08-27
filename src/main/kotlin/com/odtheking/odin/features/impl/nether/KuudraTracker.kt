package com.odtheking.odin.features.impl.nether

import com.odtheking.odin.clickgui.settings.impl.ActionSetting
import com.odtheking.odin.clickgui.settings.impl.ListSetting
import com.odtheking.odin.clickgui.settings.impl.MapSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.GuiEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onSend
import com.odtheking.odin.features.Module
import com.odtheking.odin.features.ModuleManager
import com.odtheking.odin.features.impl.nether.Vesuvius.getPriceOfKey
import com.odtheking.odin.features.impl.nether.Vesuvius.keys
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.lore
import com.odtheking.odin.utils.render.text
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket
import net.minecraft.world.item.Items
import kotlin.let

object KuudraTracker : Module(
    name = "Kuudra Tracker",
    description = "Tracks your Kuudra runs."
) {
    private val lineAmount by NumberSetting("Lines", 10, 0,40, 1, "The amount of items to display in the profit tracker.")
    private val reset by ActionSetting("Reset Profit", "Resets the Profit Tracker") {
        totalKeys = mutableListOf(0, 0, 0, 0, 0)
        singleItems.clear()
        multiItems.clear()
        paid = 0
        free = 0
        last = LastAdded(mutableListOf(), mutableListOf(), 0)

        updateDisplay()
        ModuleManager.saveConfigurations()
    }

    private val chestRegex = Regex("(Free|Paid) Chest")
    private val hudRegex = Regex("^((Free|Paid) Chest)|(Kuudra - .+)|(.+)?Vesuvius$")
    private val uselessLinesRegex = Regex("^Contents|Cost|Click to open!|FREE|Already opened!|Can't open another chest!|Paid Chest$")
    private val amountRegex = Regex("(.+)? x(\\d+)$")

    private val lightPurpleRegex = Regex("Crimson Essence")
    private val whiteRegex = Regex("Fatal Tempo|Inferno|Book")
    private val darkPurpleRegex = Regex("Kuudra Teeth|Fire Eel Shard|Kuudra Mandible|Barbarian Duke X Shard|XYZ Shard")
    private val blueRegex = Regex("Kada Knight Shard|Wheel of Fate|Ananke Feather|Burning Kuudra Core|Matcho Shard|Kuudra Tentacle|Lava Flame Shard|Wither Spectre Shard")
    private val goldRegex = Regex("Boots|Leggings|Chestplate|Helmet|Cloak|Aurora Staff|Hollow Wand|Heavy Pearl|Kraken Shard|Hellwisp Shard|Ananke Shard|Moltenfish Shard")

    val profitHud by HUD("Kuudra Tracker HUD", "Displays the profit of all your Kudura chests.") {
        if (!it) return@HUD 0 to 0
        drawOverlay(true)
    }

    private var singleItems by MapSetting("Single Items",mutableMapOf<String, Int>())
    private var multiItems by MapSetting("Multi Items", mutableMapOf<String, Int>())

    private var paid by NumberSetting("Paid Chests", 0, 0, Int.MAX_VALUE, 1, "Amount of Paid Chests opened.").hide()
    private var free by NumberSetting("Free Chests", 0, 0, Int.MAX_VALUE, 1, "Amount of Paid Chests opened").hide()

    private var toDisplay = mutableListOf<Pair<MutableComponent, Double>>()
    private var totalKeys by ListSetting("Total Keys", mutableListOf(0, 0, 0, 0, 0)).hide() //Tier 1-5

    private var last = LastAdded(mutableListOf(), mutableListOf(), 0)

    init {
        updateDisplay()

        on<GuiEvent.DrawTooltip> {
            val title = screen.title.string
            if (enabled && title.matches(hudRegex)) {
                guiGraphics.pose().pushMatrix()
                val sf = mc.window.guiScale
                guiGraphics.pose().scale(1f / sf, 1f / sf)
                guiGraphics.pose().translate(profitHud.x.toFloat(), profitHud.y.toFloat())
                guiGraphics.pose().scale(profitHud.scale)

                guiGraphics.drawOverlay(false)

                guiGraphics.pose().popMatrix()
            }
        }

        on<ChatPacketEvent> {
            val title = mc.screen?.title?.string ?: return@on
            if (value.equalsOneOf("You cannot afford this!", "Whoa! Slow down there!") && title.matches(chestRegex)) {
                for (single in last.single) {
                    singleItems[single] = (singleItems.getOrDefault(single, 0) - 1).coerceAtLeast(0)
                }
                for (multi in last.multi) {
                    multiItems[multi.component] = (multiItems.getOrDefault(multi.component, 0) - multi.amount).coerceAtLeast(0)
                }
                totalKeys[last.key - 1]--
                paid--

                updateDisplay()
                ModuleManager.saveConfigurations()
            }
        }

        onSend<ServerboundContainerClickPacket> {
            val title = mc.screen?.title?.string ?: return@onSend
            if (!title.matches(chestRegex)) return@onSend

            val cursorStack = mc.player?.containerMenu?.carried ?: return@onSend

            if (!cursorStack.`is`(Items.CHEST)) return@onSend

            val loreLines = cursorStack.lore

            updateDisplay()

            if (loreLines.any { it.string.equalsOneOf("Already opened!", "Can't open another chest!")}) return@onSend

            last = LastAdded(mutableListOf(), mutableListOf(), 0)

            for (lore in loreLines) {
                if (lore.string.matches(uselessLinesRegex)) continue

                if (lore.string.contains("Kuudra Key")) {
                    keys.find { it.type == lore.string }?.let { key ->
                        totalKeys[key.tier - 1]++
                        last.key = key.tier
                        continue
                    }
                }

                amountRegex.find(lore.string)?.destructured?.let { (name, amount) ->
                    multiItems[name] = multiItems.getOrDefault(name, 0) + (amount.toIntOrNull() ?: 0)
                    continue
                }

                singleItems[lore.string] = singleItems.getOrDefault(lore.string, 0) + 1
            }

            if (title == "Paid Chest") paid++
            if (title == "Free Chest") free++

            updateDisplay()
            ModuleManager.saveConfigurations()
        }
    }

    private fun updateDisplay() {
        toDisplay.clear()

        singleItems.forEach { (name, amount) ->
            if (amount == 0) return@forEach

            val price = Vesuvius.parseItemValue(Component.literal(name))?.times(amount) ?: return@forEach

            toDisplay.add(Component.literal(name).withStyle(getColor(name)).append(Component.literal(" x$amount").withStyle(ChatFormatting.DARK_GRAY)) to price)
        }

        multiItems.forEach { (name, amount) ->
            if (amount == 0) return@forEach

            val built = Component.literal(name).append(Component.literal(" x$amount").withStyle(ChatFormatting.DARK_GRAY))

            val price = Vesuvius.parseItemValue(built) ?: return@forEach

            toDisplay.add(built.withStyle(getColor(name)) to price)
        }
    }

    private fun getKeyPrices(): Double {
        return getPriceOfKey("Kuudra Key") * totalKeys[0] +
                getPriceOfKey("Hot Kuudra Key") * totalKeys[1] +
                getPriceOfKey("Burning Kuudra Key") * totalKeys[2] +
                getPriceOfKey("Fiery Kuudra Key") * totalKeys[3] +
                getPriceOfKey("Infernal Kuudra Key") * totalKeys[4]
    }

    private fun GuiGraphicsExtractor.drawOverlay(isEditing: Boolean): Pair<Int, Int> {
        var yOffset = 0
        val maxWidth = 300

        var profit = 0.0

        val display = if (isEditing) { sampleProfitData } else toDisplay

        for ((index, pair) in display
            .sortedByDescending { it.second }
            .withIndex()
        ) {
            val price = "%,.0f".format(pair.second)

            profit += pair.second

            if (index >= lineAmount) continue

            text(mc.font, pair.first, 0, yOffset, -1)
            text(mc.font, price, maxWidth - mc.font.width(price), yOffset, Colors.MINECRAFT_DARK_GRAY.rgba)

            yOffset += 9
        }

        yOffset += 6

        val keyPrices = if (isEditing) getPriceOfKey("Infernal Kuudra Key").toString() else "%,.0f".format(getKeyPrices())

        text(mc.font, "§cTotal Key Costs:", 0, yOffset, -1)
        text(mc.font, keyPrices, maxWidth - mc.font.width(keyPrices), yOffset, Colors.MINECRAFT_RED.rgba)

        yOffset += 12

        val profitStr = "%,.0f".format(profit - getKeyPrices())

        text("§aProfit:", 0, yOffset)
        text(profitStr, maxWidth - mc.font.width(profitStr), yOffset, Colors.MINECRAFT_GREEN)

        yOffset += 15

        val freeChests = if (isEditing) 0 else free
        val paidChests = if (isEditing) 1 else paid

        val avg = "%,.0f".format((profit - getKeyPrices()) / (freeChests + paidChests))

        text(mc.font, "Total Chests: Free [$freeChests], Paid [$paidChests]", 0, yOffset, Colors.MINECRAFT_YELLOW.rgba)
        text(mc.font, "Avg [$avg]", maxWidth - mc.font.width("Avg [$avg]"), yOffset, Colors.MINECRAFT_GOLD.rgba)

        yOffset += 9

        return maxWidth to yOffset
    }

    private fun getColor(name: String): ChatFormatting {
        if (name.contains(whiteRegex)) return ChatFormatting.WHITE
        if (name.contains(goldRegex)) return ChatFormatting.GOLD
        if (name.contains(darkPurpleRegex)) return ChatFormatting.DARK_PURPLE
        if (name.contains(lightPurpleRegex)) return ChatFormatting.LIGHT_PURPLE
        if (name.contains(blueRegex)) return ChatFormatting.BLUE

        return ChatFormatting.WHITE
    }

    data class LastAdded(var single: MutableList<String>, var multi: MutableList<Multi>, var key: Int)
    data class Multi(val component: String, val amount: Int)

    private val sampleProfitData = listOf(
        Pair(
            Component.literal("Fervor Helmet")
                .withStyle(ChatFormatting.GOLD),
            748000.0
        ),
        Pair(
            Component.literal("Enchanted Book (").withStyle(ChatFormatting.WHITE)
                .append(Component.literal("Ferocious Mana V").withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(Component.literal(")").withStyle(ChatFormatting.WHITE)),
            118500000.0
        ),
        Pair(
            Component.literal("Crimson Essence").withStyle(ChatFormatting.LIGHT_PURPLE)
                .append(Component.literal(" x2000").withStyle(ChatFormatting.DARK_GRAY)),
            2420000.0
        ),
        Pair(
            Component.literal("Kuudra Teeth").withStyle(ChatFormatting.DARK_PURPLE)
                .append(Component.literal(" x4").withStyle(ChatFormatting.DARK_GRAY)),
            35480.0
        ),
        Pair(
            Component.literal("Kraken Shard").withStyle(ChatFormatting.GOLD)
                .append(Component.literal(" x2").withStyle(ChatFormatting.DARK_GRAY)),
            821226.0
        )
    )
}
