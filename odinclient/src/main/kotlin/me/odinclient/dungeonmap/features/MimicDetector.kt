package me.odinclient.dungeonmap.features

import me.odinclient.dungeonmap.core.map.Room
import me.odinclient.utils.skyblock.dungeon.map.ScanUtils.getRoomFromPos
import me.odinmain.OdinMain.mc
import net.minecraft.tileentity.TileEntityChest

object MimicDetector {
    fun findMimic(): String? {
        val mimicRoom = getMimicRoom()
        if (mimicRoom == "") return null
        Dungeon.Info.dungeonList.forEach {
            if (it is Room && it.data.name == mimicRoom) {
                it.hasMimic = true
            }
        }
        return mimicRoom
    }

    private fun getMimicRoom(): String {
        mc.theWorld.loadedTileEntityList.filter { it is TileEntityChest && it.chestType == 1 }
            .groupingBy { getRoomFromPos(it.pos)?.data?.name }.eachCount().forEach { (room, trappedChests) ->
                Dungeon.Info.uniqueRooms.find { it.data.name == room && it.data.trappedChests < trappedChests }
                    ?.let { return it.data.name }
            }
        return ""
    }
}