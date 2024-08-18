package me.odinmain.events.impl


import me.odinmain.utils.skyblock.dungeon.tiles.FullRoom
import net.minecraftforge.fml.common.eventhandler.Event

sealed class DungeonEvent : Event() {
    data class RoomEnterEvent(val fullRoom: FullRoom?) : DungeonEvent()
}