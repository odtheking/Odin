package me.odinmain.features.impl.dungeon

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.ui.hud.HudElement
import me.odinmain.utils.render.*
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils

object MapInfo : Module(
    name = "Map Info",
    category = Category.DUNGEON,
    description = "Displays various information about the current dungeon map"
) {

    private val disableInBoss: Boolean by BooleanSetting("Disable in boss", default = true, description = "Disables the information display when you're in boss.")
    private val scoreTitle: Boolean by BooleanSetting("300 Score Title", default = true, description = "Displays a title on 300 score")
    private val scoreText: String by StringSetting("Title Text", default = "&c300 Score!", description = "Text to be displayed on 300 score.").withDependency { scoreTitle }
    val togglePaul: Int by SelectorSetting("Paul Settings", "Automatic", options = arrayListOf("Automatic", "Force Disable", "Force Enable"))

    private val fullHud: HudElement by HudSetting("Full Hud", 10f, 10f, 1f, true) {
        if ((!DungeonUtils.inDungeons || (disableInBoss && DungeonUtils.inBoss)) && !it) return@HudSetting 0f to 0f

        val scoreText = "§7Score: ${colorizeScore(DungeonUtils.score)}"
        val secretText = "§7Secrets: §b${DungeonUtils.secretCount}" +
                (if (fullAddRemaining && alternate) "§7-§d${(DungeonUtils.neededSecretsAmount - DungeonUtils.secretCount).coerceAtLeast(0)}" else "") +
                "§7-§e${if (!fullRemaining || (fullAddRemaining && alternate)) DungeonUtils.neededSecretsAmount else (DungeonUtils.neededSecretsAmount - DungeonUtils.secretCount).coerceAtLeast(0)}"+
                "§7-§c${DungeonUtils.totalSecrets}"
        val unknownSecretsText = if (!unknown) "§7Deaths: §c${colorizeDeaths(DungeonUtils.deathCount)}" else "§7Unfound: §e${(DungeonUtils.totalSecrets - DungeonUtils.knownSecrets).coerceAtLeast(0)}"
        val mimicText = "§7Mimic: ${if (DungeonUtils.mimicKilled) "§a✔" else "§c✘"}"
        val cryptText = "§7Crypts: ${colorizeCrypts(DungeonUtils.cryptCount.coerceAtMost(5))}"

        val (trText, brText) = if (alternate) listOf(cryptText, scoreText) else listOf(scoreText, cryptText)

        if (fullBackground) roundedRectangle(-fullMargin, 0, fullWidth + (fullMargin * 2), 19, fullColor, 0, 0)
        val brWidth = getMCTextWidth(brText)
        val trWidth = getMCTextWidth(trText)
        mcText(secretText, 1, 1, 1f, Color.WHITE, center = false)
        mcText(trText, fullWidth-1 - trWidth, 1, 1f, Color.WHITE, center = false)
        val unknownWidth = mcTextAndWidth(unknownSecretsText, 1, 10, 1f, Color.WHITE, center = false)
        val centerX = (unknownWidth+1+(fullWidth-1-unknownWidth-brWidth)/2) - getMCTextWidth(mimicText)/2
        mcText(mimicText, centerX, 10, 1f, Color.WHITE, center = false)
        mcText(brText, fullWidth-1 - brWidth, 10, 1f, Color.WHITE, center = false)
        fullWidth to 19f
    }

    private val alternate: Boolean by  BooleanSetting("Flip Crypts and Score", default = false, description = "Flips crypts and score.").withDependency { fullHud.enabled }
    private val fullAddRemaining: Boolean by BooleanSetting("Include Remaining", default = false, description = "adds remaining to the secrets display.").withDependency { alternate && fullHud.enabled }
    private val fullRemaining: Boolean by DualSetting("Remaining Secrets", "Minimum", "Remaining", default = false, description = "Display minimum secrets or secrets until s+.").withDependency { !(fullAddRemaining && alternate) && fullHud.enabled }
    private val fullWidth: Float by NumberSetting("Width", default = 160f, min = 160f, max = 200f, increment = 1f).withDependency { fullHud.enabled }
    private val unknown: Boolean by DualSetting("Deaths", "Deaths", "Unfound", default = false, description = "Display deaths or unfound secrets. (Unknown secrets are secrets in rooms that haven't been discovered yet. May not be helpful in full party runs.)").withDependency { fullHud.enabled }
    private val fullBackground: Boolean by BooleanSetting("Hud Background", default = false, description = "Render a background behind the score info").withDependency { fullHud.enabled }
    private val fullMargin: Float by NumberSetting("Hud Margin", default = 0f, min = 0f, max = 5f, increment = 1f).withDependency { fullBackground && fullHud.enabled }
    private val fullColor: Color by ColorSetting("Hud Background Color", default = Color.DARK_GRAY.withAlpha(0.5f), true, description = "The color of the background").withDependency { fullBackground && fullHud.enabled }

    private val compactSecrets: HudElement by HudSetting("Compact Secrets", 10f, 10f, 1f, true) {
        if ((!DungeonUtils.inDungeons || (disableInBoss && DungeonUtils.inBoss)) && !it) return@HudSetting 0f to 0f
        val secretText = "§7Secrets: §b${DungeonUtils.secretCount}" +
                (if (compactAddRemaining) "§7-§d${(DungeonUtils.neededSecretsAmount - DungeonUtils.secretCount).coerceAtLeast(0)}" else "") +
                "§7-§e${if (!compactRemaining || fullAddRemaining) DungeonUtils.neededSecretsAmount else (DungeonUtils.neededSecretsAmount - DungeonUtils.secretCount).coerceAtLeast(0)}"+
                "§7-§c${DungeonUtils.totalSecrets}"
        val width = getMCTextWidth(secretText)
        if (compactSecretBackground) roundedRectangle(-compactSecretMargin, 0, width + 2 + (compactSecretMargin * 2), 9, compactSecretColor, 0, 0)
        mcText(secretText, 1, 1, 1f, Color.WHITE, center = false)
        width.toFloat() to 9f
    }

    private val compactAddRemaining: Boolean by BooleanSetting("Include remaining", default = false, description = "adds remaining to the secrets display.").withDependency { compactSecrets.enabled }
    private val compactRemaining: Boolean by DualSetting("Min Secrets", "Minimum", "Remaining", default = false, description = "Display minimum secrets or secrets until s+.").withDependency { !compactAddRemaining && compactSecrets.enabled }
    private val compactSecretBackground: Boolean by BooleanSetting("Secret Background", default = false, description = "Render a background behind the score info").withDependency { compactSecrets.enabled }
    private val compactSecretMargin: Float by NumberSetting("Secret Margin", default = 0f, min = 0f, max = 5f, increment = 1f).withDependency { compactSecretBackground && compactSecrets.enabled }
    private val compactSecretColor: Color by ColorSetting("Secret Background Color", default = Color.DARK_GRAY.withAlpha(0.5f), true, description = "The color of the background").withDependency { compactSecretBackground && compactSecrets.enabled }

    private val compactScore: HudElement by HudSetting("Compact Score", 10f, 10f, 1f, true) {
        if ((!DungeonUtils.inDungeons || (disableInBoss && DungeonUtils.inBoss)) && !it) return@HudSetting 0f to 0f
        val scoreText = "§7Score: ${colorizeScore(DungeonUtils.score)}" + if (!DungeonUtils.mimicKilled) " §7(§6+2?§7)" else ""
        val width = getMCTextWidth(scoreText)
        if (compactScoreBackground) roundedRectangle(-compactScoreMargin, 0, width + 2 + (compactScoreMargin * 2), 9, compactScoreColor, 0, 0)
        mcText(scoreText, 1, 1, 1f, Color.WHITE, center = false)
        width.toFloat() to 9f
    }

    private val compactScoreBackground: Boolean by BooleanSetting("Score Background", default = false, description = "Render a background behind the score info").withDependency { compactScore.enabled }
    private val compactScoreMargin: Float by NumberSetting("Score Margin", default = 0f, min = 0f, max = 5f, increment = 1f).withDependency { compactScoreBackground && compactScore.enabled }
    private val compactScoreColor: Color by ColorSetting("Score Background Color", default = Color.DARK_GRAY.withAlpha(0.5f), true, description = "The color of the background").withDependency { compactScoreBackground && compactScore.enabled }

    var shownTitle = false

    init {
        execute(250) {
            if (DungeonUtils.score < 300 || shownTitle || !scoreTitle || !DungeonUtils.inDungeons) return@execute
            PlayerUtils.alert(scoreText.replace("&", "§"))
            shownTitle = true
        }
    }

    private fun colorizeCrypts(count: Int): String {
        return when {
            count < 3 -> "§c${count}"
            count < 5 -> "§e${count}"
            else -> "§a${count}"
        }
    }

    private fun colorizeScore(score: Int): String {
        return when {
            score < 270 -> "§c${score}"
            score < 300 -> "§e${score}"
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