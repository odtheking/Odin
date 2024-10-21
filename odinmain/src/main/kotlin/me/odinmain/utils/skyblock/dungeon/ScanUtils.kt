package me.odinmain.utils.skyblock.dungeon

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import me.odinmain.OdinMain.logger
import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.DungeonEvents.RoomEnterEvent
import me.odinmain.utils.*
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.inBoss
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.inDungeons
import me.odinmain.utils.skyblock.dungeon.tiles.*
import net.minecraft.block.Block
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import java.io.FileNotFoundException

object ScanUtils {
    private const val ROOM_SIZE_SHIFT = 5  // Since ROOM_SIZE = 32 (2^5)
    private const val START = -185
    private const val DEFAULT_HEIGHT = 170

    private var lastRoomPos: Vec2 = Vec2(0, 0)
    private val roomList: Set<RoomData> = loadRoomData()
    var currentFullRoom: FullRoom? = null
        private set
    var passedRooms: MutableSet<FullRoom> = mutableSetOf()
        private set

    private fun loadRoomData(): Set<RoomData> {
        return try {
            GsonBuilder()
                .registerTypeAdapter(RoomData::class.java, RoomDataDeserializer())
                .create().fromJson(
                    (ScanUtils::class.java.getResourceAsStream("/rooms.json") ?: throw FileNotFoundException()).bufferedReader(),
                    object : TypeToken<Set<RoomData>>() {}.type
                )
        } catch (e: Exception) {
            handleRoomDataError(e)
            setOf()
        }
    }

    private fun handleRoomDataError(e: Exception) {
        when (e) {
            is JsonSyntaxException -> println("Error parsing room data.")
            is JsonIOException -> println("Error reading room data.")
            is FileNotFoundException -> println("Room data not found, something went wrong! Please report this!")
            else -> {
                println("Unknown error while reading room data.")
                logger.error("Error reading room data", e)
                println(e.message)
            }
        }
    }

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.END || mc.theWorld == null || mc.thePlayer == null) return

        if ((!inDungeons && !LocationUtils.currentArea.isArea(Island.SinglePlayer)) || inBoss) {
            currentFullRoom?.let { RoomEnterEvent(null).postAndCatch() }
            return
        } // If not in dungeon or in boss room, return and register current room as null

        val roomCenter = getRoomCenter(mc.thePlayer.posX.toInt(), mc.thePlayer.posZ.toInt()).takeIf { it != lastRoomPos } ?: return
        lastRoomPos = roomCenter

        passedRooms.find { previousRoom -> previousRoom.components.any { it.vec2 == roomCenter } }?.let { room ->
            if (currentFullRoom?.components?.none { it.vec2 == roomCenter } == true) RoomEnterEvent(room).postAndCatch()
            return
        } // If room is in passedRooms, post RoomEnterEvent and return only posts Event if room is not in currentFullRoom

        scanRoom(roomCenter)?.let { room ->
            FullRoom(room, BlockPos(0, 0, 0), findRoomTilesRecursively(room.vec2, room, mutableSetOf()), arrayListOf()).apply {
                updateRotation(this)
                if (room.rotation != Rotations.NONE) RoomEnterEvent(this).postAndCatch()
            }
        }
    }

    private fun updateRotation(fullRoom: FullRoom) {
        fullRoom.room.rotation = Rotations.entries.dropLast(1).find { rotation ->
            fullRoom.components.any { pos ->
                BlockPos(pos.x + rotation.x, getTopLayerOfRoom(fullRoom.room.vec2), pos.z + rotation.z).let { blockPos ->
                    getBlockIdAt(blockPos) == 159 && (fullRoom.components.size == 1 || EnumFacing.HORIZONTALS.all { facing ->
                        getBlockIdAt(blockPos.add(facing.frontOffsetX, 0, facing.frontOffsetZ)).equalsOneOf(159, 0)
                    }).also { isCorrectClay -> if (isCorrectClay) fullRoom.clayPos = blockPos }
                }
            }
        } ?: Rotations.NONE
    }

    private fun findRoomTilesRecursively(vec2: Vec2, room: Room, visited: MutableSet<Vec2>, tiles: ArrayList<ExtraRoom> = arrayListOf()): ArrayList<ExtraRoom> {
        if (vec2 in visited) return tiles
        visited.add(vec2)
        val core = getCore(vec2)
        if (core in room.data.cores) {
            tiles.add(ExtraRoom(vec2.x, vec2.z, core))
            EnumFacing.HORIZONTALS.forEach { facing ->
                findRoomTilesRecursively(Vec2(vec2.x + (facing.frontOffsetX shl ROOM_SIZE_SHIFT), vec2.z + (facing.frontOffsetZ shl ROOM_SIZE_SHIFT)), room, visited, tiles)
            }
        }
        return tiles
    }

    private fun scanRoom(vec2: Vec2): Room? =
        getCore(vec2).let { core -> getRoomData(core)?.let { Room(vec2.x, vec2.z, it, core) } }

    fun getRoomSecrets(name: String): Int =
        roomList.find { it.name == name }?.secrets ?: 0

    private fun getRoomData(hash: Int): RoomData? =
        roomList.find { hash in it.cores }

    fun getRoomCenter(posX: Int, posZ: Int): Vec2 {
        val roomX = (posX - START + (1 shl (ROOM_SIZE_SHIFT - 1))) shr ROOM_SIZE_SHIFT
        val roomZ = (posZ - START + (1 shl (ROOM_SIZE_SHIFT - 1))) shr ROOM_SIZE_SHIFT
        return Vec2((roomX shl ROOM_SIZE_SHIFT) + START, (roomZ shl ROOM_SIZE_SHIFT) + START)
    }

    fun getCore(vec2: Vec2): Int {
        val sb = StringBuilder(150)
        val chunk = mc.theWorld?.getChunkFromChunkCoords(vec2.x shr 4, vec2.z shr 4) ?: return 0
        val height = chunk.getHeightValue(vec2.x and 15, vec2.z and 15).coerceIn(11..140)
        sb.append(CharArray(140 - height) { '0' })
        var bedrock = 0
        for (y in height downTo 12) {
            val id = Block.getIdFromBlock(chunk.getBlock(BlockPos(vec2.x, y, vec2.z)))
            if (id == 0 && bedrock >= 2 && y < 69) {
                sb.append(CharArray(y - 11) { '0' })
                break
            }

            if (id == 7) bedrock++
            else {
                bedrock = 0
                if (id.equalsOneOf(5, 54, 146)) continue
            }
            sb.append(id)
        }
        return sb.toString().hashCode()
    }

    private fun getTopLayerOfRoom(vec2: Vec2, currentHeight: Int = DEFAULT_HEIGHT): Int {
        return if ((isAir(BlockPos(vec2.x, currentHeight, vec2.z)) || isGold(BlockPos(vec2.x, currentHeight, vec2.z))) && currentHeight > 70) getTopLayerOfRoom(vec2, currentHeight - 1) else currentHeight
    }

    @SubscribeEvent
    fun enterDungeonRoom(event: RoomEnterEvent) {
        currentFullRoom = event.fullRoom
        if (passedRooms.none { it.room.data.name == currentFullRoom?.room?.data?.name }) passedRooms.add(currentFullRoom ?: return)
        devMessage("${event.fullRoom?.room?.data?.name} - ${event.fullRoom?.room?.rotation} || clay: ${event.fullRoom?.clayPos}")
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Unload) {
        passedRooms.clear()
        currentFullRoom = null
        lastRoomPos = Vec2(0, 0)
    }
}