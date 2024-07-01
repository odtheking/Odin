package me.odinmain.features.impl.dungeon

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.DualSetting
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.ui.hud.HudElement
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.getMCTextWidth
import me.odinmain.utils.render.mcText
import me.odinmain.utils.skyblock.dungeon.DungeonUtils

object MapInfo : Module(
    name = "Map Info",
    category = Category.DUNGEON,
    description = "Displays various information about the current dungeon map"
) {
    private val disableInBoss: Boolean by BooleanSetting("Disable in boss", default = true, description = "Disables the information display when you're in boss.")
    private val minSecrets: Boolean by DualSetting("Min Secrets", "Minimum", "Remaining", default = false, description = "Display minimum secrets or secrets until s+.")
    private val unknown: Boolean by DualSetting("Deaths", "Deaths", "Unknown", default = false, description = "Display deaths or unknown secrets. (Unknown secrets are secrets in rooms that haven't been discovered yet. May not be helpful in full party runs.)")
    val togglePaul: Int by SelectorSetting("Paul Settings", "Automatic", options = arrayListOf("Automatic", "Force Disable", "Force Enable"))

    val hud: HudElement by HudSetting("Hud", 10f, 10f, 1f, false) {
        if (it) {
            val unknownText = if (!unknown) "§7Deaths: §c0" else "§7Unknown: §b??"
            val unknownWidth = getMCTextWidth(unknownText)
            val cryptWidth = getMCTextWidth("§7Crypts: §c?")
            val scoreWidth = getMCTextWidth("§7Score: §d???")
            mcText("§7Secrets: §e0§7-§b?§7-§c?", 1, 1, 1f, Color.WHITE, center = false)
            mcText("§7Score: §d???", 159 - scoreWidth, 1, 1f, Color.WHITE, center = false)
            mcText(unknownText, 1, 10, 1f, Color.WHITE, center = false)
            val centerX = (unknownWidth+(160-unknownWidth-cryptWidth)/2) - getMCTextWidth("§7Mimic: §c✘")/2
            mcText("§7Mimic: §c✘", centerX, 10, 1f, Color.WHITE, center = false)
            mcText("§7Crypts: §c?", 159 - cryptWidth, 10, 1f, Color.WHITE, center = false)
        } else if (DungeonUtils.inDungeons && (!disableInBoss || !DungeonUtils.inBoss)){
            val unknownWidth = getMCTextWidth(unknownSecretsText)
            val cryptWidth = getMCTextWidth(cryptText)
            val scoreWidth = getMCTextWidth(scoreText)
            mcText(secretText, 1, 1, 1f, Color.WHITE, center = false)
            mcText(scoreText, 159 - scoreWidth, 1, 1f, Color.WHITE, center = false)
            mcText(unknownSecretsText, 1, 10, 1f, Color.WHITE, center = false)
            val centerX = (unknownWidth+(159-unknownWidth-cryptWidth)/2) - getMCTextWidth(mimicText)/2
            mcText(mimicText, centerX, 10, 1f, Color.WHITE, center = false)
            mcText(cryptText, 159 - cryptWidth, 10, 1f, Color.WHITE, center = false)
        } else return@HudSetting 0f to 0f
        160f to 18f
    }

    private var secretText = "§7Secrets: §e0§7-§b?§7-§c?"
    private var unknownSecretsText = if (!unknown) "§7Deaths: §a0" else "§7Unknown: §b??"
    private var mimicText = "§7Mimic: §c✘"
    private var cryptText = "§7Crypts: §c0"
    private var scoreText = "§7Score: §d???"

    private fun colorizeCrypts(count: Int): String {
        return when {
            count < 3 -> "§c${count}"
            count <5 -> "§e${count}"
            else -> "§a${count}"
        }
    }

    private fun colorizeScore(score: Int): String {
        return when {
            score < 270 -> "§c${score}"
            score < 300-> "§e${score}"
            else -> "§a${score}"
        }
    }

    private fun colorizeDeaths(count: Int): String {
        return when {
            count == 0 -> "§a0"
            count <= if (DungeonUtils.floorNumber < 6) 2 else 3 -> "§e${count}"
            count == if (DungeonUtils.floorNumber < 6) 3 else 4 -> "§c${count}"
            else -> "§4${count}"
        }
    }

    init {
        execute(500) {
            if (!DungeonUtils.inDungeons || (disableInBoss && DungeonUtils.inBoss)) return@execute
            secretText = "§7Secrets: §e${DungeonUtils.secretCount}§7-§b${if (!minSecrets) DungeonUtils.neededSecretsAmount else (DungeonUtils.neededSecretsAmount - DungeonUtils.secretCount).coerceAtLeast(0)}§7-§c${DungeonUtils.totalSecrets}"
            unknownSecretsText = if (!unknown) "§7Deaths: §c${colorizeDeaths(DungeonUtils.deathCount)}" else "§7Unknown: §e${(DungeonUtils.totalSecrets - DungeonUtils.knownSecrets).coerceAtLeast(0)}"
            mimicText = if (DungeonUtils.mimicKilled) "§7Mimic: §a✔" else "§7Mimic: §c✘"
            cryptText = "§7Crypts: ${colorizeCrypts(DungeonUtils.cryptCount)}"
            scoreText = "§7Score: ${colorizeScore(DungeonUtils.score)}"
        }
    }

}