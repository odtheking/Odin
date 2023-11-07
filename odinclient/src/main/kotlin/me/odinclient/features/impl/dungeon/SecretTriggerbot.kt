package me.odinclient.features.impl.dungeon

import me.odinclient.utils.skyblock.PlayerUtils.rightClick
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.init.Blocks
import net.minecraft.util.BlockPos

object SecretTriggerbot : Module(
    name = "Secret Triggerbot",
    description = "Automatically gets secrets when looking at them.",
    category = Category.DUNGEON
) {
    private val delay: Long by NumberSetting("Delay", 200L, 70, 1000)
    private val crystalHollowsChests: Boolean by BooleanSetting("Crystal Hollows Chests", true, description = "Opens chests in crystal hollows when looking at them")
    private val inBoss: Boolean by BooleanSetting("In Boss", true, description = "Makes the triggerbot work in dungeon boss aswell.")
    private val triggerBotClock = Clock(delay)
    private var clickedPositions = mapOf<BlockPos, Long>()

    fun tryTriggerbot() {
        if (
            !enabled ||
            !triggerBotClock.hasTimePassed(delay) ||
            DungeonUtils.currentRoomName.equalsOneOf("Water Board", "Three Weirdos") ||
            mc.currentScreen != null
        ) return

        val pos = mc.objectMouseOver?.blockPos ?: return
        val state = mc.theWorld.getBlockState(pos) ?: return
        clickedPositions = clickedPositions.filter { it.value + 1000L > System.currentTimeMillis() }
        if (
            (pos.x in 58..62 && pos.y in 133..136 && pos.z == 142) || // looking at lights device
            clickedPositions.containsKey(pos) // already clicked
        ) return

        if (crystalHollowsChests && LocationUtils.currentArea == "Crystal Hollows" && state.block == Blocks.chest) {
            rightClick()
            triggerBotClock.update()
            clickedPositions = clickedPositions.plus(pos to System.currentTimeMillis())
            return
        }

        if (!DungeonUtils.inDungeons || (!inBoss && DungeonUtils.inBoss) || !DungeonUtils.isSecret(state, pos)) return

        rightClick()
        triggerBotClock.update()
        clickedPositions = clickedPositions.plus(pos to System.currentTimeMillis())
    }
}