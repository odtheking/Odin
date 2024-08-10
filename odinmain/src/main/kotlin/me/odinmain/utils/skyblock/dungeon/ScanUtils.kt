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
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.passedRooms
import me.odinmain.utils.skyblock.dungeon.tiles.*
import net.minecraft.block.Block
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import java.io.FileNotFoundException
import kotlin.math.roundToInt

object ScanUtils {
    private val roomList: Set<RoomData> = try {
        GsonBuilder()
            .registerTypeAdapter(RoomData::class.java, RoomDataDeserializer())
            .create().fromJson(
                (ScanUtils::class.java.getResourceAsStream("/rooms.json") ?: throw FileNotFoundException()).bufferedReader(),
                object : TypeToken<Set<RoomData>>() {}.type
            )
    } catch (e: Exception) {
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
        setOf()
    }

    private const val ROOM_SIZE = 32
    private const val START_X = -185
    private const val START_Z = -185
    private var lastRoomPos: Vec2 = Vec2(0, 0)
    private var previousRoomScanned: FullRoom? = null
    private var noneRotationList: MutableList<FullRoom?> = mutableListOf()

//    @SubscribeEvent
//    fun onTick(event: ClientTickEvent) {
//        if (event.phase != TickEvent.Phase.END || mc.theWorld == null || mc.thePlayer == null) return
//
//        // If not in dungeons or in the boss area, handle room exit/reset events and also set current room to null by posting a null room event prevents features work when not needed
//        if ((!inDungeons && !LocationUtils.currentArea.isArea(Island.SinglePlayer)) || inBoss) {
//            if (DungeonUtils.currentFullRoom == null) return
//            RoomEnterEvent(null).postAndCatch() // Send event indicating room exit or reset
//            return
//        }
//
//        val roomCenter = getRoomCenter(mc.thePlayer.posX.toInt(), mc.thePlayer.posZ.toInt())
//
//        // Check if we are scanning the same room as last tick
//        if (previousRoomScanned?.extraRooms?.any { it.x == roomCenter.x && it.z == roomCenter.z } == true && previousRoomScanned?.room?.rotation != Rotations.NONE) {
//            // If the player is still in the same room and the rotation has been found, skip scanning
//            if (lastRoomPos.equal(roomCenter)) return
//            lastRoomPos = roomCenter
//
//            // Check if the room has been visited before
//            passedRooms.find { previousRoom -> previousRoom.extraRooms.any { it.x == roomCenter.x && it.z == roomCenter.z } }?.let { room ->
//                // If we're already in the cached room, avoid posting duplicate events
//                if (DungeonUtils.currentFullRoom?.extraRooms?.any { it.x == roomCenter.x && it.z == roomCenter.z } == true) return
//                RoomEnterEvent(room).postAndCatch()
//                return
//            }
//        }
//
//        // If scanning the same room as the previous tick and rotation wasn't found, use the previous room as fallback
//        val room = if (previousRoomScanned?.extraRooms?.any { it.x == roomCenter.x && it.z == roomCenter.z } == true && previousRoomScanned?.room?.rotation != Rotations.NONE)
//            previousRoomScanned?.room ?: return
//        else scanRoom(roomCenter) ?: return
//
//        val fullRoom = FullRoom(room, BlockPos(0, 0, 0), findRoomTilesRecursively(room.vec2, room, mutableSetOf()), emptyList()).apply {
//            this.room.rotation = Rotations.entries.dropLast(1).find { rotation ->
//                this.extraRooms.any { pos ->
//                    BlockPos(pos.x + rotation.x, getTopLayerOfRoom(this.room.vec2), pos.z + rotation.z).let { blockPos ->
//                        getBlockIdAt(blockPos) == 159 && EnumFacing.HORIZONTALS.all { facing ->
//                            getBlockIdAt(blockPos.add(facing.frontOffsetX, 0, facing.frontOffsetZ)).equalsOneOf(159, 0)
//                        }.also { isCorrectClay -> if (isCorrectClay) this.clayPos = blockPos }
//                    }
//                }
//            } ?: Rotations.NONE
//        }.also {
//            previousRoomScanned = it
//            if (it.room.rotation == Rotations.NONE) return
//        }
//
//        devMessage("Found rotation ${fullRoom.room.rotation}, clay pos: ${fullRoom.clayPos}")
//        RoomEnterEvent(fullRoom).postAndCatch()
//    }

    @SubscribeEvent
    fun onTick1(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.END || mc.theWorld == null || mc.thePlayer == null) return

        // If not in dungeons or in the boss area, handle room exit/reset events and also set current room to null by posting a null room event prevents features work when not needed
        if ((!inDungeons && !LocationUtils.currentArea.isArea(Island.SinglePlayer)) || inBoss) {
            if (DungeonUtils.currentFullRoom == null) return
            RoomEnterEvent(null).postAndCatch() // Send event indicating room exit or reset
            return
        }

        val roomCenter = getRoomCenter(mc.thePlayer.posX.toInt(), mc.thePlayer.posZ.toInt())

        noneRotationList.removeIf {
            if (it?.room?.vec2?.equals(roomCenter) == true) {
                updateRotation(it)
                if (it.room.rotation != Rotations.NONE) {
                    RoomEnterEvent(it).postAndCatch()
                    true
                } else false
            } else false
        }

        if (lastRoomPos.equal(roomCenter)) return
        lastRoomPos = roomCenter

        passedRooms.find { previousRoom -> previousRoom.extraRooms.any { it.x == roomCenter.x && it.z == roomCenter.z } }?.let { room ->
            if (DungeonUtils.currentFullRoom?.extraRooms?.any { it.x == roomCenter.x && it.z == roomCenter.z } == false) RoomEnterEvent(room).postAndCatch()
            return
        }

        val room = scanRoom(roomCenter) ?: return

        val fullRoom = FullRoom(room, BlockPos(0, 0, 0), findRoomTilesRecursively(room.vec2, room, mutableSetOf()), emptyList()).apply { updateRotation(this) }
            .also {
                if (it.room.rotation == Rotations.NONE) {
                    noneRotationList.add(it)
                    return@also
                }
            }

        devMessage("Found rotation ${fullRoom.room.rotation}, clay pos: ${fullRoom.clayPos}")
        RoomEnterEvent(fullRoom).postAndCatch()
    }

    fun updateRotation(fullRoom: FullRoom) {
        fullRoom.room.rotation = Rotations.entries.dropLast(1).find { rotation ->
            fullRoom.extraRooms.any { pos ->
                BlockPos(pos.x + rotation.x, getTopLayerOfRoom(fullRoom.room.vec2), pos.z + rotation.z).let { blockPos ->
                    getBlockIdAt(blockPos) == 159 && EnumFacing.HORIZONTALS.all { facing ->
                        getBlockIdAt(blockPos.add(facing.frontOffsetX, 0, facing.frontOffsetZ)).equalsOneOf(159, 0)
                    }.also { isCorrectClay -> if (isCorrectClay) fullRoom.clayPos = blockPos }
                }
            }
        } ?: Rotations.NONE
    }

    private fun findRoomTilesRecursively(vec2: Vec2, room: Room, visited: MutableSet<Vec2>): List<ExtraRoom> {
        val tiles = mutableListOf<ExtraRoom>()
        val core = getCore(vec2.takeIf { it !in visited }?.also { visited.add(it) } ?: return tiles)
        if (core in room.data.cores) {
            tiles.add(ExtraRoom(vec2.x, vec2.z, core))
            EnumFacing.HORIZONTALS.forEach {
                tiles.addAll(findRoomTilesRecursively(Vec2(vec2.x + it.frontOffsetX * ROOM_SIZE, vec2.z + it.frontOffsetZ * ROOM_SIZE), room, visited))
            }
        }
        return tiles
    }

    private fun scanRoom(vec2: Vec2): Room? =
        getCore(vec2).let { core -> getRoomData(core)?.let { Room(vec2.x, vec2.z, it).apply { this.core = core } } }

    fun getRoomSecrets(name: String): Int {
        return roomList.find { it.name == name }?.secrets ?: return 0
    }

    private fun getRoomData(hash: Int): RoomData? =
        roomList.find { hash in it.cores }

    fun getRoomCenter(posX: Int, posZ: Int): Vec2 {
        val roomX = ((posX - START_X) / 32f).roundToInt()
        val roomZ = ((posZ - START_Z) / 32f).roundToInt()
        return Vec2(roomX * 32 + START_X, roomZ * 32 + START_Z)
    }

    fun getCore(vec2: Vec2): Int {
        val sb = StringBuilder(150)
        val chunk = mc.theWorld?.getChunkFromBlockCoords(BlockPos(vec2.x, 0, vec2.z)) ?: return 0
        for (y in 140 downTo 12) {
            val id = Block.blockRegistry.getIDForObject(chunk.getBlock(BlockPos(vec2.x, y, vec2.z)))
            if (!id.equalsOneOf(5, 54, 153)) sb.append(id)
        }
        return sb.toString().hashCode()
    }

    /**
     * Gets the top layer of blocks in a room (the roof) for finding the rotation of the room.
     *
     * @param x The x of the room to scan
     * @param z The z of the room to scan
     * @param currentHeight The current height to scan at, default is 170
     * @return The y-value of the roof, this is the y-value of the blocks.
     */
    private fun getTopLayerOfRoom(vec2: Vec2, currentHeight: Int = 170): Int {
        return if ((isAir(vec2.x, currentHeight, vec2.z) || isGold(vec2.x, currentHeight, vec2.z)) && currentHeight > 70) getTopLayerOfRoom(vec2, currentHeight - 1) else currentHeight
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        lastRoomPos = Vec2(0, 0)
    }
}