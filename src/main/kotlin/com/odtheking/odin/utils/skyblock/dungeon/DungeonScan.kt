package com.odtheking.odin.utils.skyblock.dungeon

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.LocationChangeEvent
import com.odtheking.odin.events.WorldEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.utils.IVec2
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.skyblock.dungeon.door.DoorRotation
import com.odtheking.odin.utils.skyblock.dungeon.door.DoorType
import com.odtheking.odin.utils.skyblock.dungeon.door.DungeonDoor
import com.odtheking.odin.utils.skyblock.dungeon.room.DungeonRoom
import com.odtheking.odin.utils.skyblock.dungeon.room.DungeonTile
import com.odtheking.odin.utils.skyblock.dungeon.room.RoomData
import com.odtheking.odin.utils.toIVec2
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.LevelChunk

/*
pseudocode
Dungeon Scan

WORLD SCAN:

    When a chunk is loaded and in dungeon:
        If the chunk corresponds to a room core (x % 2 == 0, z % 2 == 0)
        Scan the room core.
        Match room core with room data.
        Get or put room (with data) into tile.
        (Either check neighbouring tiles if same room, or hashmap for room data to room)
        (If room exists in that tile but has no data. Add reference in hashmap for core to room)
        If room has enough segments, attempt to find a shape for the room

        Or if the chunk corresponds to a door.
        Scan if there is a door (add its type if wither/blood). (Make sure it isn't a part of a room)
        Get its position
        Then put it in an array of other doors (or hashmap with position as key),

MAP SCAN:

    When receiving map data:
        Check if it corresponds to:

        - Room open:
        Skip if it's an unknown/gray room
        Get the shape of the loaded room,
        If its chunks haven't been loaded for world scan
        Put the reference to the room into all tiles it covers

        - Room status change: (cleared, fully cleared)
        Get the tile it changed on (Shouldn't be possible for room to be uninitialized)
        Update the status based on what changed

        - Door load:
        If door doesn't exist, add it based on its position
        then add type,
        if door already exists only replace type IF previous type was wither door and new type is fairy

MAP RENDER:

    Render all the rooms.
    Render all the doors:
        If one of the tiles is discovered and other is not discovered,
        Render the undiscovered tile, and use that color as the color,
        If the tile can be guessed based on what's discovered (e.g. can't move further, meaning puzzle/trap/yellow/rare room)
        Use a darkened gradient of the colors for those rooms. (Might need shader for good-looking gradient)
*/

object DungeonScan {

    val tiles: Array<DungeonTile> = Array(36) { index ->
        DungeonTile(position = IVec2(x = index % 6, z = index / 6))
    }

    val rooms: ArrayList<DungeonRoom> = arrayListOf()
    val doors: HashMap<IVec2, DungeonDoor> = hashMapOf()
    val dataToRoom: HashMap<RoomData, DungeonRoom> = hashMapOf()

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
        }

        ClientChunkEvents.CHUNK_LOAD.register { _, chunk ->

            // if dungeon isn't loaded, yet we add it to chunksToScan
            // otherwise just directly load it, no need to scan every tick

            if (!DungeonUtils.inDungeons) chunksToScan.add(IVec2(chunk.pos.x, chunk.pos.z))
            else scanChunk(chunk)
        }
        ClientChunkEvents.CHUNK_UNLOAD.register { _, chunk ->
            if (!DungeonUtils.inDungeons) chunksToScan.remove(IVec2(chunk.pos.x, chunk.pos.z))
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
        // assume it is dungeon since it should be based on checks above
        val chunkPosition = chunk.pos.toIVec2()

        val rowEven = chunkPosition.x % 2 == 0
        val columnEven = chunkPosition.z % 2 == 0

        if (chunkPosition.x in -12..-2 && chunkPosition.z in -12..-2) {
            if (rowEven && columnEven) scanRoom(chunk, chunkPosition)
            if (rowEven || columnEven)
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
        doors[chunkPosition] = DungeonDoor(position = ((chunkPosition - 1) / 2) + 6, rotation, type)
    }

    private fun scanRoom(chunk: LevelChunk, chunkPosition: IVec2) {
        val (core, highestBlock) = getRoomCore(chunk, position = (chunkPosition * 16) + 7)
        val data = RoomData.getRoomData(core) ?: return

        val tilePosition = (chunkPosition / 2) + 6
        val tile = tiles.getOrNull(tilePosition.x + (tilePosition.z * 6)) ?: return

        // if room exists in tile, add roomData to the room
        if (tile.room != null) {
            //
        } else {
            val room = dataToRoom.getOrPut(data) { DungeonRoom(data) }
            tile.room = room
            room.segments.add(tile)

            if (room.hasAllSegments()) room.setShapeAndRotation(chunk, chunkPosition, highestBlock)
        }
    }

    // re-usable for getRoomCore
    private val stringBuilder = StringBuilder(1024)

    private fun getRoomCore(chunk: LevelChunk, position: IVec2): IVec2 {
        stringBuilder.setLength(0)

        // compatible with current format

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
                    repeat(y - 11) {
                        stringBuilder.append('0')
                    }
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

        return IVec2(stringBuilder.toString().hashCode(), highestBlock)
    }

    fun LevelChunk.getBlockState(x: Int, y: Int, z: Int): BlockState {
        val sectionIndex = getSectionIndex(y)
        if (sectionIndex >= 0 && sectionIndex < sections.size) {
            val section = sections[sectionIndex]
            if (!section.hasOnlyAir()) return section.getBlockState(x and 15, y and 15, z and 15)
        }
        return Blocks.AIR.defaultBlockState()
    }
}