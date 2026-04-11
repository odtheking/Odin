package com.odtheking.odin.utils.skyblock.dungeon

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.events.DungeonRoomEnterEvent
import com.odtheking.odin.events.RoomEnterEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.WorldEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.utils.JsonResourceLoader
import com.odtheking.odin.utils.Vec2
import com.odtheking.odin.utils.devMessage
import com.odtheking.odin.utils.equalsOneOf
import com.odtheking.odin.utils.skyblock.Island
import com.odtheking.odin.utils.skyblock.LocationUtils
import com.odtheking.odin.utils.skyblock.dungeon.tiles.Room
import com.odtheking.odin.utils.skyblock.dungeon.tiles.RoomComponent
import com.odtheking.odin.utils.skyblock.dungeon.tiles.RoomData
import com.odtheking.odin.utils.skyblock.dungeon.tiles.Rotations
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.chunk.LevelChunk


// get rid of all the old scanning

object ScanUtils {
    private const val ROOM_SIZE_SHIFT = 5  // Since ROOM_SIZE = 32 (2^5) so we can perform bitwise operations
    private const val START = -185

    val roomList: Set<RoomData> = JsonResourceLoader.loadJson("/assets/odin/rooms.json", setOf())
    val coreToRoomData: Map<Int, RoomData> =
        roomList.flatMap { room -> room.cores.map { core -> core to room } }.toMap()

    private val horizontals = Direction.entries.filter { it.axis.isHorizontal }
    private val mutableBlockPos = BlockPos.MutableBlockPos()
    private var lastRoomPos: Vec2 = Vec2(0, 0)

    var currentRoom: Room? = null
        private set
    var passedRooms: MutableSet<Room> = mutableSetOf()
        private set

    init {
        on<TickEvent.End> {
            if (mc.level == null || mc.player == null) return@on

            if ((!DungeonUtils.inDungeons && !LocationUtils.isCurrentArea(Island.SinglePlayer)) || DungeonUtils.inBoss) {
                currentRoom?.let { RoomEnterEvent(null).postAndCatch() }
                return@on
            } // We want the current room to register as null if we are not in a dungeon

            val roomCenter = getRoomCenter(mc.player?.x?.toInt() ?: return@on, mc.player?.z?.toInt() ?: return@on)
            if (roomCenter == lastRoomPos && LocationUtils.isCurrentArea(Island.SinglePlayer)) return@on // extra SinglePlayer caching for invalid placed rooms
            lastRoomPos = roomCenter

            passedRooms.find { previousRoom -> previousRoom.roomComponents.any { it.vec2 == roomCenter } }?.let { room ->
                if (currentRoom?.roomComponents?.none { it.vec2 == roomCenter } == true) RoomEnterEvent(room).postAndCatch()
                return@on
            } // We want to use cached rooms instead of scanning it again if we have already passed through it and if we are already in it, we don't want to trigger the event

            scanRoom(roomCenter)?.let { room -> if (room.rotation != Rotations.NONE) RoomEnterEvent(room).postAndCatch() } ?: run {
                if ((!DungeonUtils.inClear) && !LocationUtils.isCurrentArea(Island.SinglePlayer)) return@on
                devMessage("Unable to determine room at $roomCenter core: ${getCore(roomCenter)}")
            }
        }

        on<RoomEnterEvent> {
            currentRoom = room
            if (passedRooms.none { it.data.name == currentRoom?.data?.name }) passedRooms.add(currentRoom ?: return@on)
            devMessage("${room?.data?.name} - ${room?.rotation} || clay: ${room?.clayPos}")
        }

        on<DungeonRoomEnterEvent> {
            devMessage("-- ${room?.data?.name} - ${room?.rotation} || clay: ${room?.clayPos}")
        }

        on<WorldEvent.Load> {
            passedRooms.clear()
            currentRoom = null
            lastRoomPos = Vec2(0, 0)
        }
    }

    fun updateRotation(room: Room, roomHeight: Int) {
        if (room.data.name == "Fairy") { // Fairy room doesn't have a clay block so we need to set it manually
            room.clayPos = room.roomComponents.firstOrNull()?.let { BlockPos(it.x - 15, roomHeight, it.z - 15) } ?: return
            room.rotation = Rotations.SOUTH
            return
        }

        val level = mc.level ?: return
        room.rotation = Rotations.entries.dropLast(1).find { rotation ->
            room.roomComponents.any { component ->
                BlockPos(component.x + rotation.x, roomHeight, component.z + rotation.z).let { blockPos ->
                    level.getBlockState(blockPos).block == Blocks.BLUE_TERRACOTTA && (room.roomComponents.size == 1 || horizontals.all { facing ->
                        level.getBlockState(
                            blockPos.offset((if (facing.axis == Direction.Axis.X) facing.stepX else 0), 0, (if (facing.axis == Direction.Axis.Z) facing.stepZ else 0))
                        ).block.equalsOneOf(Blocks.AIR, Blocks.BLUE_TERRACOTTA)
                    }).also { isCorrectClay -> if (isCorrectClay) room.clayPos = blockPos }
                }
            }
        } ?: Rotations.NONE // Rotation isn't found if we can't find the clay block
    }

    fun scanRoom(vec2: Vec2): Room? {
        val level = mc.level ?: return null
        val chunk = level.getChunk(vec2.x shr 4, vec2.z shr 4)
        val roomHeight = getTopLayerOfRoom(vec2, chunk)
        return getCoreAtHeight(vec2, roomHeight, chunk).let { core ->
            coreToRoomData[core]?.let { roomData ->
                Room(data = roomData, roomComponents = findRoomComponentsRecursively(vec2, roomData.cores, roomHeight, level))
            }?.apply { updateRotation(this, roomHeight) }
        }
    }

    private fun findRoomComponentsRecursively(vec2: Vec2, cores: List<Int>, roomHeight: Int, level: ClientLevel, visited: MutableSet<Vec2> = mutableSetOf(), tiles: MutableSet<RoomComponent> = mutableSetOf()): MutableSet<RoomComponent> {
        if (vec2 in visited) return tiles else visited.add(vec2)

        val chunk = level.getChunk(vec2.x shr 4, vec2.z shr 4)
        val core = getCoreAtHeight(vec2, roomHeight, chunk)
        if (core !in cores) return tiles

        tiles.add(RoomComponent(vec2.x, vec2.z, core))
        horizontals.forEach { facing ->
            findRoomComponentsRecursively(
                Vec2(
                    vec2.x + ((if (facing.axis == Direction.Axis.X) facing.stepX else 0) shl ROOM_SIZE_SHIFT),
                    vec2.z + ((if (facing.axis == Direction.Axis.Z) facing.stepZ else 0) shl ROOM_SIZE_SHIFT)
                ), cores, roomHeight, level, visited, tiles
            )
        }
        return tiles
    }

    fun getRoomCenter(posX: Int, posZ: Int): Vec2 {
        val roomX = (posX - START + (1 shl (ROOM_SIZE_SHIFT - 1))) shr ROOM_SIZE_SHIFT
        val roomZ = (posZ - START + (1 shl (ROOM_SIZE_SHIFT - 1))) shr ROOM_SIZE_SHIFT
        return Vec2(((roomX shl ROOM_SIZE_SHIFT) + START), ((roomZ shl ROOM_SIZE_SHIFT) + START))
    }

    fun getCore(vec2: Vec2): Int {
        val level = mc.level ?: return 0
        val chunk = level.getChunk(vec2.x shr 4, vec2.z shr 4)
        return getCoreAtHeight(vec2, getTopLayerOfRoom(vec2, chunk), chunk)
    }

    private fun getCoreAtHeight(vec2: Vec2, roomHeight: Int, chunk: LevelChunk): Int {
        val sb = StringBuilder(150)
        val clampedHeight = roomHeight.coerceIn(11..140)
        sb.append(CharArray(140 - clampedHeight) { '0' })
        var bedrock = 0

        for (y in clampedHeight downTo 12) {
            mutableBlockPos.set(vec2.x, y, vec2.z)
            val block = chunk.getBlockState(mutableBlockPos).block
            if (block == Blocks.AIR && bedrock >= 2 && y < 69) {
                sb.append(CharArray(y - 11) { '0' })
                break
            }

            if (block == Blocks.BEDROCK) bedrock++
            else {
                bedrock = 0
                if (block.equalsOneOf(Blocks.OAK_PLANKS, Blocks.TRAPPED_CHEST, Blocks.CHEST)) continue
            }
            sb.append(block)
        }
        return sb.toString().hashCode()
    }

    fun getTopLayerOfRoom(vec2: Vec2, chunk: LevelChunk): Int {
        for (y in 160 downTo 12) {
            mutableBlockPos.set(vec2.x, y, vec2.z)
            val blockState = chunk.getBlockState(mutableBlockPos)
            if (!blockState.isAir) return if (blockState.block == Blocks.GOLD_BLOCK) y - 1 else y
        }
        return 0
    }

        /*
        if (false) HeightMap.getHeight(vec2.x and 15, vec2.z and 15)
        else {
            val chunk = mc.world?.getChunk(ChunkSectionPos.getSectionCoord(vec2.x), ChunkSectionPos.getSectionCoord(vec2.z)) ?: return 0
            chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE).get(vec2.x and 15, vec2.z and 15).coerceIn(11..140) - 1
        }*/
}