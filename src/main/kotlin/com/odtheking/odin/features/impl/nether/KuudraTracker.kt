package com.odtheking.odin.features.impl.nether

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mojang.serialization.JsonOps
import com.odtheking.odin.clickgui.settings.Saving
import com.odtheking.odin.clickgui.settings.Setting
import com.odtheking.odin.clickgui.settings.impl.ListSetting
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
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket
import net.minecraft.world.item.Items
import java.util.Optional

object KuudraTracker : Module(
    name = "Kuudra Tracker",
    description = "Tracks your Kuudra runs."
) {
    private val lineAmount by NumberSetting("Lines", 10, 0,40, 1, "The amount of items to display in the profit tracker.")

    private val chestRegex = Regex("(Free|Paid) Chest")
    private val hudRegex = Regex("^((Free|Paid) Chest)|(Kuudra - .+)|(.+)?Vesuvius$")
    private val uselessLinesRegex = Regex("^Contents|Cost|Click to open!|FREE|Already opened!|Can't open another chest!|Paid Chest|")
    private val amountRegex = Regex("""x(\d+)$""")

    val profitHud by HUD("Kuudra Tracker HUD", "") {
        if (!it) return@HUD 0 to 0
        drawOverlay(true)
    }

    private var singleItems by ComponentCountMapSetting("Single Items", ::updateDisplay)
    private var multiItems by ComponentCountMapSetting("Multi Items", ::updateDisplay)

    private var paid by NumberSetting("Paid Chests", 0, 0, Int.MAX_VALUE, 1, "").hide()
    private var free by NumberSetting("Free Chests", 0, 0,Int.MAX_VALUE, 1, "").hide()

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

                val (item, amount) = split(lore) ?: continue

                if (amount == 1) {
                    singleItems[item] = singleItems.getOrDefault(item, 0).coerceAtLeast(0) + amount
                    last.single.add(item)
                } else {
                    multiItems[item] = multiItems.getOrDefault(item, 0).coerceAtLeast(0) + amount
                    last.multi.add(Multi(item, amount))
                }
            }

            if (title == "Paid Chest") paid++
            if (title == "Free Chest") free++

            updateDisplay()
            ModuleManager.saveConfigurations()
        }
    }

    private fun split(line: Component): Pair<Component, Int>? {
        val result = Component.empty()

        val amount = amountRegex.find(line.string.trim())
            ?.groupValues
            ?.get(1)
            ?.toIntOrNull()
            ?: 1

        line.visit(
            { style, str ->
                if (style.color != TextColor.fromLegacyFormat(ChatFormatting.DARK_GRAY)) {
                    result.append(Component.literal(str).withStyle(style))
                }

                Optional.empty<Unit>()
            },
            Style.EMPTY
        )

        if (result.string.isBlank()) return null
        return result to amount
    }

    private fun updateDisplay() {
        toDisplay.clear()
        singleItems.forEach { (item, amount) ->
            if (amount == 0) return@forEach

            val new = Component.empty()
            new.append(item).append(Component.literal(" x$amount").withStyle(ChatFormatting.DARK_GRAY))

            val price = Vesuvius.parseItemValue(item)?.times(amount) ?: 0.0

            toDisplay.add(Pair(new, price))
        }
        multiItems.forEach { (item, amount) ->
            if (amount == 0) return@forEach

            val new = Component.empty()
            new.append(item).append(Component.literal("x$amount").withStyle(ChatFormatting.DARK_GRAY))

            val price = Vesuvius.parseItemValue(new) ?: 0.0

            toDisplay.add(Pair(new, price))
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
        var yOffset = 100
        val maxWidth = 300

        var profit = 0.0

        for ((index, pair) in toDisplay
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

        val keyPrices = "%,.0f".format(getKeyPrices())

        text(mc.font, "§cTotal Key Costs:", 0, yOffset, -1)
        text(mc.font, keyPrices, maxWidth - mc.font.width(keyPrices), yOffset, Colors.MINECRAFT_RED.rgba)

        yOffset += 12

        val profitStr = "%,.0f".format(profit - getKeyPrices())

        text("§aProfit:", 0, yOffset)
        text(profitStr, maxWidth - mc.font.width(profitStr), yOffset, Colors.MINECRAFT_GREEN)

        yOffset += 15

        val avg = "%,.0f".format((profit - getKeyPrices()) / (free + paid))

        text(mc.font, "Total Chests: Free [$free], Paid [$paid]", 0, yOffset, Colors.MINECRAFT_YELLOW.rgba)
        text(mc.font, "Avg [$avg]", maxWidth - mc.font.width("Avg [$avg]"), yOffset, Colors.MINECRAFT_GOLD.rgba)

        return maxWidth to yOffset
    }

    data class LastAdded(var single: MutableList<Component>, var multi: MutableList<Multi>, var key: Int)
    data class Multi(val component: Component, val amount: Int)

    private class ComponentCountMapSetting(
        name: String,
        private val afterLoad: () -> Unit
    ) : Setting<MutableMap<Component, Int>>(name), Saving {
        override val default = mutableMapOf<Component, Int>()
        override var value = default

        init {
            hidden = true
        }

        override fun write(gson: Gson): JsonElement = JsonArray().apply {
            value.forEach { (component, count) ->
                val encoded = ComponentSerialization.CODEC
                    .encodeStart(JsonOps.INSTANCE, component)
                    .result()
                    .orElse(null) ?: return@forEach

                add(JsonObject().apply {
                    add("component", encoded)
                    addProperty("count", count)
                })
            }
        }

        override fun read(element: JsonElement, gson: Gson) {
            value.clear()
            element.asJsonArray.forEach { entry ->
                val obj = entry.asJsonObject
                val component = ComponentSerialization.CODEC
                    .parse(JsonOps.INSTANCE, obj["component"])
                    .result()
                    .orElse(null) ?: return@forEach
                val count = obj["count"]?.asInt ?: return@forEach
                value[component] = count
            }
            afterLoad()
        }
    }
}
