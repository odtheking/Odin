package me.odinmain.features.impl.dungeon.puzzlesolvers

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import me.odinmain.events.impl.EnteredDungeonRoomEvent
import me.odinmain.utils.Vec2
import me.odinmain.utils.addRotationCoords
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.util.BlockPos
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

object BeamsSolver {
    private var scanned = false
    private var lanternPairs: List<List<Int>>
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val isr = this::class.java.getResourceAsStream("/creeperBeamsSolutions.json")
        ?.let { InputStreamReader(it, StandardCharsets.UTF_8) }

    init {
        try {
            val text = isr?.readText()
            lanternPairs = gson.fromJson(
                text, object : TypeToken<List<List<List<Int>>>>() {}.type
            )
            isr?.close()
            println(lanternPairs.toString())
        } catch (e: Exception) {
            e.printStackTrace()
            lanternPairs = emptyList()
        }
    }

    fun enterDungeonRoom(event: EnteredDungeonRoomEvent) {
        if (event.room?.room?.data?.name != "Creeper Beams" || scanned) return
        val rotation = event.room.room.rotation
        lanternPairs.forEach {

        }
        scanned = true
    }

    fun onRenderWorld() {
        lanternPairs.forEach {
            val room = DungeonUtils.currentRoom?.room ?: return@forEach
            val x = DungeonUtils.currentRoom?.room?.x ?: return
            val z = DungeonUtils.currentRoom?.room?.z ?: return

            val pos = Vec2(x, z).addRotationCoords(room.rotation, x = it[0], y = it[2])
            RenderUtils.drawBlockBox(BlockPos(pos.x, it[1], pos.z), Color.WHITE)
        }
    }
}