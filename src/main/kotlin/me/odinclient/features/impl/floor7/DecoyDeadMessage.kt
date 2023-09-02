package me.odinclient.features.impl.floor7

import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.utils.clock.Clock
import me.odinclient.utils.skyblock.ChatUtils
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object DecoyDeadMessage : Module(
    name = "Decoy Dead Message",
    description = "Sends a message in party chat when a decoy dies",
    category = Category.FLOOR7,
    tag = TagType.NEW
) {
    private var lastSentMessage = Clock(500)

    @SubscribeEvent
    fun onLivingDeath(event: LivingDeathEvent) {
        if (event.entity.name != "Decoy " || event.entity.isDead || !lastSentMessage.hasTimePassed()) return
        ChatUtils.partyMessage("Decoy killed.")
        lastSentMessage.update()
    }
}