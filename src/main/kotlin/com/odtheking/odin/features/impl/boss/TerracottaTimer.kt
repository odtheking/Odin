package com.odtheking.odin.features.impl.boss

import com.odtheking.odin.events.BlockUpdateEvent
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.RenderExtractEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.center
import com.odtheking.odin.utils.handlers.ServerTick
import com.odtheking.odin.utils.render.drawText
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.toFixed
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.FlowerPotBlock

object TerracottaTimer : Module(
    name = "Terracotta Timer",
    description = "Displays the time until the terracotta respawns."
) {
    private data class Terracotta(val pos: BlockPos, val spawnTick: Long)
    private val terracottaSpawning = ArrayList<Terracotta>()

    init {
        on<BlockUpdateEvent> {
            if (DungeonUtils.isFloor(6) && DungeonUtils.inBoss && updated.block is FlowerPotBlock && terracottaSpawning.none { it.pos == pos && it.spawnTick > ServerTick.now })
                terracottaSpawning.add(Terracotta(pos.immutable(), ServerTick.now + if (DungeonUtils.floor?.isMM == true) 240 else 300))
        }

        on<RenderExtractEvent> {
            if (!DungeonUtils.inBoss || !DungeonUtils.isFloor(6) || terracottaSpawning.isEmpty()) return@on
            terracottaSpawning.removeIf { it.spawnTick <= ServerTick.now }
            terracottaSpawning.forEach {
                val time = (it.spawnTick - ServerTick.now) / 20f
                drawText("§${getColor(time)}${time.toFixed()}s", it.pos.center, 2f, false)
            }
        }

        on<LevelEvent.Load> {
            terracottaSpawning.clear()
        }
    }

    private fun getColor(time: Float): Char {
        return when {
            time > 5f -> 'a'
            time > 2f -> '6'
            else -> 'c'
        }
    }
}