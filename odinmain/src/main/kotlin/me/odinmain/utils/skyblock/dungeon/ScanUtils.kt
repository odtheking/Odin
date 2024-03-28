package me.odinmain.utils.skyblock.dungeon

import com.google.gson.GsonBuilder
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import me.odinmain.OdinMain.mc
import me.odinmain.utils.Vec2
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.skyblock.dungeon.tiles.RoomData
import me.odinmain.utils.skyblock.dungeon.tiles.RoomDataDeserializer
import net.minecraft.block.Block
import net.minecraft.util.BlockPos
import java.io.FileNotFoundException

object ScanUtils {
    val roomList: Set<RoomData> = try {
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
                e.printStackTrace()
                println(e.message)
            }
        }
        setOf()
    }

    fun getRoomData(hash: Int): RoomData? =
        roomList.find { it.cores.any { core -> hash == core } }

    fun getCore(pos: Vec2): Int = getCore(pos.x, pos.z)

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