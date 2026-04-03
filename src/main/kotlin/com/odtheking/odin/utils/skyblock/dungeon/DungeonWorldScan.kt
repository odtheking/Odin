package com.odtheking.odin.utils.skyblock.dungeon

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.LocationChangeEvent
import com.odtheking.odin.events.WorldEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.impl.dungeon.map.DungeonMapModule
import com.odtheking.odin.utils.IVec2
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.skyblock.dungeon.door.DoorRotation
import com.odtheking.odin.utils.skyblock.dungeon.door.DoorType
import com.odtheking.odin.utils.skyblock.dungeon.door.DungeonDoor
import com.odtheking.odin.utils.skyblock.dungeon.room.DungeonRoom
import com.odtheking.odin.utils.skyblock.dungeon.room.DungeonTile
import com.odtheking.odin.utils.skyblock.dungeon.room.RoomData
import com.odtheking.odin.utils.skyblock.dungeon.room.RoomRotation
import com.odtheking.odin.utils.toIVec2
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.chunk.LevelChunk
import java.util.concurrent.CopyOnWriteArrayList

object DungeonWorldScan {

    // move outside prob
    val tiles: Array<DungeonTile> = Array(36) { index ->
        DungeonTile(position = IVec2(x = index % 6, z = index / 6))
    }

    val rooms: CopyOnWriteArrayList<DungeonRoom> = CopyOnWriteArrayList()
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

        // maybe not use chunk load, but instead tick like every other map,
        // because if player decides to turn off and on world scanning
        // it will "break" because the chunk would need to be resent by server etc
        ClientChunkEvents.CHUNK_LOAD.register { _, chunk ->
            if (DungeonMapModule.disableWorldScan) return@register

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
                if (DungeonMapModule.disableWorldScan) return@on
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

        val rowEven = chunkPosition.x % 2 == 0
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
        doors[chunkPosition] = DungeonDoor(position = ((chunkPosition - 1) / 2) + 6, rotation, type)
    }

    private fun scanRoom(chunk: LevelChunk, chunkPosition: IVec2) {
        val (core, highestBlock) = getRoomCore(chunk, position = (chunkPosition * 16) + 7)
        val data = RoomData.getRoomData(core) ?: return

        val tilePosition = (chunkPosition / 2) + 6
        val tile = tiles.getOrNull(tilePosition.x + (tilePosition.z * 6)) ?: return

        (dataToRoom[data] as? DungeonRoom.WorldResolved)?.let { resolved ->
            tile.room = resolved
            if (!resolved.segments.contains(tile)) resolved.segments.add(tile)
            return
        }

        (tile.room as? DungeonRoom.MapResolved)?.let { mapRoom ->
            promoteMapResolved(mapRoom, data, chunk, chunkPosition, highestBlock)
            return
        }

        val collecting: DungeonRoom.Collecting = when (val tracked = dataToRoom[data]) {
            is DungeonRoom.Collecting -> {
                (tile.room as? DungeonRoom.MapResolved)?.let { mapRoom ->
                    promoteMapResolved(mapRoom, data, chunk, chunkPosition, highestBlock)
                    return
                }
                tracked
            }
            else -> DungeonRoom.Collecting().also { dataToRoom[data] = it }
        }

        tile.room = collecting
        if (!collecting.segments.contains(tile)) collecting.segments.add(tile)

        if (data.shape.segments != collecting.segments.size) return

        val worldResolved = DungeonRoom.WorldResolved(
            worldData = data,
            rotation  = inferRotation(chunk, chunkPosition, highestBlock, collecting.segments),
        ).also {
            it.checkmark = collecting.checkmark
            it.segments.addAll(collecting.segments)
        }
        for (s in worldResolved.segments) s.room = worldResolved
        rooms.add(worldResolved)
        dataToRoom[data] = worldResolved
    }

    private fun promoteMapResolved(
        mapRoom: DungeonRoom.MapResolved,
        data: RoomData,
        chunk: LevelChunk,
        chunkPosition: IVec2,
        highestBlock: Int
    ) {
        val worldResolved = DungeonRoom.WorldResolved(
            worldData = data,
            rotation  = inferRotation(chunk, chunkPosition, highestBlock, mapRoom.segments),
        ).also {
            it.checkmark = mapRoom.checkmark
            it.segments.addAll(mapRoom.segments)
        }

        for (s in worldResolved.segments) {
            s.room = worldResolved
        }
        rooms.remove(mapRoom)
        rooms.add(worldResolved)
        dataToRoom[data] = worldResolved
    }

    private fun inferRotation(chunk: LevelChunk, chunkPosition: IVec2, highestBlock: Int, segments: List<DungeonTile>): RoomRotation {
        val (_, rotation) = DungeonRoom.inferLayout(segments.map { it.position })
        if (rotation != null) return rotation

        val base = chunkPosition * 16
        return when {
            chunk.getBlockState(base.x + 14, highestBlock, base.z).block       === Blocks.BLUE_TERRACOTTA -> RoomRotation.WEST
            chunk.getBlockState(base.x + 14, highestBlock, base.z + 14).block  === Blocks.BLUE_TERRACOTTA -> RoomRotation.NORTH
            chunk.getBlockState(base.x,      highestBlock, base.z + 14).block  === Blocks.BLUE_TERRACOTTA -> RoomRotation.EAST
            else -> RoomRotation.SOUTH
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

    private fun LevelChunk.getBlockState(x: Int, y: Int, z: Int): BlockState {
        val sectionIndex = getSectionIndex(y)
        if (sectionIndex >= 0 && sectionIndex < sections.size) {
            val section = sections[sectionIndex]
            if (!section.hasOnlyAir()) return section.getBlockState(x and 15, y and 15, z and 15)
        }
        return Blocks.AIR.defaultBlockState()
    }
}