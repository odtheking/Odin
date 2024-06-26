package me.odinmain.events.impl


import me.odinmain.utils.skyblock.dungeon.Floor
import me.odinmain.utils.skyblock.dungeon.tiles.FullRoom
import net.minecraftforge.fml.common.eventhandler.Event

abstract class DungeonEvents : Event() {

    class RoomEnterEvent(val room: FullRoom?) : DungeonEvents()

    class DungeonStartEvent(val floor: Floor) : DungeonEvents()

    class DungeonEndEvent(val floor: Floor) : DungeonEvents()

}