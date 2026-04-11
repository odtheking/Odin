package com.odtheking.odin.utils.skyblock.dungeon.map.scan

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.DungeonRoomEnterEvent
import com.odtheking.odin.events.LocationChangeEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.WorldEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.utils.IVec2
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.map.tile.*
import com.odtheking.odin.utils.toIVec2
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.LevelChunk
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

object DungeonWorldScan {

    // move outside prob
    val tiles: Array<DungeonTile> = Array(36) { index ->
        DungeonTile(position = IVec2(x = index % 6, z = index / 6))
    }

    val rooms: CopyOnWriteArrayList<DungeonRoom> = CopyOnWriteArrayList()
    val doors: ConcurrentHashMap<IVec2, DungeonDoor> = ConcurrentHashMap()
    val dataToRoom: HashMap<RoomData, DungeonRoom> = hashMapOf()

    var currentRoom: DungeonRoom? = null
        private set

    // keep track of chunks loaded before loading into a dungeon
    // if the area loaded into wasn't a dungeon. we simply discard it
    private val chunksToScan: HashSet<IVec2> = HashSet()

    init {
        on<WorldEvent.Load> {
            repeat(36) { index ->
                tiles[index] = DungeonTile(position = IVec2(x = index % 6, z = index / 6))
            }
            rooms.clear()
            doors.clear()
            dataToRoom.clear()
            chunksToScan.clear()
            currentRoom = null
        }

        on<TickEvent.End> {
            val player = mc.player ?: return@on

            if (!DungeonUtils.inDungeons || DungeonUtils.inBoss) {
                if (currentRoom != null) {
                    currentRoom = null
                    DungeonRoomEnterEvent(null).postAndCatch()
                }
                return@on
            }

            val tileX = (player.blockX + 201) shr 5
            val tileZ = (player.blockZ + 201) shr 5
            if (tileX !in 0..5 || tileZ !in 0..5) return@on

            val room = tiles[tileX + tileZ * 6].room
            if (room == currentRoom) return@on

            currentRoom = room
            room?.discovered = true
            DungeonRoomEnterEvent(room).postAndCatch()
        }

        ClientChunkEvents.CHUNK_LOAD.register { _, chunk ->
            // if dungeon isn't loaded, yet we add it to chunksToScan
            // otherwise just directly load it, no need to scan every tick

            if (!DungeonUtils.inDungeons) {
                chunksToScan.add(IVec2(chunk.pos.x, chunk.pos.z))
            } else {
                scanChunk(chunk)
            }
        }
        ClientChunkEvents.CHUNK_UNLOAD.register { _, chunk ->
            if (!DungeonUtils.inDungeons) {
                chunksToScan.remove(IVec2(chunk.pos.x, chunk.pos.z))
            }
        }
        // in case chunks are somehow unloaded before area is loaded
        // remove from chunksToScan, if dungeon has started no need to do this.

        on<LocationChangeEvent> {
            if (DungeonUtils.inDungeons) {
                val level = mc.level ?: return@on
                for (position in chunksToScan) {
                    scanChunk(level.getChunk(position.x, position.z))
                }
            }
            chunksToScan.clear()
        }
    }

    private fun scanChunk(chunk: LevelChunk) {
        val chunkPosition = chunk.pos.toIVec2()
        val rowEven    = chunkPosition.x % 2 == 0
        val columnEven = chunkPosition.z % 2 == 0

        if (chunkPosition.x in -12..-2 && chunkPosition.z in -12..-2) {
            if (rowEven && columnEven) scanRoom(chunk, chunkPosition)
            else if (rowEven || columnEven)
                scanDoor(chunk, chunkPosition, if (columnEven) DoorRotation.Horizontal else DoorRotation.Vertical)
        }
    }

    private fun scanDoor(chunk: LevelChunk, chunkPosition: IVec2, rotation: DoorRotation) {
        if (doors.contains(chunkPosition)) return
        val position = (chunkPosition * 16) + 7

        for (y in 86..160) {
            if (!chunk.getBlockState(position.x, y, position.z).isAir) return
        }
        if (chunk.getBlockState(position.x, 68, position.z).isAir) return

        val type = when (chunk.getBlockState(position.x, 69, position.z).block) {
            Blocks.COAL_BLOCK -> DoorType.Wither
            Blocks.RED_TERRACOTTA -> DoorType.Blood
            else -> DoorType.Normal
        }
        val doorPos = ((chunkPosition - 1) / 2) + 6
        val destPos = IVec2(doorPos.x + rotation.offset.x, doorPos.z + rotation.offset.z)
        val originIndex = doorPos.x + doorPos.z * 6
        val destIndex = (destPos.x + destPos.z * 6)
        doors[chunkPosition] = DungeonDoor(doorPos, rotation, type, originIndex, destIndex)
    }

    private fun scanRoom(chunk: LevelChunk, chunkPosition: IVec2) {
        val (core, highestBlock) = getRoomCore(chunk, (chunkPosition * 16) + 7)
        val data = RoomData.getRoomData(core) ?: return

        val tilePosition = (chunkPosition / 2) + 6
        val tile = tiles.getOrNull(tilePosition.x + (tilePosition.z * 6)) ?: return

        val room = when {
            tile.room?.data == data -> tile.room
            else -> dataToRoom.getOrPut(data) { DungeonRoom(data, tile.position).also { rooms.add(it) } }
        } ?: return

        tile.room = room
        room.addSegment(tile)

        if (room.segments.size != data.shape.segments) return

        val level = mc.level ?: return
        if (room.clayPos == null && highestBlock > 0) {
            // Layout probes can cross chunk borders; resolve blocks from the world, not this chunk.
            room.inferLayout({ pos -> level.getBlockState(pos).block }, highestBlock)
        }
    }

    private val stringBuilder = StringBuilder(1024)

    private fun getRoomCore(chunk: LevelChunk, position: IVec2): Pair<Int, Int> {
        stringBuilder.setLength(0)

        var foundHighest = false
        var highestBlock = 0
        var bedrock = 0

        for (y in 140 downTo 12) {
            val block = chunk.getBlockState(position.x, y, position.z).block

            if (!foundHighest) {
                if (block !== Blocks.AIR && block !== Blocks.GOLD_BLOCK) {
                    foundHighest = true
                    highestBlock = y
                } else stringBuilder.append('0')
            }

            if (foundHighest) {
                if (block === Blocks.AIR && bedrock >= 2 && y < 69) {
                    repeat(y - 11) { stringBuilder.append('0') }
                    break
                }
                if (block === Blocks.BEDROCK) bedrock++
                else {
                    bedrock = 0
                    if (block.equalsOneOf(Blocks.OAK_PLANKS, Blocks.TRAPPED_CHEST, Blocks.CHEST)) continue
                }
                stringBuilder.append(block)
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