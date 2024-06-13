package me.odinmain.events.impl

import me.odinmain.utils.skyblock.dungeon.SecretItem
import net.minecraftforge.fml.common.eventhandler.Event

open class SecretPickupEvent(val type: SecretItem) : Event() {
}