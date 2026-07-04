package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.impl.SelectorSetting
import com.odtheking.odin.events.CheckmarkUpdateEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.features.impl.dungeon.map.tile.MapCheckmark
import com.odtheking.odin.features.impl.dungeon.map.tile.RoomType
import com.odtheking.odin.utils.alert
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils

object RoomClear : Module(
    name = "Room Clear",
    description = "Notifies you when a room is cleared in dungeons."
) {
    private val mode by SelectorSetting("Mode", "Both", listOf("Both", "Green", "White"), desc = "Notification for when a room turns green or white.")

    init {
        on<CheckmarkUpdateEvent> {
            if (DungeonUtils.currentRoom != room || !checkmark.equalsOneOf(MapCheckmark.GREEN, MapCheckmark.WHITE)) return@on
            if (room.type == RoomType.FAIRY || room.type == RoomType.ENTRANCE) return@on

            if (mode == 0 || (mode == 1 && checkmark == MapCheckmark.GREEN) || (mode == 2 && checkmark == MapCheckmark.WHITE))
                alert(if (checkmark == MapCheckmark.GREEN) "§aRoom Complete!" else "Room Cleared!")
        }
    }
}