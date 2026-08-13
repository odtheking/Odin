package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.alert
import com.odtheking.odin.utils.itemId
import com.odtheking.odin.utils.playSoundAtPlayer
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.item.BowItem

object BowReleaseNotification : Module(
    name = "Bow Release Notification",
    description = "Notifies you when your standard bow (excluding shortbows) is fully drawn in dungeons."
) {
    private var notifiedForDraw = false

    // Terminator is the only bow that in it is ITEMID does not contain SHORTBOW and might in future updates more like this come
    private val ignoredBowIds = setOf(
        "TERMINATOR"
    )

    init {
        on<TickEvent.End> {
            val player = mc.player ?: run {
                notifiedForDraw = false
                return@on
            }
            
            val usedItem = player.useItem
            val itemId = usedItem.itemId

            val isShortbow = itemId.contains("SHORTBOW", ignoreCase = true) || itemId in ignoredBowIds
            val isStandardBow = usedItem.item is BowItem && !isShortbow

            val isUsingTargetBow = DungeonUtils.inDungeons &&
                player.isUsingItem &&
                isStandardBow

            if (!isUsingTargetBow) {
                notifiedForDraw = false
                return@on
            }

            val fullyDrawn = BowItem.getPowerForTime(player.ticksUsingItem) >= 1.0f
            if (fullyDrawn && !notifiedForDraw) {
                alert("§aREADY TO RELEASE!", playSound = false)
                playSoundAtPlayer(SoundEvents.EXPERIENCE_ORB_PICKUP)
                notifiedForDraw = true
            }
        }
    }

    override fun onDisable() {
        notifiedForDraw = false
        super.onDisable()
    }
}
