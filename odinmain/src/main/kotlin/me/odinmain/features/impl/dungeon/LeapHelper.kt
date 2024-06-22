package me.odinmain.features.impl.dungeon

import me.odinmain.OdinMain.mc
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.equal
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.M7Phases
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

    var leapHelperClear = ""
    var leapHelperBoss = ""
    var leapHelper: String = if (DungeonUtils.inBoss) leapHelperBoss else leapHelperClear

    fun getPlayer() {
        if (DungeonUtils.dungeonTeammates.isEmpty()) return
        if (DungeonUtils.getPhase() == M7Phases.P3) scanGates()
        if (currentPos == NONE) return
        leapHelperBoss = DungeonUtils.dungeonTeammates
            .filter {
                it.entity != null && it.entity != mc.thePlayer &&
                if (currentPos.equal(Vec3(54.0, 4.0, 95.0))) it.entity.posY < 54.0 else true // To make sure the player is underneath necron's platform
            }
            .minByOrNull { it.entity?.positionVector?.distanceTo(currentPos) ?: 10000.0 }
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
            val block = mc.theWorld.getBlockState(pos)
            if (block == Blocks.air.defaultState) // Is barrier if gate is closed
                currentPos = vec
        }
    }

    fun worldLoad() {
        currentPos = NONE
        leapHelperBoss = ""
        leapHelperClear = ""
    }

    private val keyRegex = Regex("(?:\\[\\w+] )?(\\w+) opened a (?:WITHER|Blood) door!")
    private val leapHelperClock = Clock(LeapMenu.delay * 1000L)

    fun leapHelperClearChatEvent(message: String) {
        if(leapHelperClock.hasTimePassed()) leapHelperClear = ""
        leapHelperClear = keyRegex.find(message)?.groupValues?.get(1) ?: return
        leapHelperClock.update()
    }

    fun leapHelperBossChatEvent(message: String) {
        if (message !in messageMap) return
        currentPos = messageMap[message] ?: NONE
        leapHelperBoss = ""
    }
}