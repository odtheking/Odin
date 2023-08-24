package me.odinclient.features.impl.dungeon

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.events.impl.ItemOverlayEvent
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.render.world.OverlayUtils
import me.odinclient.utils.skyblock.ItemUtils.itemID
import me.odinclient.utils.skyblock.LocationUtils.inSkyblock
import me.odinclient.utils.skyblock.ScoreboardUtils.stripControlCodes
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object BonzoMask : Module(
    "Mask proc warning",
    description = "Displays a title when a Spirit Mask or Bonzo Mask triggers.",
    category = Category.DUNGEON
) {

    private var spiritMaskProc = 0L
    private var bonzoMaskProc = 0L
    private var fraggedBonzoMaskProc = 0L

    private const val secondWindString = "Second Wind Activated! Your Spirit Mask saved your life!"
    private const val bonzoString = "Your Bonzo's Mask saved your life!"
    private const val fraggedBonzoString = "Your ⚚ Bonzo's Mask saved your life!"

    @SubscribeEvent
    fun onChat(event: ClientChatReceivedEvent) {
        if (!inSkyblock) return
        val didMaskProc = when (event.message.unformattedText.stripControlCodes()) {
            secondWindString -> {
                spiritMaskProc = System.currentTimeMillis()
                true
            }
            bonzoString -> {
                bonzoMaskProc = System.currentTimeMillis()
                true
            }
            fraggedBonzoString -> {
                fraggedBonzoMaskProc = System.currentTimeMillis()
                true
            }
            else -> false
        }
        if (didMaskProc) {
            mc.ingameGUI.displayTitle("§cMask!", null, 5, 40, 5)
        }
    }

    @SubscribeEvent
    fun onRenderItemOverlay(event: ItemOverlayEvent) {
        if (!inSkyblock) return
        val durability = when (event.item?.itemID) {
            "BONZO_MASK" -> (System.currentTimeMillis() - bonzoMaskProc) / 180000.0
            "STARRED_BONZO_MASK" -> (System.currentTimeMillis() - fraggedBonzoMaskProc) / 180000.0
            "SPIRIT_MASK" -> (System.currentTimeMillis() - spiritMaskProc) / 30000.0
            else -> 1.0
        }
        if (durability < 1.0) {
            OverlayUtils.renderDurabilityBar(event.x, event.y, durability)
        }
    }


}