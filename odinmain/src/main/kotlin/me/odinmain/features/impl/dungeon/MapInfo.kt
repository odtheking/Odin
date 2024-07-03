package me.odinmain.features.impl.dungeon

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.ui.hud.HudElement
import me.odinmain.utils.render.*
import me.odinmain.utils.skyblock.dungeon.DungeonUtils

object MapInfo : Module(
    name = "Map Info",
    category = Category.DUNGEON,
    description = "Displays various information about the current dungeon map"
) {
    private val disableInBoss: Boolean by BooleanSetting("Disable in boss", default = true, description = "Disables the information display when you're in boss.")
    private val remaining: Boolean by DualSetting("Min Secrets", "Minimum", "Remaining", default = false, description = "Display minimum secrets or secrets until s+.")
    private val unknown: Boolean by DualSetting("Deaths", "Deaths", "Unknown", default = false, description = "Display deaths or unknown secrets. (Unknown secrets are secrets in rooms that haven't been discovered yet. May not be helpful in full party runs.)")
    val togglePaul: Int by SelectorSetting("Paul Settings", "Automatic", options = arrayListOf("Automatic", "Force Disable", "Force Enable"))
    private val background: Boolean by BooleanSetting("Background", default = false, description = "Render a background behind the score info")
    private val color: Color by ColorSetting("Background Color", default = Color.DARK_GRAY.withAlpha(0.5f), true, description = "The color of the background").withDependency { background }

    val hud: HudElement by HudSetting("Hud", 10f, 10f, 1f, false) {
        if ((!DungeonUtils.inDungeons || (disableInBoss && DungeonUtils.inBoss)) && !it) return@HudSetting 0f to 0f

        val cryptText = "§7Crypts: ${colorizeCrypts(DungeonUtils.cryptCount)}"
        val secretText = "§7Secrets: §b${DungeonUtils.secretCount}§7-§e${if (!remaining) DungeonUtils.neededSecretsAmount else (DungeonUtils.neededSecretsAmount - DungeonUtils.secretCount).coerceAtLeast(0)}§7-§c${DungeonUtils.totalSecrets}"
        val unknownSecretsText = if (!unknown) "§7Deaths: §c${colorizeDeaths(DungeonUtils.deathCount)}" else "§7Unknown: §e${(DungeonUtils.totalSecrets - DungeonUtils.knownSecrets).coerceAtLeast(0)}"
        val mimicText = if (DungeonUtils.mimicKilled) "§7Mimic: §a✔" else "§7Mimic: §c✘"
        val scoreText = "§7Score: ${colorizeScore(DungeonUtils.score)}"

        if (background) roundedRectangle(0, 0, 160, 19, color, 0, 0)
        val cryptWidth = getMCTextWidth(cryptText)
        val scoreWidth = getMCTextWidth(scoreText)
        mcText(secretText, 1, 1, 1f, Color.WHITE, center = false)
        mcText(scoreText, 159 - scoreWidth, 1, 1f, Color.WHITE, center = false)
        val unknownWidth = mcTextAndWidth(unknownSecretsText, 1, 10, 1f, Color.WHITE, center = false)
        val centerX = (unknownWidth+1+(159-unknownWidth-cryptWidth)/2) - getMCTextWidth(mimicText)/2
        mcText(mimicText, centerX, 10, 1f, Color.WHITE, center = false)
        mcText(cryptText, 159 - cryptWidth, 10, 1f, Color.WHITE, center = false)
        160f to 19f
    }

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
}