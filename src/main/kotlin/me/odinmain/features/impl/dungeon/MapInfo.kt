package me.odinmain.features.impl.dungeon

import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.render.getMCTextWidth
import me.odinmain.utils.render.mcTextAndWidth
import me.odinmain.utils.render.roundedRectangle
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.ui.Colors
import me.odinmain.utils.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.ui.hud.HudElement

object MapInfo : Module(
    name = "Map Info",
    desc = "Displays stats about the dungeon such as score, secrets, and deaths."
) {
    private val disableInBoss by BooleanSetting("Disable in boss", true, desc = "Disables the information display when you're in boss.")
    private val scoreTitle by BooleanSetting("300 Score Title", true, desc = "Displays a title on 300 score.")
    private val scoreText by StringSetting("Title Text", "&c300 Score!", desc = "Text to be displayed on 300 score.").withDependency { scoreTitle }
    private val printWhenScore by BooleanSetting("Print Score Time", true, desc = "Sends elapsed time in chat when 300 score is reached.")
    val togglePaul by SelectorSetting("Paul Settings", "Automatic", options = arrayListOf("Automatic", "Force Disable", "Force Enable"), desc = "Toggle Paul's settings.")

    private val fullHud: HudElement by HudSetting("Full Hud", 10f, 10f, 1f, true) {
        if ((!DungeonUtils.inDungeons || (disableInBoss && DungeonUtils.inBoss)) && !it) return@HudSetting 0f to 0f

        val scoreText = "§7Score: ${colorizeScore(DungeonUtils.score)}"
        val secretText = "§7Secrets: §b${DungeonUtils.secretCount}" +
                (if (fullAddRemaining && alternate) "§7-§d${(DungeonUtils.neededSecretsAmount - DungeonUtils.secretCount).coerceAtLeast(0)}" else "") +
                "§7-§e${if (fullRemaining != 0 || (fullAddRemaining && alternate)) DungeonUtils.neededSecretsAmount else (DungeonUtils.neededSecretsAmount - DungeonUtils.secretCount).coerceAtLeast(0)}"+
                "§7-§c${DungeonUtils.totalSecrets}"
        val unknownSecretsText = if (unknown == 0) "§7Deaths: §c${colorizeDeaths(DungeonUtils.deathCount)}" else "§7Unfound: §e${(DungeonUtils.totalSecrets - DungeonUtils.knownSecrets).coerceAtLeast(0)}"
        val mimicText = "§7Mimic: ${if (DungeonUtils.mimicKilled) "§a✔" else "§c✘"}"
        val cryptText = "§7Crypts: ${colorizeCrypts(DungeonUtils.cryptCount.coerceAtMost(5))}"

        val (trText, brText) = if (alternate) listOf(cryptText, scoreText) else listOf(scoreText, cryptText)

        if (fullBackground) roundedRectangle(-fullMargin, 0, fullWidth + (fullMargin * 2), 19, fullColor, 0, 0)
        val brWidth = getMCTextWidth(brText)
        val trWidth = getMCTextWidth(trText)
        RenderUtils.drawText(secretText, 1f, 1f, 1f, Colors.WHITE, center = false)
        RenderUtils.drawText(trText, fullWidth - 1f - trWidth, 1f, 1f, Colors.WHITE, center = false)
        val unknownWidth = mcTextAndWidth(unknownSecretsText, 1, 10, 1f, Colors.WHITE, center = false)
        val centerX = (unknownWidth + 1 + (fullWidth - 1 - unknownWidth - brWidth) / 2) - getMCTextWidth(mimicText) / 2
        RenderUtils.drawText(mimicText, centerX, 10f, 1f, Colors.WHITE, center = false)
        RenderUtils.drawText(brText, fullWidth - 1 - brWidth, 10f, 1f, Colors.WHITE, center = false)
        fullWidth to 19f
    }

    private val alternate by  BooleanSetting("Flip Crypts and Score", false, desc = "Flips crypts and score.").withDependency { fullHud.enabled }
    private val fullAddRemaining by BooleanSetting("Include Remaining", false, desc = "Adds remaining to the secrets display.").withDependency { alternate && fullHud.enabled }
    private val fullRemaining by SelectorSetting("Remaining Secrets", "Minimum", options = arrayListOf("Minimum", "Remaining"), desc = "Display minimum secrets or secrets until s+.").withDependency { !(fullAddRemaining && alternate) && fullHud.enabled }
    private val fullWidth by NumberSetting("Width", 160f, min = 160f, max = 200f, increment = 1f, desc = "The width of the hud.").withDependency { fullHud.enabled }
    private val unknown by SelectorSetting("Deaths", "Deaths", options = arrayListOf("Deaths", "Unfound"), desc = "Display deaths or unfound secrets. (Unknown secrets are secrets in rooms that haven't been discovered yet. May not be helpful in full party runs.)").withDependency { fullHud.enabled }
    private val fullBackground by BooleanSetting("Hud Background", false, desc = "Render a background behind the score info.").withDependency { fullHud.enabled }
    private val fullMargin by NumberSetting("Hud Margin", 0f, min = 0f, max = 5f, increment = 1f, desc = "The margin around the hud.").withDependency { fullBackground && fullHud.enabled }
    private val fullColor by ColorSetting("Hud Background Color", Colors.MINECRAFT_DARK_GRAY.withAlpha(0.5f), true, desc = "The color of the background.").withDependency { fullBackground && fullHud.enabled }

    private val compactSecrets: HudElement by HudSetting("Compact Secrets", 10f, 10f, 1f, true) {
        if ((!DungeonUtils.inDungeons || (disableInBoss && DungeonUtils.inBoss)) && !it) return@HudSetting 0f to 0f
        val secretText = "§7Secrets: §b${DungeonUtils.secretCount}" +
                (if (compactAddRemaining) "§7-§d${(DungeonUtils.neededSecretsAmount - DungeonUtils.secretCount).coerceAtLeast(0)}" else "") +
                "§7-§e${if (compactRemaining == 0 || fullAddRemaining) DungeonUtils.neededSecretsAmount else (DungeonUtils.neededSecretsAmount - DungeonUtils.secretCount).coerceAtLeast(0)}"+
                "§7-§c${DungeonUtils.totalSecrets}"
        val width = getMCTextWidth(secretText)
        if (compactSecretBackground) roundedRectangle(-compactSecretMargin, 0, width + 2 + (compactSecretMargin * 2), 9, compactSecretColor, 0, 0)
        RenderUtils.drawText(secretText, 1f, 1f, 1f, Colors.WHITE, center = false)
        width.toFloat() to 9f
    }

    private val compactAddRemaining by BooleanSetting("Compact Include remaining", false, desc = "Adds remaining to the secrets display.").withDependency { compactSecrets.enabled }
    private val compactRemaining by SelectorSetting("Min Secrets", "Minimum", options = arrayListOf("Minimum", "Remaining"), desc = "Display minimum secrets or secrets until s+.").withDependency { !compactAddRemaining && compactSecrets.enabled }
    private val compactSecretBackground by BooleanSetting("Secret Background", false, desc = "Render a background behind the score info.").withDependency { compactSecrets.enabled }
    private val compactSecretMargin by NumberSetting("Secret Margin", 0f, min = 0f, max = 5f, increment = 1f, desc = "The margin around the hud.").withDependency { compactSecretBackground && compactSecrets.enabled }
    private val compactSecretColor by ColorSetting("Secret Background Color", Colors.MINECRAFT_DARK_GRAY.withAlpha(0.5f), true, desc = "The color of the background.").withDependency { compactSecretBackground && compactSecrets.enabled }

    private val compactScore: HudElement by HudSetting("Compact Score", 10f, 10f, 1f, true) {
        if ((!DungeonUtils.inDungeons || (disableInBoss && DungeonUtils.inBoss)) && !it) return@HudSetting 0f to 0f
        val scoreText = "§7Score: ${colorizeScore(DungeonUtils.score)}" + if (!DungeonUtils.mimicKilled) " §7(§6+2?§7)" else ""
        val width = getMCTextWidth(scoreText)
        if (compactScoreBackground) roundedRectangle(-compactScoreMargin, 0, width + 2 + (compactScoreMargin * 2), 9, compactScoreColor, 0, 0)
        RenderUtils.drawText(scoreText, 1f, 1f, 1f, Colors.WHITE, center = false)
        width.toFloat() to 9f
    }

    private val compactScoreBackground by BooleanSetting("Score Background", false, desc = "Render a background behind the score info.").withDependency { compactScore.enabled }
    private val compactScoreMargin by NumberSetting("Score Margin", 0f, min = 0f, max = 5f, increment = 1f, desc = "The margin around the hud.").withDependency { compactScoreBackground && compactScore.enabled }
    private val compactScoreColor by ColorSetting("Score Background Color", Colors.MINECRAFT_DARK_GRAY.withAlpha(0.5f), true, desc = "The color of the background.").withDependency { compactScoreBackground && compactScore.enabled }

    var shownTitle = false

    init {
        execute(250) {
            if (!DungeonUtils.inDungeons || shownTitle || (!scoreTitle && !printWhenScore) || DungeonUtils.score < 300) return@execute
            if (scoreTitle) PlayerUtils.alert(scoreText.replace("&", "§"))
            if (printWhenScore) modMessage("§b${DungeonUtils.score} §ascore reached in §6${DungeonUtils.dungeonTime} || ${DungeonUtils.floor?.name}.")
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
        val floor = DungeonUtils.floor?.floorNumber ?: 0
        return when {
            count == 0 -> "§a0"
            count <= if (floor < 6) 2 else 3 -> "§e${count}"
            count == if (floor < 6) 3 else 4 -> "§c${count}"
            else -> "§4${count}"
        }
    }
}