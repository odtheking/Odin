package me.odinmain.utils.skyblock.dungeon

import com.google.gson.Gson
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import me.odinmain.OdinMain.mc
import me.odinmain.utils.Utils.equalsOneOf
import me.odinmain.utils.skyblock.dungeon.ScanUtils.roomList
import net.minecraft.block.Block
import net.minecraft.util.BlockPos
import net.minecraft.util.ResourceLocation
import java.io.FileNotFoundException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

object ScanUtils {
    val roomList: Set<RoomData> = try {
        Gson().fromJson(
            (ScanUtils::class.java.getResourceAsStream("/rooms.json") ?: throw FileNotFoundException()).bufferedReader(),
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

    fun getRoomData(hash: Int): RoomData? {
        return roomList.find { hash in it.cores }
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