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

object DeathBowNotification : Module(
    name = "DeathBow Notification",
    description = "Notifies you when your Death Bow is fully drawn in dungeons."
) {
    private var notifiedForDraw = false

    init {
        on<TickEvent.End> {
            val player = mc.player ?: run {
                notifiedForDraw = false
                return@on
            }
            val usedItem = player.useItem
            val isUsingDeathBow = DungeonUtils.inDungeons &&
                player.isUsingItem &&
                usedItem.item is BowItem &&
                usedItem.itemId == "DEATH_BOW"

            if (!isUsingDeathBow) {
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