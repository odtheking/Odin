package me.odinmain.features.impl.dungeon

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.DualSetting
import me.odinmain.features.settings.impl.HudSetting
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
    val hud: HudElement by HudSetting("Hud", 10f, 10f, 1f, false) {
        if (it) {
            mcText("§7Secrets: §c1§7-§e42§7-§c50", 1, 1, 1f, Color.WHITE, center = false)
            mcText("§7Unknown: §b12", 159 - getMCTextWidth("§7Unknown: §b12"), 1, 1f, Color.WHITE, center = false)
            mcText("§7Mimic: §a✔", 1, 9, 1f, Color.WHITE, center = false)
            mcText("§7Crypts: §e4", 159 - getMCTextWidth("§7Crypts: §a4"), 9, 1f, Color.WHITE, center = false)
        } else if (DungeonUtils.inDungeons){
            mcText(secretText, 1, 1, 1f, Color.WHITE, center = false)
            mcText(unknownSecretsText, 159 - getMCTextWidth(unknownSecretsText), 1, 1f, Color.WHITE, center = false)
            mcText(mimicText, 1, 9, 1f, Color.WHITE, center = false)
            mcText(cryptText, 159 - getMCTextWidth(cryptText), 9, 1f, Color.WHITE, center = false)
        } else return@HudSetting 0f to 0f
        160f to 10f
    }

    private val secretText = "§7Secrets: ${colorizeSecrets(DungeonUtils.secretCount, DungeonUtils.minSecrets)}§7-§e${DungeonUtils.minSecrets}§7-§c${DungeonUtils.totalSecrets}"
    private val unknownSecretsText = "§7Unknown: §b${DungeonUtils.totalSecrets - DungeonUtils.knownSecrets}"
    private val mimicText = if (DungeonUtils.mimicKilled) "§7Mimic: §a✔" else "§7Mimic: §c✘"
    private val cryptText = "§7Crypts: ${colorizeCrypts(DungeonUtils.cryptCount)}"

    private fun colorizeCrypts(count: Int): String {
        return when {
            count < 3 -> "§c${count}"
            count <5 -> "§e${count}"
            else -> "§a${count}"
        }
    }

    private fun colorizeSecrets(count: Int, max: Int): String {
        return when {
            count == 0 -> "§7${count}"
            count <= max * 0.5 -> "§c${count}"
            count <= max * 0.99 -> "§e${count}"
            else -> "§a${count}"
        }
    }
}