package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.*
import com.odtheking.odin.events.OverlayPacketEvent
import com.odtheking.odin.events.RenderEvent
import com.odtheking.odin.events.RoomEnterEvent
import com.odtheking.odin.events.WorldEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.alert
import com.odtheking.odin.utils.handlers.TickTask
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.render.drawFilledBox
import com.odtheking.odin.utils.render.getStringWidth
import com.odtheking.odin.utils.render.text
import com.odtheking.odin.utils.render.textDim
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils.getRealCoords
import com.odtheking.odin.utils.skyblock.dungeon.tiles.RoomType
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB

object MapInfo : Module(
    name = "Map Info",
    description = "Displays stats about the dungeon such as score, secrets, and deaths. \nRequires \"Mimic\" enabled to be accurate"
) {
    private val highlightPortal by BooleanSetting("Highlight Portal", true, desc = "Highlights the blood room portal when 300 score is reached.")
    private val disableInBoss by BooleanSetting("Disable in boss", true, desc = "Disables the information display when you're in boss.")
    private val scoreTitle by BooleanSetting("300 Score Title", true, desc = "Displays a title on 300 score.")
    private val printWhenScore by BooleanSetting("Print Score Time", true, desc = "Sends elapsed time in chat when 300 score is reached.")
    val togglePaul by SelectorSetting("Paul Settings", "Automatic", arrayListOf("Automatic", "Force Disable", "Force Enable"), desc = "Toggle Paul's settings.")

    private var cachedScore = 0
    private var cachedSecretCount = 0
    private var cachedNeededSecrets = 0
    private var cachedTotalSecrets = 0
    private var cachedKnownSecrets = 0
    private var cachedMimicKilled = false
    private var cachedPrinceKilled = false
    private var cachedCryptCount = 0
    private var cachedDeathCount = 0

    private val fullHud: HudElement by HUD("Full Hud", "Displays a full hud with score, secrets, crypts, and mimic info.") {
        if ((!DungeonUtils.inDungeons || (disableInBoss && DungeonUtils.inBoss)) && !it) return@HUD 0 to 0

        val score = cachedScore
        val secretCount = cachedSecretCount
        val neededSecrets = cachedNeededSecrets
        val totalSecrets = cachedTotalSecrets
        val mimicKilled = cachedMimicKilled
        val princeKilled = cachedPrinceKilled
        val cryptCount = cachedCryptCount

        val showRemaining = fullAddRemaining && alternate
        val useNeededSecrets = fullRemaining != 0 || showRemaining

        val scoreText = buildString {
            append("§7Score: ")
            append(colorizeScore(score))
        }

        val secretText = buildString {
            append("§7Secrets: §b")
            append(secretCount)
            if (showRemaining) {
                append("§7-§d")
                append((neededSecrets - secretCount).coerceAtLeast(0))
            }
            append("§7-§e")
            append(if (useNeededSecrets) neededSecrets else (neededSecrets - secretCount).coerceAtLeast(0))
            append("§7-§c")
            append(totalSecrets)
        }

        val unknownSecretsText = if (unknown == 0) {
            buildString {
                append("§7Deaths: §c")
                append(colorizeDeaths(cachedDeathCount))
            }
        } else {
            buildString {
                append("§7Unfound: §e")
                append((totalSecrets - cachedKnownSecrets).coerceAtLeast(0))
            }
        }

        val mimicText = buildString {
            append("§7M: ")
            append(if (mimicKilled) "§a✔" else "§c✘")
            append(" §8| §7P: ")
            append(if (princeKilled) "§a✔" else "§c✘")
        }

        val cryptText = buildString {
            append("§7Crypts: ")
            append(colorizeCrypts(cryptCount))
        }

        val trText = if (alternate) cryptText else scoreText
        val brText = if (alternate) scoreText else cryptText

        val hudWidth = fullWidth
        val brWidth = getStringWidth(brText)
        val trWidth = getStringWidth(trText)
        val mimicWidth = getStringWidth(mimicText)

        if (fullBackground) {
            val margin = fullMargin
            fill((-margin).toInt(), 0, (hudWidth + (margin * 2)).toInt(), 19, fullColor.rgba)
        }

        text(secretText, 0, 0, Colors.WHITE)
        text(trText, hudWidth - 1 - trWidth, 1, Colors.WHITE)
        val unknownWidth = textDim(unknownSecretsText, 1, 10, Colors.WHITE).first
        val centerX = (unknownWidth + 1 + (hudWidth - 1 - unknownWidth - brWidth) / 2) - mimicWidth / 2
        text(mimicText, centerX, 10, Colors.WHITE)
        text(brText, hudWidth - 1 - brWidth, 10, Colors.WHITE)
        hudWidth to 18
    }

    private val alternate by BooleanSetting("Flip Crypts and Score", false, desc = "Flips crypts and score.").withDependency { fullHud.enabled }
    private val fullAddRemaining by BooleanSetting("Include Remaining", false, desc = "Adds remaining to the secrets display.").withDependency { alternate && fullHud.enabled }
    private val fullRemaining by SelectorSetting("Remaining Secrets", "Minimum", options = arrayListOf("Minimum", "Remaining"), desc = "Display minimum secrets or secrets until s+.").withDependency { !(fullAddRemaining && alternate) && fullHud.enabled }
    private val fullWidth by NumberSetting("Width", 160, 160, 200, 1, desc = "The width of the hud.").withDependency { fullHud.enabled }
    private val unknown by SelectorSetting("Deaths", "Deaths", arrayListOf("Deaths", "Unfound"), desc = "Display deaths or unfound secrets. (Unknown secrets are secrets in rooms that haven't been discovered yet. May not be helpful in full party runs.)").withDependency { fullHud.enabled }
    private val fullBackground by BooleanSetting("Hud Background", false, desc = "Render a background behind the score info.").withDependency { fullHud.enabled }
    private val fullMargin by NumberSetting("Hud Margin", 0f, 0f, 5f, 1f, desc = "The margin around the hud.").withDependency { fullBackground && fullHud.enabled }
    private val fullColor by ColorSetting("Hud Background Color", Colors.MINECRAFT_DARK_GRAY.withAlpha(0.5f), true, desc = "The color of the background.").withDependency { fullBackground && fullHud.enabled }

    private val compactSecrets: HudElement by HUD("Compact Secrets", "Displays a compact secrets hud with score and secrets.") {
        if ((!DungeonUtils.inDungeons || (disableInBoss && DungeonUtils.inBoss)) && !it) return@HUD 0 to 0

        val secretCount = cachedSecretCount
        val neededSecrets = cachedNeededSecrets
        val totalSecrets = cachedTotalSecrets

        val secretText = buildString {
            append("§7Secrets: §b")
            append(secretCount)
            if (compactAddRemaining) {
                append("§7-§d")
                append((neededSecrets - secretCount).coerceAtLeast(0))
            }
            append("§7-§e")
            append(if (compactRemaining == 0 || fullAddRemaining) neededSecrets else (neededSecrets - secretCount).coerceAtLeast(0))
            append("§7-§c")
            append(totalSecrets)
        }

        val width = getStringWidth(secretText)

        if (compactSecretBackground) {
            val margin = compactSecretMargin
            fill((-margin).toInt(), 0, (width + 2 + (margin * 2)).toInt(), 9, compactSecretColor.rgba)
        }
        text(secretText, 0, 0, Colors.WHITE)
        width to 9
    }

    private val compactAddRemaining by BooleanSetting("Compact Include remaining", false, desc = "Adds remaining to the secrets display.").withDependency { compactSecrets.enabled }
    private val compactRemaining by SelectorSetting("Min Secrets", "Minimum", options = arrayListOf("Minimum", "Remaining"), desc = "Display minimum secrets or secrets until s+.").withDependency { !compactAddRemaining && compactSecrets.enabled }
    private val compactSecretBackground by BooleanSetting("Secret Background", false, desc = "Render a background behind the score info.").withDependency { compactSecrets.enabled }
    private val compactSecretMargin by NumberSetting("Secret Margin", 0f, 0f, 5f, 1f, desc = "The margin around the hud.").withDependency { compactSecretBackground && compactSecrets.enabled }
    private val compactSecretColor by ColorSetting("Secret Background Color", Colors.MINECRAFT_DARK_GRAY.withAlpha(0.5f), true, desc = "The color of the background.").withDependency { compactSecretBackground && compactSecrets.enabled }

    private val compactScore: HudElement by HUD("Compact Score", "Displays a compact score hud with score info.") {
        if ((!DungeonUtils.inDungeons || (disableInBoss && DungeonUtils.inBoss)) && !it) return@HUD 0 to 0

        val score = cachedScore
        val mimicKilled = cachedMimicKilled
        val princeKilled = cachedPrinceKilled

        val missing = (if (mimicKilled) 0 else 2) + (if (princeKilled) 0 else 1)
        val scoreText = buildString {
            append("§7Score: ")
            append(colorizeScore(score))
            if (missing > 0) {
                append(" §7(§6+")
                append(missing)
                append("?§7)")
            }
        }

        val width = getStringWidth(scoreText)
        if (compactScoreBackground) {
            val margin = compactScoreMargin
            fill((-margin).toInt(), 0, (width + 2 + (margin * 2)).toInt(), 9, compactScoreColor.rgba)
        }
        text(scoreText, 0, 0, Colors.WHITE)
        width to 9
    }

    private val compactScoreBackground by BooleanSetting("Score Background", false, desc = "Render a background behind the score info.").withDependency { compactScore.enabled }
    private val compactScoreMargin by NumberSetting("Score Margin", 0f, 0f, 5f, 1f, desc = "The margin around the hud.").withDependency { compactScoreBackground && compactScore.enabled }
    private val compactScoreColor by ColorSetting("Score Background Color", Colors.MINECRAFT_DARK_GRAY.withAlpha(0.5f), true, desc = "The color of the background.").withDependency { compactScoreBackground && compactScore.enabled }

    private val roomSecrets by HUD("Room Secrets", "Displays the number of secrets in the current room.") {
        if ((!DungeonUtils.inClear) && !it) return@HUD 0 to 0

        val secrets = if (it) 0 to 2 else currentRoomSecrets ?: return@HUD 0 to 0
        val color = when {
            secrets.first * 2 < secrets.second -> "§c"
            secrets.first * 4 < secrets.second * 3 -> "§e"
            else -> "§a"
        }
        val roomText = buildString {
            append("§7\uD83D\uDDDD ")
            append(color)
            append(secrets.first)
            append("§7/")
            append(color)
            append(secrets.second)
        }

        val width = getStringWidth(roomText)
        text(roomText, 0, 0, Colors.WHITE)
        width to 9
    }

    private var portalAABB: AABB? = null
    private var currentRoomSecrets: Pair<Int, Int>? = null
    private val secretRegex = Regex("(\\d+)/(\\d+) Secrets")
    var shownTitle = false

    init {
        TickTask(1) {
            if (!enabled || !DungeonUtils.inDungeons) return@TickTask
            cachedScore = DungeonUtils.score
            cachedSecretCount = DungeonUtils.secretCount
            cachedNeededSecrets = DungeonUtils.neededSecretsAmount
            cachedTotalSecrets = DungeonUtils.totalSecrets
            cachedKnownSecrets = DungeonUtils.knownSecrets
            cachedMimicKilled = DungeonUtils.mimicKilled
            cachedPrinceKilled = DungeonUtils.princeKilled
            cachedCryptCount = DungeonUtils.cryptCount.coerceAtMost(5)
            cachedDeathCount = DungeonUtils.deathCount
        }

        TickTask(10) {
            if (!enabled || !DungeonUtils.inDungeons || shownTitle || (!scoreTitle && !printWhenScore) || DungeonUtils.score < 300) return@TickTask
            if (scoreTitle) alert("§c300 Score!")
            if (printWhenScore) modMessage("§b${DungeonUtils.score} §ascore reached in §6${DungeonUtils.dungeonTime} || ${DungeonUtils.floor?.name}.")
            shownTitle = true
        }

        on<OverlayPacketEvent> {
            secretRegex.find(value)?.destructured?.let { (found, total) ->
                currentRoomSecrets = Pair(found.toIntOrNull() ?: 0, total.toIntOrNull() ?: 0)
            }
        }

        on<RoomEnterEvent> {
            currentRoomSecrets = null
            if (room?.data?.type == RoomType.BLOOD) {
                portalAABB = AABB.encapsulatingFullBlocks(room.getRealCoords(BlockPos(16, 69, 29)), room.getRealCoords(BlockPos(14, 69, 29))).inflate(0.0, 4.0, 0.0)
            }
        }

        on<RenderEvent.Extract> {
            if (!highlightPortal || !DungeonUtils.inClear || DungeonUtils.score < 300) return@on
            portalAABB?.let{ pos ->
                drawFilledBox(pos, Colors.MINECRAFT_GREEN.withAlpha(0.5f), depth = true)
            }
        }

        on<WorldEvent.Load> {
            shownTitle = false
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