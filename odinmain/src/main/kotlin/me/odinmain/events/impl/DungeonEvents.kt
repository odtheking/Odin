package me.odinmain.events.impl


import me.odinmain.utils.skyblock.dungeon.tiles.FullRoom
import net.minecraftforge.fml.common.eventhandler.Event

abstract class DungeonEvents : Event() {

    class RoomEnterEvent(val room: FullRoom?) : DungeonEvents()
}