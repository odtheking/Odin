package me.odinclient.features.impl.skyblock

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.events.impl.HitBlockEvent
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.skyblock.Area
import me.odinclient.utils.skyblock.ChatUtils.modMessage
import me.odinclient.utils.skyblock.ItemUtils.enchants
import me.odinclient.utils.skyblock.ItemUtils.itemID
import me.odinclient.utils.skyblock.LocationUtils
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object BlockMissclick : Module(
    "Block crop missclicks",
    category = Category.SKYBLOCK
) {

    private val stems: Set<Block> = setOf(Blocks.melon_stem, Blocks.pumpkin_stem)
    private val crops: Set<Block> = setOf(Blocks.wheat, Blocks.carrots, Blocks.potatoes, Blocks.cocoa, Blocks.nether_wart)
    private val talls: Set<Block> = setOf(Blocks.cactus, Blocks.reeds)
    private val others: Set<Block> = setOf(Blocks.pumpkin, Blocks.melon_block, Blocks.brown_mushroom, Blocks.red_mushroom)

    private val whitelist = setOf(Items.melon_seeds, Items.pumpkin_seeds)

    private var lastHeldItemIndex = -1
    private var hasReplenish = false
    private var usingFarmingTool = false
    private var nextWarningTime = System.currentTimeMillis()

    private val farmingLocations = setOf(Area.PrivateIsland, Area.Garden)

    private val farmingItems = setOf(
        "ROOKIE_HOE",
        "WOOD_HOE",
        "STONE_HOE",
        "IRON_HOE",
        "GOLD_HOE",
        "DIAMOND_HOE",
        "THEORETICAL_HOE",
        "PUMPKIN_DICER",
        "COCO_CHOPPER",
        "MELON_DICER",
        "FUNGI_CUTTER",
        "CACTUS_KNIFE"
    )

    @SubscribeEvent
    fun onGuiOpen(event: GuiOpenEvent) {
        lastHeldItemIndex = -1
    }

    @SubscribeEvent
    fun onBlockHit(event: HitBlockEvent) {
        if (!farmingLocations.contains(LocationUtils.area)) return
        val hitBlock = mc.theWorld?.getBlockState(event.blockPos)?.block ?: return
        val currentItem = mc.thePlayer?.inventory?.getCurrentItem()
        if (currentItem == null || whitelist.contains(currentItem.item)) return
        if (lastHeldItemIndex != heldItemIndex) {
            hasReplenish = currentItem.enchants?.get("replenish") != null
            usingFarmingTool = farmingItems.any { currentItem.itemID.startsWith(it) ?: false }
            lastHeldItemIndex = heldItemIndex
        }
        when {
            stems.contains(hitBlock) -> cancelAndWarn(event, "Blocked breaking a stem!")
            crops.contains(hitBlock) -> {
                if (!hasReplenish) cancelAndWarn(event, "Blocked breaking a crop without Replenish!")
            }
            talls.contains(hitBlock) -> {
                if (mc.theWorld?.getBlockState(event.blockPos.down())?.block != hitBlock) {
                    cancelAndWarn(event, "Blocked breaking the bottom block of a tall crop!")
                }
            }
            usingFarmingTool -> event.isCanceled = !others.contains(hitBlock)
        }
    }

    private fun cancelAndWarn(event: HitBlockEvent, message: String) {
        if ((System.currentTimeMillis() - nextWarningTime) >= 0) {
            modMessage("§c${message}")
            mc.thePlayer?.playSound("random.pop", 1f, 0f)
            nextWarningTime = System.currentTimeMillis() + 500
        }
        event.isCanceled = true
    }

    private val heldItemIndex: Int
        get() = mc.thePlayer?.inventory?.currentItem ?: -1
}