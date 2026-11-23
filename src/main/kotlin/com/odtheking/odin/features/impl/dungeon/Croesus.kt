package com.odtheking.odin.features.impl.dungeon

import com.google.gson.JsonObject
import com.odtheking.odin.OdinMod
import com.odtheking.odin.OdinMod.scope
import com.odtheking.odin.clickgui.settings.RenderableSetting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.ActionSetting
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.clickgui.settings.impl.drawAtHud
import com.odtheking.odin.events.GuiEvent
import com.odtheking.odin.events.MessageEvent
import com.odtheking.odin.events.ScreenCloseEvent
import com.odtheking.odin.events.SetSlotEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.*
import com.odtheking.odin.utils.network.WebUtils.fetchJson
import com.odtheking.odin.utils.render.getStringWidth
import com.odtheking.odin.utils.render.roundedOutline
import com.odtheking.odin.utils.render.text
import com.odtheking.odin.utils.render.textDim
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import kotlinx.coroutines.launch
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

object Croesus :
    Module(
        name = "Croesus",
        description = "Enhances the Croesus chest menu with profit calculations and highlights.",
    ) {
  private val highlightState by
      BooleanSetting(
          "Highlight State",
          true,
          desc = "Highlights chests in the Croesus menu based on their claim status.",
      )
  private val highlightProfitable by
      BooleanSetting(
          "Highlight Profitable",
          true,
          desc = "Highlights the most and 2nd most profitable chests.",
      )
  private val hideClaimed by
      BooleanSetting("Hide Claimed", true, desc = "Hides chests that have already been claimed.")
  private val includeKey by
      BooleanSetting("Include Key", desc = "Count Dungeon Chest Key as unclaimed.").withDependency {
        hideClaimed
      }
  private val minimized by
      BooleanSetting(
          "Minimized",
          false,
          desc = "Only display profit for each chest instead of all items.",
      )

  private val croesusHud by
      HUD("Croesus Chest HUD", "Displays all chest contents with prices, sorted by profit.") {
        if (!it) return@HUD 0 to 0
        drawOverlay(true)
      }

  private val chestCount by
      HUD(
          "Chest Count HUD",
          "Displays the number of chests opened in the current Croesus session.",
      ) {
        if (DungeonUtils.inDungeons || it) textDim("§6Chests: §a${currentChestCount}", 0, 0)
        else 0 to 0
      }

  private val chestWarning by
      NumberSetting(
              "Chest Warning Threshold",
              55,
              0..60,
              desc =
                  "Displays a warning in the chest profit HUD if the profit is below this amount.",
          )
          .withDependency { chestCount.enabled }

  private val useBazaarInstantSell by
      BooleanSetting(
          "Bazaar Instant Sell",
          false,
          desc = "Use Bazaar instant-sell estimates for Bazaar items.",
      )

  private val refresh by
      ActionSetting(
          "Refresh Prices",
          desc = "Manually refresh the cached prices used for profit calculations.",
      ) {
        requestPriceRefresh(force = true)
      }

  var cachedPrices = emptyMap<String, Double>()

  private var instantSellPrices = emptyMap<String, Double>()
  private var bazaarUpdatedAt = 0L

  private val priceBaseUrl =
      System.getProperty("odin.priceBaseUrl")?.trimEnd('/') ?: "https://lb.odtheking.com"

  private const val REFRESH_INTERVAL_MS = 5 * 60_000L
  private const val MAX_BAZAAR_AGE_MS = 15 * 60_000L

  private var lastRefreshAttempt = 0L
  private var refreshInProgress = false

  private var priceRevision = 0
  private var lastRepricedRevision = -1
  private var lastRepricedMode = false
  private var lastRepricedFresh = false

  private var currentChestCount = 0

  private val chestNameRegex =
      Regex("^(Wood|Iron|Gold|Diamond|Emerald|Obsidian|Bedrock)(?: Chest)?$")
  private val croesusScreenRegex = Regex("^(?:\\(\\d+/\\d+\\) )?Croesus$")
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
  private val noMoreChestsRegex = Regex("^No more chests to open!$")

  private const val KISMET_FEATHER_LABEL = "Kismet Feather"

  private val ultimateEnchants =
      setOf(
          "Soul Eater",
          "Combo",
          "Legion",
          "One For All",
          "Rend",
          "Bank",
          "Swarm",
          "Last Stand",
          "Wisdom",
          "No Pain No Gain",
      )

  private data class ChestItem(val name: String, val price: Double)

  private data class ChestData(
      val name: Component,
      val items: List<ChestItem>,
      val profit: Double,
      val slotIndex: Int,
  )

  private var chestData = listOf<ChestData>()
  private var currentChestProfit: Double? = null
  private var mostProfitableSlots = setOf<Int>()

  private val chestSlotData = linkedMapOf<Int, ChestData>()
  private val chestItemContributions = linkedMapOf<Int, ChestItem>()
  private val chestCostContributions = linkedMapOf<Int, Double>()

  private fun parseInstantSellResponse(root: JsonObject): Pair<Long, Map<String, Double>>? {

    val updatedAt = runCatching { root.get("updatedAt")?.asLong }.getOrNull() ?: return null

    if (updatedAt <= 0L) return null

    val prices = runCatching { root.getAsJsonObject("instantSell") }.getOrNull() ?: return null

    val mapped =
        prices
            .entrySet()
            .mapNotNull { (id, value) ->
              val price = runCatching { value.asDouble }.getOrNull()

              if (price != null && price.isFinite() && price > 0.0) {
                id to price
              } else {
                null
              }
            }
            .toMap()

    return if (mapped.isEmpty()) null else updatedAt to mapped
  }

  private fun requestPriceRefresh(force: Boolean = false) {
    val now = System.currentTimeMillis()

    if (refreshInProgress) return

    if (!force && now - lastRefreshAttempt < REFRESH_INTERVAL_MS) {
      return
    }

    refreshInProgress = true
    lastRefreshAttempt = now

    scope.launch {
      val averages =
          fetchJson<Map<String, Double>>("$priceBaseUrl/averages/7day")
              .onFailure { OdinMod.logger.warn("Failed to refresh Croesus averages", it) }
              .getOrNull()
              ?.filterValues { it.isFinite() && it > 0.0 }
              ?.takeIf { it.isNotEmpty() }

      val bazaar =
          fetchJson<JsonObject>("$priceBaseUrl/bazaar/prices")
              .onFailure { OdinMod.logger.warn("Failed to refresh Bazaar instant prices", it) }
              .getOrNull()
              ?.let(::parseInstantSellResponse)

      mc.execute {
        if (averages != null) {
          cachedPrices = averages
        }

        if (bazaar != null) {
          bazaarUpdatedAt = bazaar.first
          instantSellPrices = bazaar.second
        }

        if (averages != null || bazaar != null) {
          priceRevision++
        }

        refreshInProgress = false

        if (force) {
          modMessage(
              if (averages != null || bazaar != null) {
                "Croesus prices refreshed."
              } else {
                "Failed to refresh Croesus prices."
              }
          )
        }
      }
    }
  }

  private fun bazaarFresh(): Boolean {
    if (bazaarUpdatedAt <= 0L) return false

    val age = System.currentTimeMillis() - bazaarUpdatedAt
    return age in 0L..MAX_BAZAAR_AGE_MS
  }

  private fun priceFor(id: String): Double? {
    if (useBazaarInstantSell && bazaarFresh()) {
      instantSellPrices[id]?.let {
        return it
      }
    }
    return cachedPrices[id]
  }

  private fun repriceVisibleScreen(title: String) {
    val slots = mc.player?.containerMenu?.slots ?: return

    when {
      title.matches(chestPreviewScreenRegex) -> {
        chestSlotData.clear()

        slots.take(17).forEachIndexed { index, slot -> handleCroesusScreen(index, slot.item) }
      }

      title.matches(chestNameRegex) -> {
        chestItemContributions.clear()
        chestCostContributions.clear()

        slots.take(41).forEachIndexed { index, slot -> handleChestContents(index, slot.item) }
      }
    }
  }

  init {
    requestPriceRefresh()

    on<GuiEvent.Render> {
      val title = screen.title.string

      if (!title.matches(chestNameRegex) && !title.matches(chestPreviewScreenRegex)) {
        return@on
      }

      requestPriceRefresh()

      val fresh = bazaarFresh()

      val needsRepricing =
          lastRepricedRevision != priceRevision ||
              lastRepricedMode != useBazaarInstantSell ||
              lastRepricedFresh != fresh

      if (needsRepricing) {
        repriceVisibleScreen(title)

        lastRepricedRevision = priceRevision
        lastRepricedMode = useBazaarInstantSell
        lastRepricedFresh = fresh
      }
    }

    on<GuiEvent.DrawTooltip> {
      val title = screen.title.string
      if (
          croesusHud.enabled &&
              (title.matches(chestNameRegex) || title.matches(chestPreviewScreenRegex))
      )
          guiGraphics.drawAtHud(croesusHud) { drawOverlay(false) }
    }

    on<GuiEvent.RenderSlot> {
      if (
          screen.title.string.matches(croesusScreenRegex) &&
              slot.item.hoverName.string.equalsOneOf("The Catacombs", "Master Mode The Catacombs")
      ) {
        val loreString = slot.item.loreString

        if (
            hideClaimed &&
                loreString.any { it.matches(chestStatusRegex) } &&
                (!includeKey || hasStrikeThrough("Dungeon Chest Key", slot.item.lore))
        )
            cancel()
        else {
          if (highlightState) {
            val color =
                when {
                  loreString.any { it.matches(noMoreChestsRegex) } -> Colors.MINECRAFT_RED.rgba
                  loreString.any { it.matches(chestOpenedRegex) } -> Colors.MINECRAFT_GOLD.rgba
                  else -> Colors.MINECRAFT_GREEN.rgba
                }
            guiGraphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, color)
          }

          if (hasStrikeThrough(KISMET_FEATHER_LABEL, slot.item.lore))
              guiGraphics.roundedOutline(
                  slot.x,
                  slot.y,
                  slot.x + 16,
                  slot.y + 16,
                  Colors.MINECRAFT_AQUA.rgba,
                  1f,
              )
        }
      } else if (
          highlightProfitable &&
              screen.title.string.matches(chestPreviewScreenRegex) &&
              slot.index in mostProfitableSlots
      ) {
        val color =
            when (mostProfitableSlots.indexOf(slot.index)) {
              0 -> Colors.MINECRAFT_DARK_GREEN.rgba
              1 -> Colors.MINECRAFT_YELLOW.rgba
              else -> return@on
            }
        guiGraphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, color)
      }
    }

    on<SetSlotEvent> {
      val screenTitle = mc.gui.screen()?.title?.string ?: return@on

      when {
        screenTitle.matches(chestNameRegex) -> handleChestContents(slotIndex, itemStack)
        screenTitle.matches(chestPreviewScreenRegex) -> handleCroesusScreen(slotIndex, itemStack)
      }
    }

    on<ScreenCloseEvent> {
      mostProfitableSlots = emptySet()
      currentChestProfit = null
      chestData = emptyList()
      chestSlotData.clear()
      chestItemContributions.clear()
      chestCostContributions.clear()
      lastRepricedRevision = -1
    }

    onReceive<ClientboundPlayerInfoUpdatePacket> {
      if (
          actions().none {
            it.equalsOneOf(
                ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME,
                ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER,
            )
          }
      )
          return@onReceive
      entries()
          .mapNotNull { it.displayName?.string }
          .ifEmpty {
            return@onReceive
          }
          .forEach { tabListEntry ->
            unclaimedChestsRegex.find(tabListEntry)?.groupValues?.get(1)?.toIntOrNull()?.let {
                unclaimedChests ->
              currentChestCount = unclaimedChests
              if (currentChestCount > chestWarning) alert("§cChest limit reached!")
            }
          }
    }

    on<MessageEvent.Chat> {
      if (DungeonUtils.inBoss && message.matches(extraStatsRegex)) {
        currentChestCount++
        if (currentChestCount > chestWarning) alert("§cChest limit reached!")
      }
    }
  }

  private fun handleCroesusScreen(index: Int, stack: ItemStack) {
    if (index > 16) return

    if (stack.isEmpty || stack.item != Items.PLAYER_HEAD) chestSlotData.remove(index)
    else {
      val lore = stack.loreString
      val loreStartIndex = lore.indexOfFirst { it == "Contents" } + 1

      if (loreStartIndex == 0) chestSlotData.remove(index)
      else {
        val loreEndIndex = lore.indexOfFirst { it.isEmpty() }

        val chestCost =
            lore.getOrNull(loreEndIndex + 2)?.let { costLine ->
              chestCostRegex.find(costLine)?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull()
            } ?: 0.0

        val items = mutableListOf<ChestItem>()
        var totalValue = 0.0

        lore.subList(loreStartIndex, loreEndIndex).forEach { item ->
          val price = parseItemValue(item) ?: 0.0
          totalValue += price
          items.add(ChestItem(item, price))
        }

        chestSlotData[index] = ChestData(stack.hoverName, items, totalValue - chestCost, index)
      }
    }

    chestData = chestSlotData.values.sortedByDescending { it.profit }
    mostProfitableSlots = chestData.filter { it.profit > 0 }.take(2).map { it.slotIndex }.toSet()
  }

  private fun parseItemValue(item: String): Double? {
    previewEnchantedBookRegex.find(item)?.destructured?.let { (name, level) ->
      val ult =
          if (name in ultimateEnchants) {
            "ULTIMATE_"
          } else {
            ""
          }
      val id =
          "ENCHANTED_BOOK-$ult${
            name.uppercase().replace(" ", "_")
          }-${romanToInt(level)}"

      return priceFor(id)
    }

    previewEssenceRegex.find(item)?.destructured?.let { (name, quantity) ->
      val price = priceFor("ESSENCE_${name.uppercase()}") ?: return null

      return price * quantity.toDouble()
    }

    shardRegex.find(item)?.groupValues?.get(1)?.let { shardName ->
      val id =
          "SHARD_${
              shardName.uppercase().replace(" ", "_")
            }"

      return priceFor(id)
    }

    itemReplacements[item]?.let { itemId ->
      return priceFor(itemId)
    }

    val id = item.uppercase().replace("'", "").replace(" -", "").replace(" ", "_")

    return priceFor(id)
  }

  private fun handleChestContents(index: Int, stack: ItemStack) {
    if (index > 40) return

    when {
      stack.isEmpty -> {
        chestItemContributions.remove(index)
        chestCostContributions.remove(index)
      }

      stack.item == Items.CHEST -> {
        chestItemContributions.remove(index)
        chestCostContributions.remove(index)
        stack.loreString.forEach { loreLine ->
          chestCostRegex.find(loreLine)?.groupValues?.get(1)?.let { cost ->
            chestCostContributions[index] = cost.replace(",", "").toDouble()
          }
        }
      }

      stack.item == Items.ENCHANTED_BOOK -> {
        chestCostContributions.remove(index)
        val contribution =
            chestEnchantsRegex
                .find(stack.customData.get("enchantments").toString())
                ?.destructured
                ?.let { (name, level) ->
                  priceFor("ENCHANTED_BOOK-${name.uppercase()}-$level")?.let {
                    ChestItem(stack.hoverName.string, it)
                  }
                }
        if (contribution != null) chestItemContributions[index] = contribution
        else chestItemContributions.remove(index)
      }

      else -> {
        chestCostContributions.remove(index)
        val contribution = resolveChestItemContribution(stack)
        if (contribution != null) chestItemContributions[index] = contribution
        else chestItemContributions.remove(index)
      }
    }

    val profit = chestItemContributions.values.sumOf { it.price }
    currentChestProfit = profit - chestCostContributions.values.sum()
    chestData =
        listOf(
            ChestData(
                Component.literal("§eChest"),
                chestItemContributions.values.toList(),
                currentChestProfit ?: 0.0,
                -1,
            )
        )
  }

  private fun resolveChestItemContribution(stack: ItemStack): ChestItem? {

    previewEssenceRegex.find(stack.hoverName.string)?.destructured?.let { (name, quantity) ->
      val price = priceFor("ESSENCE_${name.uppercase()}") ?: return null

      return ChestItem(stack.hoverName.string, price * quantity.toDouble())
    }

    previewShardRegex.find(stack.hoverName.string)?.destructured?.let { (shardName) ->
      val id =
          "SHARD_${
                    shardName.uppercase()
                        .replace(" ", "_")
                        .replace("'s", "")
                }"

      priceFor(id)?.let {
        return ChestItem(stack.hoverName.string, it)
      }
    }

    return priceFor(stack.itemId)?.let { ChestItem(stack.hoverName.string, it) }
  }

  private fun hasStrikeThrough(itemName: String, loreComponents: List<Component>): Boolean =
      loreComponents.any { line ->
        line.siblings.any { sibling -> sibling.string == itemName && sibling.style.isStrikethrough }
      }

  private fun GuiGraphicsExtractor.drawOverlay(isEditing: Boolean): Pair<Int, Int> {
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

  private val itemReplacements =
      mapOf(
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
          "NECROMANCERS_BROOCH" to "NECROMANCER_BROOCH",
          "SHADOW_WARP" to "SHADOW_WARP_SCROLL",
          "SPIRIT_STONE" to "SPIRIT_DECOY",
          "WARPED_STONE" to "AOTE_STONE",
      )

  private val sampleChestData =
      listOf(
          ChestData(
              Component.literal("Bedrock"),
              listOf(
                  ChestItem("Enchanted Book (One For All V)", 85000000.0),
                  ChestItem("Wither Catalyst", 12000000.0),
                  ChestItem("Necron's Handle", 550000000.0),
              ),
              632000000.0,
              0,
          ),
          ChestData(
              Component.literal("Obsidian"),
              listOf(
                  ChestItem("Enchanted Book (Legion V)", 45000000.0),
                  ChestItem("Wither Blood", 8500000.0),
              ),
              46500000.0,
              1,
          ),
          ChestData(
              Component.literal("Diamond"),
              listOf(
                  ChestItem("Wither Essence x10", 2000000.0),
                  ChestItem("Recombobulator 3000", 6500000.0),
              ),
              5000000.0,
              2,
          ),
      )
}
