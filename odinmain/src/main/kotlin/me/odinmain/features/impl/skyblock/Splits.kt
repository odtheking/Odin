package me.odinmain.features.impl.skyblock

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.ui.hud.HudElement
import me.odinmain.utils.*
import me.odinmain.utils.render.*
import me.odinmain.utils.skyblock.*

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
            val (times, current) = SplitsManager.getSplitTimes()
            if (SplitsManager.currentSplits.isEmpty()) return@HudSetting 0f to 0f

            SplitsManager.currentSplits.dropLast(1).forEachIndexed { index, split ->
                val time = formatTime(if (index >= times.size) 0 else times[index])
                text("${split.name}: $time", 1f, 9f + index * getTextHeight("12", 13f), Color.WHITE, 12f, shadow = true)
            }
            text("Total: ${formatTime(times.sum())}", 1f, (SplitsManager.currentSplits.size + 1) * getTextHeight("12", 13f), Color.WHITE, 12f, shadow = true)

            getTextWidth("Fuel/Stun: 0h 00m 00s", 12f) + 2f to 80f
        }
    }

    private val instanceSplits: Int by SelectorSetting("Instance Splits", "None", arrayListOf("None", "SinglePlayer", "Kuudra T5", "Kuudra T1-T4", "F1", "F2", "F3", "F4", "F5", "F6", "F7", "M7"), description = "Shows splits for the current instance.")
    private val dropdownSetting: Boolean by BooleanSetting("Dropdown", false, description = "Shows the splits in a dropdown menu.").withDependency { instanceSplits != 0 }
    private val aaa: String by StringSetting("aaa", "Supplies").withDependency { dropdownSetting && instanceSplits == 1 }
    private val bbb: String by StringSetting("bbb", "Build").withDependency { dropdownSetting && instanceSplits == 1 }
    private val ccc: String by StringSetting("ccc", "Fuel/Stun").withDependency { dropdownSetting && instanceSplits == 1 }
    private val ddd: String by StringSetting("ddd", "Kill").withDependency { dropdownSetting && instanceSplits == 1 }
    private val eee: String by StringSetting("eee", "WTF").withDependency { dropdownSetting && instanceSplits == 1 }
    private val fff: String by StringSetting("fff", "WTFv3").withDependency { dropdownSetting && instanceSplits == 1 }

    private var hasChangeWorld = false
    init {
        onWorldLoad{
            hasChangeWorld = true
            allSplits.forEach{ (_, splits) ->
                splits.forEach { it.time = 0L }
            }
            SplitsManager.currentSplits.clear()
        }

        onMessage(Regex(".*")){
            SplitsManager.handleMessage(it)
        }

        execute(500) {
            if (!hasChangeWorld || LocationUtils.currentArea.isArea(Island.Unknown)) return@execute
            hasChangeWorld = false
            modMessage("Loading splits for ${LocationUtils.currentArea.name}")
            val currentInstance = allSplits[LocationUtils.currentArea.name] ?: return@execute
            currentInstance.forEach { it.time = 0L }
            SplitsManager.currentSplits = currentInstance.toMutableList()
        }
    }

    private val singlePlayer = mutableListOf(
        Split("aaa", aaa),
        Split("bbb", bbb),
        Split("ccc", ccc),
        Split("ddd", ddd),
        Split("eee", eee),
        Split("fff", fff),
        Split("end", "Run Total"),
    )

    private val allSplits = mutableMapOf(
        "SinglePlayer" to singlePlayer
    )
}