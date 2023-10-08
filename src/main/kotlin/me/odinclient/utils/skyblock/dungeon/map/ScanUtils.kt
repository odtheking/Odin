package me.odinclient.utils.skyblock.dungeon.map

import com.google.gson.Gson
import com.google.gson.JsonIOException
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import me.odinclient.ModCore.Companion.mc
import me.odinclient.dungeonmap.core.RoomData
import me.odinclient.dungeonmap.core.map.Room
import me.odinclient.dungeonmap.features.Dungeon
import me.odinclient.dungeonmap.features.DungeonScan
import me.odinclient.features.impl.dungeon.WaterSolver
import me.odinclient.utils.Utils.equalsOneOf
import net.minecraft.block.Block
import net.minecraft.util.BlockPos
import net.minecraft.util.ResourceLocation
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import kotlin.math.roundToInt

object ScanUtils {
    val roomList: Set<RoomData> = try {
        /*Gson().fromJson(WaterSolver::class.java.getResourceAsStream("/watertimes.json")
            ?.let { InputStreamReader(it, StandardCharsets.UTF_8) })

         */

        Gson().fromJson(
            mc.resourceManager.getResource(ResourceLocation("odinclient", "map/rooms.json"))
                .inputStream.bufferedReader(),
            object : TypeToken<Set<RoomData>>() {}.type
        )
    } catch (e: JsonSyntaxException) {
        println("Error parsing room data.")
        setOf()
    } catch (e: JsonIOException) {
        println("Error reading room data.")
        setOf()
    } catch (e: FileNotFoundException) {
        println("Room data not found. You are either in developer environment, or something went wrong. Please report this!")
        setOf()
    }

    fun getRoomData(x: Int, z: Int): RoomData? {
        return getRoomData(getCore(x, z))
    }

    fun getRoomData(hash: Int): RoomData? {
        return roomList.find { hash in it.cores }
    }

    fun getRoomCentre(posX: Int, posZ: Int): Pair<Int, Int> {
        val roomX = ((posX - DungeonScan.startX) / 32f).roundToInt()
        val roomZ = ((posZ - DungeonScan.startZ) / 32f).roundToInt()
        return Pair(roomX * 32 + DungeonScan.startX, roomZ * 32 + DungeonScan.startZ)
    }

    fun getRoomFromPos(pos: BlockPos): Room? {
        val x = ((pos.x - DungeonScan.startX + 15) shr 5)
        val z = ((pos.z - DungeonScan.startZ + 15) shr 5)
        val room = Dungeon.Info.dungeonList.getOrNull(x * 2 + z * 22)
        return if (room is Room) room else null
    }

    fun getRoomFromPos(pos: Pair<Int, Int>): Room? {
        val x = ((pos.first - DungeonScan.startX + 15) shr 5)
        val z = ((pos.second - DungeonScan.startZ + 15) shr 5)
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