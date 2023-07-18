package me.odinclient.dungeonmap.features

import cc.polyfrost.oneconfig.libs.universal.UChat
import me.odinclient.OdinClient.Companion.config
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.dungeonmap.core.map.*
import me.odinclient.utils.skyblock.ChatUtils
import me.odinclient.utils.skyblock.dungeon.map.ScanUtils.getCore
import me.odinclient.utils.skyblock.dungeon.map.ScanUtils.getRoomData
import me.odinclient.utils.skyblock.dungeon.map.ScanUtils.isColumnAir
import me.odinclient.utils.skyblock.dungeon.map.ScanUtils.isDoor
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos

object DungeonScan {
    fun scanDungeon() {
        Dungeon.reset()
        var allLoaded = true
        val startTime = if (config.nanoScanTime) System.nanoTime() else System.currentTimeMillis()

        scan@ for (x in 0..10) {
            for (z in 0..10) {
                val xPos = Dungeon.startX + x * (Dungeon.roomSize shr 1)
                val zPos = Dungeon.startZ + z * (Dungeon.roomSize shr 1)

                if (!mc.theWorld.getChunkFromChunkCoords(xPos shr 4, zPos shr 4).isLoaded) {
                    allLoaded = false
                    break@scan
                }
                if (isColumnAir(xPos, zPos)) continue

                getRoom(xPos, zPos, z, x)?.let {
                    if (it is Room && x and 1 == 0 && z and 1 == 0) Dungeon.rooms.add(it)
                    if (it is Door && it.type == DoorType.WITHER) Dungeon.doors[it] = Pair(x, z)
                    Dungeon.dungeonList[z * 11 + x] = it
                }
            }
        }

        if (allLoaded) {
            Dungeon.hasScanned = true
            MapUpdate.calibrate()

            if (config.scanChatInfo) {
                UChat.chat("""
                    ${ChatUtils.getChatBreak().dropLast(1)}
                    §3Odin§bClient §8» §6Scan Finished! It took §a${if (config.nanoScanTime) "${System.nanoTime() - startTime}ns" else "${System.currentTimeMillis() - startTime}ms"}
                    §9Puzzles (§c${Dungeon.puzzles.size}§9): §d${Dungeon.puzzles.joinToString("§7, §d")}
                    §6Trap: §d${Dungeon.trapType}
                    §8Wither Doors: §7${Dungeon.doors.size - 1}
                    §7Total Secrets: §b${Dungeon.secretCount}
                    ${ChatUtils.getChatBreak()}
                """.trimIndent())
            }
        } else Dungeon.reset()
    }

    private fun getRoom(x: Int, z: Int, row: Int, column: Int): Tile? {
        val rowEven = row and 1 == 0
        val columnEven = column and 1 == 0

        return when {
            rowEven && columnEven -> {
                val roomCore = getCore(x, z)
                Room(x, z, getRoomData(roomCore) ?: return null).apply {
                    core = roomCore
                    if (Dungeon.uniqueRooms.none { match -> match.data.name == data.name }) {
                        Dungeon.uniqueRooms.add(this)
                        Dungeon.secretCount += data.secrets
                        when (data.type) {
                            RoomType.TRAP -> Dungeon.trapType = data.name.split(" ")[0]
                            RoomType.PUZZLE -> Dungeon.puzzles.add(data.name)
                            else -> {}
                        }
                    }
                }
            }
            !rowEven && !columnEven -> {
                Dungeon.dungeonList[(row - 1) * 11 + column - 1].let {
                    if (it is Room) {
                        Room(x, z, it.data).apply { isSeparator = true }
                    } else null
                }
            }
            isDoor(x, z) -> {
                Door(x, z).apply {
                    val bState = mc.theWorld.getBlockState(BlockPos(x, 69, z))
                    type = when {
                        bState.block == Blocks.coal_block -> DoorType.WITHER
                        bState.block == Blocks.monster_egg -> DoorType.ENTRANCE
                        bState.block == Blocks.stained_hardened_clay && Blocks.stained_hardened_clay.getMetaFromState(
                            bState
                        ) == 14 -> DoorType.BLOOD
                        else -> DoorType.NORMAL
                    }
                }
            }
            else -> {
                Dungeon.dungeonList[if (rowEven) row * 11 + column - 1 else (row - 1) * 11 + column].let {
                    if (it is Room) {
                        if (it.data.type == RoomType.ENTRANCE) {
                            Door(x, z).apply { type = DoorType.ENTRANCE }
                        } else {
                            Room(x, z, it.data).apply { isSeparator = true }
                        }
                    } else null
                }
            }
        }
    }
}