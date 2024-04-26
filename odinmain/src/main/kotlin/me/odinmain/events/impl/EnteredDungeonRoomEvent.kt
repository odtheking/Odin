package me.odinmain.events.impl


import me.odinmain.utils.skyblock.dungeon.DungeonUtils.FullRoom
import net.minecraftforge.fml.common.eventhandler.Event

class EnteredDungeonRoomEvent(val room: FullRoom?) : Event()