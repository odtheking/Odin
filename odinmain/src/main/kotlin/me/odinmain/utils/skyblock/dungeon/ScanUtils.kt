package me.odinmain.utils.skyblock.dungeon

import com.google.gson.Gson
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import me.odinmain.OdinMain.mc
import me.odinmain.utils.Utils.equalsOneOf
import net.minecraft.block.Block
import net.minecraft.util.BlockPos
import net.minecraft.util.ResourceLocation
import java.io.FileNotFoundException

object ScanUtils {
    val roomList: Set<RoomData> = try {
        /*Gson().fromJson(WaterSolver::class.java.getResourceAsStream("/watertimes.json")
            ?.let { InputStreamReader(it, StandardCharsets.UTF_8) })

         */

        Gson().fromJson(
            mc.resourceManager.getResource(ResourceLocation("odin", "map/rooms.json"))
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