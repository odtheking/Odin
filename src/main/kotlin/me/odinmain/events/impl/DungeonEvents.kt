package me.odinmain.events.impl

import me.odinmain.utils.skyblock.dungeon.tiles.Room
import net.minecraftforge.fml.common.eventhandler.Event

data class RoomEnterEvent(val room: Room?) : Event()
