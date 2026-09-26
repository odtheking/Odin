package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.*
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.RenderExtractEvent
import com.odtheking.odin.events.RoomEnterEvent
import com.odtheking.odin.events.ScoreUpdateEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.features.impl.dungeon.map.tile.RoomType
import com.odtheking.odin.utils.Color.Companion.withAlpha
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.alert
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.render.drawFilledBox
import com.odtheking.odin.utils.render.getStringWidth
import com.odtheking.odin.utils.render.text
import com.odtheking.odin.utils.render.textDim
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB

object MapInfo : Module(
    name = "Map Info",
    description = "Displays stats about the dungeon such as score, secrets, and deaths. \nRequires \"Mimic\" enabled to be accurate"
) {
    private val printWhenScore by BooleanSetting("300 Score Alert", true, desc = "Sends elapsed time in chat when 300 score is reached and a title.")
    private val highlightPortal by BooleanSetting("Highlight Portal", true, desc = "Highlights the blood room portal when 300 score is reached.")
    private val disableInBoss by BooleanSetting("Disable in boss", true, desc = "Disables the information display when you're in boss.")

    enum class SecretCount { MINIMUM, REMAINING }
    enum class DeathDisplay { DEATHS, UNFOUND }

    private val roomSecrets by HUD("Room Secrets", "Displays the number of secrets in the current room.") {
        if ((!DungeonUtils.inClear) && !it) return@HUD 0 to 0

        val secrets = if (it) 0 to 2 else DungeonUtils.currentRoom?.foundSecrets?.let { found ->
            DungeonUtils.currentRoom?.data?.maxSecrets?.let { max ->
                if (max == 0) return@HUD 0 to 0
                found to max
            }
        } ?: return@HUD 0 to 0
        val color = when {
            secrets.first * 2 < secrets.second -> "§c"
            secrets.first * 4 < secrets.second * 3 -> "§e"
            else -> "§a"
        }
        val roomText = buildString {
            append(color)
            append(secrets.first)
            append("§7/")
            append(color)
            append(secrets.second)
        }

        textDim(roomText, 0, 0, Colors.WHITE)
    }

    private val fullHud: HudElement by HUD("Full Hud", "Displays a full hud with score, secrets, crypts, and mimic info.") {
        if ((!DungeonUtils.inDungeons || (disableInBoss && DungeonUtils.inBoss)) && !it) return@HUD 0 to 0

        val scoreText = buildString {
            append("§7Score: ")
            append(colorizeScore(DungeonUtils.score))
        }

        val secretText = buildString {
            append("§7Secrets: §b")
            append(DungeonUtils.secretCount)
            if (fullAddRemaining) {
                append("§7-§d")
                append((DungeonUtils.neededSecretsAmount - DungeonUtils.secretCount).coerceAtLeast(0))
            }
            append("§7-§e")
            append(if (fullRemaining != SecretCount.MINIMUM || fullAddRemaining) DungeonUtils.neededSecretsAmount else (DungeonUtils.neededSecretsAmount - DungeonUtils.secretCount).coerceAtLeast(0))
            append("§7-§c")
            append(DungeonUtils.totalSecrets)
        }

        val unknownSecretsText = if (unknown == DeathDisplay.DEATHS) {
            buildString {
                append("§7Deaths: §c")
                append(colorizeDeaths(DungeonUtils.deathCount))
            }
        } else {
            buildString {
                append("§7Unfound: §e")
                append((DungeonUtils.totalSecrets - DungeonUtils.knownSecrets).coerceAtLeast(0))
            }
        }

        val mimicText = buildString {
            if (DungeonUtils.isFloor(6, 7)) append("${if (DungeonUtils.mimicKilled) "§a" else "§c"}M §8| ")
            append("${if (DungeonUtils.princeKilled) "§a" else "§c"}P §8| ")
            append("${if (DungeonUtils.batKilled) "§a" else "§c"}B")
        }

        val cryptText = buildString {
            append("§7Crypts: ")
            append(colorizeCrypts(DungeonUtils.cryptCount.coerceAtMost(5)))
        }

        val brWidth = getStringWidth(scoreText)
        val trWidth = getStringWidth(cryptText)
        val mimicWidth = getStringWidth(mimicText)

        fill(0, 0, fullWidth, 19, fullColor.rgba)

        text(secretText, 0, 0, Colors.WHITE)
        text(cryptText , fullWidth - 1 - trWidth, 1, Colors.WHITE)
        val unknownWidth = textDim(unknownSecretsText, 1, 10, Colors.WHITE).first
        text(mimicText, (unknownWidth + 1 + (fullWidth - 1 - unknownWidth - brWidth) / 2) - mimicWidth / 2, 10, Colors.WHITE)
        text(scoreText, fullWidth - 1 - brWidth, 10, Colors.WHITE)
        fullWidth to 18
    }

    private val fullAddRemaining by BooleanSetting("Include Remaining", false, desc = "Adds remaining to the secrets display.").withDependency { fullHud.enabled }
    private val fullRemaining by SelectorSetting("Remaining Secrets", SecretCount.MINIMUM, desc = "Display minimum secrets or secrets until s+.").withDependency { !fullAddRemaining && fullHud.enabled }
    private val fullWidth by NumberSetting("Width", 160, 160..200, 1, desc = "The width of the hud.").withDependency { fullHud.enabled }
    private val unknown by SelectorSetting("Deaths", DeathDisplay.DEATHS, desc = "Display deaths or unfound secrets. (Unknown secrets are secrets in rooms that haven't been discovered yet. May not be helpful in full party runs.)").withDependency { fullHud.enabled }
    private val fullColor by ColorSetting("Background Color", Colors.MINECRAFT_DARK_GRAY.withAlpha(0f), true, desc = "The color of the background.").withDependency { fullHud.enabled }

    private var portalAABB: AABB? = null
    private var shownTitle = false

    init {
        on<ScoreUpdateEvent> {
            if (shownTitle || !DungeonUtils.inDungeons || !printWhenScore || score < 300) return@on
            shownTitle = true
            alert("§c300 Score!")
            modMessage("§b${DungeonUtils.score} §ascore reached in §6${DungeonUtils.dungeonTime} §8|| §e${DungeonUtils.floor?.name}§8.")
        }

        on<RoomEnterEvent> {
            if (room?.type == RoomType.BLOOD)
                portalAABB = AABB.encapsulatingFullBlocks(room.getRealCoords(BlockPos(16, 69, 29)), room.getRealCoords(BlockPos(14, 69, 29))).inflate(0.0, 4.0, 0.0)
        }

        on<RenderExtractEvent> {
            if (!highlightPortal || !DungeonUtils.inClear || DungeonUtils.score < 300) return@on
            portalAABB?.let { pos ->
                drawFilledBox(pos, Colors.MINECRAFT_GREEN.withAlpha(0.5f), depth = true)
            }
        }

        on<LevelEvent.Load> {
            shownTitle = false
            portalAABB = null
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