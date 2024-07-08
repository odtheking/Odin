package me.odinmain.utils.skyblock.dungeon

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import me.odinmain.OdinMain.logger
import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.DungeonEvents.RoomEnterEvent
import me.odinmain.features.impl.dungeon.dungeonwaypoints.DungeonWaypoints.setWaypoints
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

    fun getRoomSecrets(name: String): Int {
        return roomList.find { it.name == name }?.secrets ?: return 0
    }

    private fun getRoomData(hash: Int): RoomData? =
        roomList.find { it.cores.any { core -> hash == core } }


    fun getCore(x: Int, z: Int): Int {
        val blocks = arrayListOf<Int>()
        for (y in 140 downTo 12) {
            val id = Block.getIdFromBlock(mc.theWorld.getBlockState(BlockPos(x, y, z)).block)
            if (!id.equalsOneOf(5, 54)) blocks.add(id)
        }
        return blocks.joinToString("").hashCode()
    }

    private const val ROOM_SIZE = 32
    private const val START_X = -185
    private const val START_Z = -185
    private var lastRoomPos: Pair<Int, Int> = Pair(0, 0)

    @SubscribeEvent
    fun onTick(event: ClientTickEvent) {
        if (event.phase != TickEvent.Phase.END || mc.theWorld == null) return
        if ((!inDungeons && !LocationUtils.currentArea.isArea(Island.SinglePlayer)) || inBoss) {
            if (DungeonUtils.currentRoom == null) return
            RoomEnterEvent(null).postAndCatch()
            return
        }

        val xPos = START_X + ((mc.thePlayer.posX + 200) / 32).toInt() * ROOM_SIZE
        val zPos = START_Z + ((mc.thePlayer.posZ + 200) / 32).toInt() * ROOM_SIZE

        if (lastRoomPos.equal(xPos, zPos)) return
        lastRoomPos = Pair(xPos, zPos)

        val room = scanRoom(xPos, zPos) ?: return
        val positions = findRoomTilesRecursively(room.x, room.z, room, mutableSetOf())

        val fullRoom = FullRoom(room, BlockPos(0, 0, 0), positions, emptyList())
        val topLayer = getTopLayerOfRoom(fullRoom.positions.first().x, fullRoom.positions.first().z)
        fullRoom.room.rotation = Rotations.entries.dropLast(1).find { rotation ->
            fullRoom.positions.any { pos ->
                val blockPos = BlockPos(pos.x + rotation.x, topLayer, pos.z + rotation.z)
                val isCorrectClay = getBlockIdAt(blockPos) == 159 &&
                        EnumFacing.HORIZONTALS.all { facing ->
                            getBlockIdAt(blockPos.add(facing.frontOffsetX, 0, facing.frontOffsetZ)).equalsOneOf(159, 0)
                        }
                if (isCorrectClay) fullRoom.clayPos = blockPos
                return@any isCorrectClay
            }
        } ?: Rotations.NONE

        //devMessage("Found rotation ${fullRoom.room.rotation}, clay pos: ${fullRoom.clayPos}")
        setWaypoints(fullRoom)
        RoomEnterEvent(fullRoom).postAndCatch()
    }

    private fun findRoomTilesRecursively(x: Int, z: Int, room: Room, visited: MutableSet<Vec2>): List<ExtraRoom> {
        val tiles = mutableListOf<ExtraRoom>()
        val pos = Vec2(x, z)
        if (visited.contains(pos)) return tiles
        visited.add(pos)
        val core = getCore(x, z)
        if (room.data.cores.any { core == it }) {
            tiles.add(ExtraRoom(x, z, core))
            EnumFacing.HORIZONTALS.forEach {
                tiles.addAll(findRoomTilesRecursively(x + it.frontOffsetX * ROOM_SIZE, z + it.frontOffsetZ * ROOM_SIZE, room, visited))
            }
        }
        return tiles
    }

    private fun scanRoom(x: Int, z: Int): Room? {
        val roomCore = getCore(x, z)
        return Room(x, z, getRoomData(roomCore) ?: return null).apply { core = roomCore }
    }

    /**
     * Gets the top layer of blocks in a room (the roof) for finding the rotation of the room.
     *
     * @param x The x of the room to scan
     * @param z The z of the room to scan
     * @param currentHeight The current height to scan at, default is 170
     * @return The y-value of the roof, this is the y-value of the blocks.
     */
    private fun getTopLayerOfRoom(x: Int, z: Int, currentHeight: Int = 170): Int {
        return if (isAir(x, currentHeight, z) && currentHeight > 70) getTopLayerOfRoom(x, z, currentHeight - 1)
        else currentHeight
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        lastRoomPos = 0 to 0
    }
}