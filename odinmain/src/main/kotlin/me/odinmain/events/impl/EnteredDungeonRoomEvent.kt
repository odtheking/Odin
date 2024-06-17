package me.odinmain.events.impl


import me.odinmain.utils.skyblock.dungeon.tiles.FullRoom
import net.minecraftforge.fml.common.eventhandler.Event

class EnteredDungeonRoomEvent(val room: FullRoom?) : Event()