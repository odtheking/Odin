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
import org.luaj.vm2.ast.Str

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
            mcText(secretTxt, 1, 1, 1f, Color.WHITE, center = false)
            mcText(unknownSecretTxt, 159 - getMCTextWidth(unknownSecretTxt), 1, 1f, Color.WHITE, center = false)
            mcText(mimicTxt, 1, 9, 1f, Color.WHITE, center = false)
            mcText(cryptTxt, 159 - getMCTextWidth(cryptTxt), 9, 1f, Color.WHITE, center = false)
        } else return@HudSetting 0f to 0f
        160f to 20f
    }

    private inline val secretTxt: String get() =
        "§7Secrets: ${colorizeSecrets(DungeonUtils.secretCount, DungeonUtils.neededSecretsAmount)}§7-§e${DungeonUtils.neededSecretsAmount}§7-§c${DungeonUtils.totalSecrets}"

    private inline val unknownSecretTxt: String get() =
        "§7Unknown: §b${DungeonUtils.totalSecrets - DungeonUtils.knownSecrets}"

    private val mimicTxt: String get() =
        "§7Mimic: ${if (DungeonUtils.mimicKilled) "§a✔" else "§c✘"}"

    private val cryptTxt: String get() =
        "§7Crypts: ${colorizeCrypts(DungeonUtils.cryptCount)}"

    private fun colorizeCrypts(count: Int): String {
        return when {
            count < 3 -> "§c${count}"
            count <5 -> "§e${count}"
            else -> "§a${count}"
        }
    }

    private fun colorizeSecrets(count: Int, max: Int): String {
        return when {
            count == 0 -> "§70"
            count <= max * 0.5 -> "§c${count}"
            count <= max * 0.99 -> "§e${count}"
            else -> "§a${count}"
        }
    }
}