package me.odinmain.features.impl.dungeon

import me.odinmain.utils.equal
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.M7Phases
import me.odinmain.utils.skyblock.getBlockAt
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3

object LeapHelper {
    private val NONE = Vec3(0.0, 0.0, 0.0)
    private val messageMap = mapOf(
        "" to NONE,
        "[BOSS] Maxor: I’M TOO YOUNG TO DIE AGAIN!" to Vec3(65.0, 175.0, 53.0),
        "[BOSS] Storm: I should have known that I stood no chance." to Vec3(107.0, 119.0, 93.0),
        "[BOSS] Goldor: You have done it, you destroyed the factory…" to Vec3(54.0, 115.0, 70.0),
        "[BOSS] Necron: You went further than any human before, congratulations." to Vec3(41.0, 64.0, 102.0),
        "[BOSS] Necron: That's a very impressive trick. I guess I'll have to handle this myself." to Vec3(54.0, 65.0, 82.0),
        "[BOSS] Necron: Let's make some space!" to Vec3(54.0, 4.0, 95.0)
    )
    private var currentPos = NONE

    val leapHelperName
        get() = getPlayer()

    private fun getPlayer(): String {
        if (DungeonUtils.dungeonTeammatesNoSelf.isEmpty()) return ""
        if (!DungeonUtils.inBoss) return DungeonUtils.doorOpener
        if (DungeonUtils.getF7Phase() == M7Phases.P3) scanGates()
        if (currentPos == NONE) return ""
        return DungeonUtils.dungeonTeammatesNoSelf
            .minByOrNull {
                val entity = it.entity
                if (entity == null) 10000.0 else {
                    if (currentPos.equal(Vec3(54.0, 4.0, 95.0)) && entity.posY >= 54.0) 10000.0
                    else entity.positionVector.distanceTo(currentPos)
                }
            }
            ?.entity
            ?.displayNameString
            .noControlCodes
    }

    private val gateBlocks = mapOf(
        BlockPos(8, 118, 50) to Vec3(8.0, 113.0, 51.0),
        BlockPos(18, 118, 132) to Vec3(19.0, 114.0, 132.0),
        BlockPos(100, 118, 122) to Vec3(100.0, 115.0, 121.0)
    )

    private fun scanGates() {
        gateBlocks.entries.forEach { (pos, vec) ->
            if (getBlockAt(pos) == Blocks.air) currentPos = vec // Is barrier if gate is closed
        }
    }

    fun worldLoad() {
        currentPos = NONE
    }

    fun leapHelperBossChatEvent(message: String) {
        if (message in messageMap) currentPos = messageMap[message] ?: NONE
    }
}