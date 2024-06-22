package me.odinmain.features.impl.skyblock

import me.odinmain.events.impl.SkyblockJoinIslandEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.ui.hud.HudElement
import me.odinmain.utils.*
import me.odinmain.utils.SplitsManager.currentSplits
import me.odinmain.utils.render.*
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object Splits : Module(
    "Splits",
    description = "Automatic advanced skyblock splits.",
    category = Category.SKYBLOCK
) {
    private val hud: HudElement by HudSetting("Splits Display HUD", 10f, 10f, 1f, true) {
        if (it) {
            for (i in 0 until 5) {
                text("Split $i: 0h 00m 00s", 1f, 9f + i * getTextHeight("12", 13f), Color.WHITE, 12f, shadow = true)
            }

            getTextWidth("Fuel/Stun: 0h 00m 00s", 12f) + 2f to 80f
        } else {
            val (times, current) = SplitsManager.getSplitTimes(currentSplits)
            if (currentSplits.splits.isEmpty()) return@HudSetting 0f to 0f
            val x = getMCTextWidth("Split: 0h 00m 00s")
            currentSplits.splits.dropLast(1).forEachIndexed { index, split ->
                val time = formatTime(if (index >= times.size) 0 else times[index])
                mcText(split.name, 1f, 9f + index * getMCTextHeight(), 1f, Color.WHITE, shadow = true, center = false)
                mcText(time, x, 9f + index * getMCTextHeight(), 1f, Color.WHITE, shadow = true, center = false)
            }
            if (bossEntrySplit && currentSplits.splits.size > 3) {
                val time = formatTime(times.take(3).sum())
                mcText("§9Boss Entry", 1f, (currentSplits.splits.size) * getMCTextHeight(), 1f, Color.WHITE, shadow = true, center = false)
                mcText(time, x, (currentSplits.splits.size) * getMCTextHeight(), 1f, Color.WHITE, shadow = true, center = false)
            }

            getMCTextWidth("Split 0: 0h 00m 00s") + 2f to 80f
        }
    }
    private val bossEntrySplit: Boolean by BooleanSetting("Boss Entry Split", true)
    val sendSplits: Boolean by BooleanSetting("Send Splits", true)
    val sendOnlyPB: Boolean by BooleanSetting("Send Only PB", false)

    private val singlePlayerPBs = PersonalBest("SinglePlayer", 4)
    private val kuudraPBs = PersonalBest("Kuudra", 4)
    private var currentPBValues = mutableListOf<Double>()

    init {
        onMessage(Regex(".*")){
            SplitsManager.handleMessage(it, currentSplits)
        }
        onWorldLoad {
            currentSplits = SplitsGroup(mutableListOf(), singlePlayerPBs)
            currentPBValues.clear()
        }
    }

    @SubscribeEvent
    fun onJoinSkyblockIsland(event: SkyblockJoinIslandEvent) {
        val currentSplits = initializeSplits(event.island) ?: return
        modMessage("Loading splits for ${LocationUtils.currentArea.name}")
        currentSplits.splits.forEach { split -> split.time = 0L }
        SplitsManager.currentSplits = currentSplits
        currentPBValues = currentSplits.personalBest?.pb ?: mutableListOf()
    }

    private fun initializeSplits(island: Island): SplitsGroup? {
        return when (island) {
            Island.SinglePlayer -> SplitsGroup(singlePlayerSplitGroup, singlePlayerPBs)

            Island.Dungeon -> {
                val split = dungeonSplits[DungeonUtils.floor.floorNumber] ?: return null

                split.add(0, Split(Regex("\\[NPC] Mort: Here, I found this map when I first entered the dungeon\\."), "§2Blood Open"))
                split.add(1, Split(Regex("The BLOOD DOOR has been opened!"), "§bBlood Clear"))
                split.add(2, Split(Regex("\\[BOSS] The Watcher: You have proven yourself\\. You may pass\\."), "§dBoss Entry"))
                split.add(Split(Regex("^\\s*☠ Defeated (.+) in 0?([\\dhms ]+?)\\s*(\\(NEW RECORD!\\))?\$"), "§1Total"))

                SplitsGroup(split, DungeonUtils.floor.personalBest)
            }
            // add rest of kuudra tiers
            Island.Kuudra -> {
                if (LocationUtils.kuudraTier != 5) return null
                SplitsGroup(mutableListOf(
                    Split(Regex("^\\[NPC] Elle: Okay adventurers, I will go and fish up Kuudra!$"), "Supplies"),
                    Split(Regex("^\\[NPC] Elle: OMG! Great work collecting my supplies!$"), "Build"),
                    Split(Regex("^\\[NPC] Elle: Phew! The Ballista is finally ready! It should be strong enough to tank Kuudra's blows now!$"), "Stun"),
                    Split(Regex("^\\[NPC] Elle: POW! SURELY THAT'S IT! I don't think he has any more in him!$"), "Kill"),
                    Split(Regex("^KUUDRA DOWN!$"), "Cleared")
                ), kuudraPBs)
            }

            else -> null
        }
    }
}