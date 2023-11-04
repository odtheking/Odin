package me.odinclient.utils.skyblock.dungeon.map

import me.odinclient.dungeonmap.features.Dungeon
import me.odinclient.dungeonmap.features.DungeonScan
import me.odinmain.OdinMain.mc
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.skyblock.dungeon.Room
import net.minecraft.block.Block
import net.minecraft.util.BlockPos

object ScanUtils {
    fun getRoomFromPos(pos: BlockPos): Room? {
        val x = ((pos.x - DungeonScan.startX + 15) shr 5)
        val z = ((pos.z - DungeonScan.startZ + 15) shr 5)
        val room = Dungeon.Info.dungeonList.getOrNull(x * 2 + z * 22)
        return if (room is Room) room else null
    }

    fun getCore(x: Int, z: Int): Int {
        val blocks = arrayListOf<Int>()
        for (y in 140 downTo 12) {
            val id = Block.getIdFromBlock(mc.theWorld.getBlockState(BlockPos(x, y, z)).block)
            if (!id.equalsOneOf(5, 54)) {
                blocks.add(id)
            }
        }
        return blocks.joinToString("").hashCode()
    }
}