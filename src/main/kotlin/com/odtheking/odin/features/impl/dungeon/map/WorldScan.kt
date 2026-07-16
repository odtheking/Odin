package com.odtheking.odin.features.impl.dungeon.map

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.LocationChangeEvent
import com.odtheking.odin.events.RoomEnterEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.impl.dungeon.map.tile.*
import com.odtheking.odin.utils.*
import com.odtheking.odin.utils.skyblock.Island
import com.odtheking.odin.utils.skyblock.LocationUtils
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.LevelChunk

object WorldScan {

    var currentRoom: DungeonRoom? = null
        private set

    private val dataToRoom = HashMap<RoomData, DungeonRoom>()
    private val chunksToScan = HashSet<IVec2>()

    init {
        on<LevelEvent.Load> {
            dataToRoom.clear()
            chunksToScan.clear()
            currentRoom = null
        }

        on<TickEvent.End> {
            val player = mc.player ?: return@on

            if (!DungeonUtils.inDungeons || DungeonUtils.inBoss) {
                if (currentRoom != null) {
                    currentRoom = null
                    RoomEnterEvent(null).postAndCatch()
                }
                return@on
            }

            for (room in DungeonScan.rooms) {
                if (room.shape != RoomShape.OneByOne) continue
                if (room.rotation != null && room.clayPos != null) continue
                if (room.highestBlock != null) room.get1x1Rotation()
            }

            val tileX = (player.blockX + 201) shr 5
            val tileZ = (player.blockZ + 201) shr 5
            if (tileX !in 0..5 || tileZ !in 0..5) return@on

            DungeonScan.tiles[tileX + tileZ * 6].room?.let { room ->
                if (room == currentRoom || room.rotation == null || room.highestBlock == null) return@on
                if (player.blockY > room.highestBlock!! - 10 && !LocationUtils.isCurrentArea(Island.SinglePlayer)) return@on

                currentRoom = room
                room.walkedInto = true
                RoomEnterEvent(room).postAndCatch()
                devMessage("${room.data?.name ?: room.type} - ${room.rotation} || clay: ${room.clayPos}")
            }
        }

        ClientChunkEvents.CHUNK_LOAD.register { _, chunk ->
            if (!DungeonUtils.inDungeons) chunksToScan.add(IVec2(chunk.pos.x, chunk.pos.z))
            else scanChunk(chunk)
        }
        ClientChunkEvents.CHUNK_UNLOAD.register { _, chunk ->
            if (!DungeonUtils.inDungeons) chunksToScan.remove(IVec2(chunk.pos.x, chunk.pos.z))
        }

        on<LocationChangeEvent> {
            if (DungeonUtils.inDungeons) {
                val level = mc.level ?: return@on
                for (position in chunksToScan) scanChunk(level.getChunk(position.x, position.z))
            }
            chunksToScan.clear()
        }
    }

    private fun scanChunk(chunk: LevelChunk) {
        val chunkPosition = chunk.pos.toIVec2()
        val columnEven = chunkPosition.z % 2 == 0
        val rowEven = chunkPosition.x % 2 == 0

        if (chunkPosition.x in -12..-2 && chunkPosition.z in -12..-2) {
            if (rowEven && columnEven) {
                val tilePos = (chunkPosition / 2) + 6
                val tile = DungeonScan.tiles.getOrNull(tilePos.x + tilePos.z * 6)
                if (tile?.room?.data == null) scanRoom(chunk, chunkPosition)
            }
        }
    }

    private fun scanRoom(chunk: LevelChunk, chunkPosition: IVec2) {
        val (core, highestBlock) = getRoomCore(chunk, (chunkPosition * 16) + 7)
        val data = RoomData.getRoomData(core) ?: run {
            if (core == -318865360) return
            else return devMessage("Unknown room data for core: $core $chunkPosition")
        }

        val tilePosition = (chunkPosition / 2) + 6
        val tile = DungeonScan.tiles.getOrNull(tilePosition.x + (tilePosition.z * 6)) ?: return

        val room = dataToRoom.getOrPut(data) {
            val existingMapRoom = tile.room
            if (existingMapRoom != null) {
                existingMapRoom.data = data
                existingMapRoom.type = data.type
                existingMapRoom
            } else DungeonRoom(data.type, tile.position, data).also { DungeonScan.rooms.add(it) }
        }

        if (tile.room !== room) {
            room.highestBlock = highestBlock
            tile.room = room
            room.addSegment(tile)
        }

        if (room.data == null) {
            room.data = data
            room.type = data.type
        }
    }

    private val stringBuilder = StringBuilder(1024)

    fun getRoomCore(chunk: LevelChunk, position: IVec2): Pair<Int, Int> {
        stringBuilder.setLength(0)

        var foundHighest = false
        var highestBlock = 0
        var bedrock = 0

        for (y in 140 downTo 12) {
            val blockState = chunk.getBlockState(position.x, y, position.z)

            if (!foundHighest) {
                if (!blockState.isAir && blockState.block !== Blocks.GOLD_BLOCK) {
                    foundHighest = true
                    highestBlock = y
                } else stringBuilder.append('0')
            }

            if (foundHighest) {
                if (blockState.isAir && bedrock >= 2 && y < 69) {
                    repeat(y - 11) { stringBuilder.append('0') }
                    break
                }
                if (blockState.block === Blocks.BEDROCK) bedrock++
                else {
                    bedrock = 0
                    if (blockState.block.equalsOneOf(Blocks.OAK_PLANKS, Blocks.TRAPPED_CHEST, Blocks.CHEST)) continue
                }
                stringBuilder.append(blockState.block)
            }
        }

        return stringBuilder.toString().hashCode() to highestBlock
    }

    private fun LevelChunk.getBlockState(x: Int, y: Int, z: Int): BlockState {
        val sectionIndex = getSectionIndex(y)
        if (sectionIndex >= 0 && sectionIndex < sections.size) {
            val section = sections[sectionIndex]
            if (!section.hasOnlyAir()) return section.getBlockState(x and 15, y and 15, z and 15)
        }
        return Blocks.AIR.defaultBlockState()
    }
}