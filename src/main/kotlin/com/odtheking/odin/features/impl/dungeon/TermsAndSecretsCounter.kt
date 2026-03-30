package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.OdinMod.scope
import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.SelectorSetting
import com.odtheking.odin.clickgui.settings.impl.StringSetting
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.WorldEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.handlers.schedule
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.network.hypixelapi.RequestUtils
import com.odtheking.odin.utils.sendCommand
import com.odtheking.odin.utils.skyblock.dungeon.DungeonClass
import com.odtheking.odin.utils.skyblock.dungeon.DungeonPlayer
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent

object TermsAndSecretsCounter : Module(
    name = "Terms & Secrets Counter",
    description = "Counts terminals and secrets for each player and shows results at the end of a dungeon run."
) {
    private val secretsEnabled by BooleanSetting("Secrets Counter", true, desc = "Track and display secrets found per player.")
    private val terminalsEnabled by BooleanSetting("Terminals Counter", true, desc = "Track terminals activated per player. Only active in Floor 7 / Master Mode 7.")
    private val autoKick by BooleanSetting("Auto Kick", false, desc = "Automatically kick players who don't meet the minimum requirements.")
    private val kickMode by SelectorSetting("Kick Mode", "All", arrayListOf("All", "Class"), desc = "All: kick by shared minimums. Class: kick by per-class minimums.").withDependency { autoKick }

    // All mode
    private val minSecretsAll by StringSetting("Min Secrets", "0", length = 6, desc = "Kick players with fewer secrets than this. 0 = ignore.").withDependency { autoKick && kickMode == 0 }
    private val minTerminalsAll by StringSetting("Min Terminals", "0", length = 6, desc = "Kick players with fewer terminals than this. 0 = ignore.").withDependency { autoKick && kickMode == 0 }

    // Class mode — secrets
    private val minSecretsTank by StringSetting("Min Tank Secrets", "0", length = 6, desc = "Minimum secrets for Tank. 0 = ignore.").withDependency { autoKick && kickMode == 1 }
    private val minSecretsHealer by StringSetting("Min Healer Secrets", "0", length = 6, desc = "Minimum secrets for Healer. 0 = ignore.").withDependency { autoKick && kickMode == 1 }
    private val minSecretsArcher by StringSetting("Min Archer Secrets", "0", length = 6, desc = "Minimum secrets for Archer. 0 = ignore.").withDependency { autoKick && kickMode == 1 }
    private val minSecretsBerserk by StringSetting("Min Berserk Secrets", "0", length = 6, desc = "Minimum secrets for Berserk. 0 = ignore.").withDependency { autoKick && kickMode == 1 }
    private val minSecretsMage by StringSetting("Min Mage Secrets", "0", length = 6, desc = "Minimum secrets for Mage. 0 = ignore.").withDependency { autoKick && kickMode == 1 }

    // Class mode — terminals
    private val minTerminalsTank by StringSetting("Min Tank Terminals", "0", length = 6, desc = "Minimum terminals for Tank. 0 = ignore.").withDependency { autoKick && kickMode == 1 }
    private val minTerminalsHealer by StringSetting("Min Healer Terminals", "0", length = 6, desc = "Minimum terminals for Healer. 0 = ignore.").withDependency { autoKick && kickMode == 1 }
    private val minTerminalsArcher by StringSetting("Min Archer Terminals", "0", length = 6, desc = "Minimum terminals for Archer. 0 = ignore.").withDependency { autoKick && kickMode == 1 }
    private val minTerminalsBerserk by StringSetting("Min Berserk Terminals", "0", length = 6, desc = "Minimum terminals for Berserk. 0 = ignore.").withDependency { autoKick && kickMode == 1 }
    private val minTerminalsMage by StringSetting("Min Mage Terminals", "0", length = 6, desc = "Minimum terminals for Mage. 0 = ignore.").withDependency { autoKick && kickMode == 1 }

    private val terminalCounts = mutableMapOf<String, Int>()
    private val secretsBaseline = mutableMapOf<String, Long>()
    private var snapshotDone = false

    private val terminalRegex = Regex("^(\\w{1,16}) activated a terminal! \\(\\d+/\\d+\\)$")
    private val dungeonEndRegex = Regex("^\\s*(?:Master Mode )?(?:The )?Catacombs - (?:Entrance|Floor .{1,3})$")

    init {
        on<WorldEvent.Load> {
            terminalCounts.clear()
            secretsBaseline.clear()
            snapshotDone = false
        }

        on<TickEvent.End> {
            if (!DungeonUtils.inDungeons || snapshotDone) return@on
            val teammates = DungeonUtils.dungeonTeammates
            if (teammates.isEmpty()) return@on

            snapshotDone = true
            if (!secretsEnabled) return@on

            for (player in teammates) {
                val name = player.name
                scope.launch(Dispatchers.IO) {
                    RequestUtils.pullSecrets(name).onSuccess { secrets ->
                        mc.execute { secretsBaseline[name] = secrets }
                    }
                }
            }
        }

        on<ChatPacketEvent> {
            if (terminalsEnabled && DungeonUtils.isFloor(7)) {
                terminalRegex.find(value)?.groupValues?.get(1)?.let { playerName ->
                    terminalCounts[playerName] = (terminalCounts[playerName] ?: 0) + 1
                }
            }

            if (dungeonEndRegex.containsMatchIn(value)) {
                schedule(30) { fetchAndDisplay() }
            }
        }
    }

    private fun fetchAndDisplay() {
        val teammates = DungeonUtils.dungeonTeammates.toList().ifEmpty { return }

        if (!secretsEnabled) {
            display(teammates, emptyMap())
            return
        }

        scope.launch(Dispatchers.IO) {
            val secretsDelta = mutableMapOf<String, Long>()
            for (player in teammates) {
                RequestUtils.pullSecrets(player.name).onSuccess { newSecrets ->
                    val baseline = secretsBaseline[player.name] ?: 0L
                    secretsDelta[player.name] = (newSecrets - baseline).coerceAtLeast(0L)
                }
            }
            mc.execute { display(teammates, secretsDelta) }
        }
    }

    private fun display(teammates: List<DungeonPlayer>, secretsDelta: Map<String, Long>) {
        val selfName = mc.player?.name?.string ?: ""
        val isF7 = DungeonUtils.isFloor(7)
        val hasSecrets = secretsEnabled
        val hasTerminals = terminalsEnabled && isF7

        if (!hasSecrets && !hasTerminals) return

        modMessage("§7================§bOdin Mod§7================", prefix = "")

        if (hasSecrets) {
            modMessage("§fSecrets:", prefix = "")
            teammates.forEachIndexed { index, player ->
                val count = secretsDelta[player.name] ?: 0L
                modMessage(buildPlayerLine(index + 1, player.name, count.toString(), selfName), prefix = "")
            }
        }

        if (hasTerminals) {
            modMessage("§fTerminals:", prefix = "")
            teammates.forEachIndexed { index, player ->
                val count = terminalCounts[player.name] ?: 0
                modMessage(buildPlayerLine(index + 1, player.name, count.toString(), selfName), prefix = "")
            }
        }

        modMessage("§7========§bTerms & Secrets Counter§7=========", prefix = "")

        if (autoKick) {
            val scheduleDelay = 20
            for (player in teammates) {
                if (player.name == selfName) continue
                val secrets = secretsDelta[player.name] ?: 0L
                val terminals = terminalCounts[player.name] ?: 0
                val shouldKick = when (kickMode) {
                    0 -> {
                        val minSec = minSecretsAll.toIntOrNull() ?: 0
                        val minTerm = minTerminalsAll.toIntOrNull() ?: 0
                        (hasSecrets && minSec > 0 && secrets < minSec) ||
                        (hasTerminals && minTerm > 0 && terminals < minTerm)
                    }
                    1 -> {
                        val (minSec, minTerm) = classMinimums(player.clazz)
                        (hasSecrets && minSec > 0 && secrets < minSec) ||
                        (hasTerminals && minTerm > 0 && terminals < minTerm)
                    }
                    else -> false
                }
                if (shouldKick) {
                    schedule(scheduleDelay) { sendCommand("p kick ${player.name}") }
                }
            }
        }
    }

    private fun buildPlayerLine(index: Int, name: String, count: String, selfName: String): Component {
        val base = Component.literal("§f$index- §d$name §7-> §f$count")
        if (name == selfName) return base
        val kickText = Component.literal(" §c[CLICK HERE TO KICK]")
            .withStyle { style ->
                style
                    .withClickEvent(ClickEvent.RunCommand("/party kick $name"))
                    .withHoverEvent(HoverEvent.ShowText(
                        Component.literal("§eClick to kick §d$name §efrom the party.\n§cAre you sure?")
                    ))
            }
        return base.append(kickText)
    }

    private fun classMinimums(clazz: DungeonClass): Pair<Int, Int> {
        val secrets = when (clazz) {
            DungeonClass.Tank -> minSecretsTank.toIntOrNull() ?: 0
            DungeonClass.Healer -> minSecretsHealer.toIntOrNull() ?: 0
            DungeonClass.Archer -> minSecretsArcher.toIntOrNull() ?: 0
            DungeonClass.Berserk -> minSecretsBerserk.toIntOrNull() ?: 0
            DungeonClass.Mage -> minSecretsMage.toIntOrNull() ?: 0
            else -> 0
        }
        val terminals = when (clazz) {
            DungeonClass.Tank -> minTerminalsTank.toIntOrNull() ?: 0
            DungeonClass.Healer -> minTerminalsHealer.toIntOrNull() ?: 0
            DungeonClass.Archer -> minTerminalsArcher.toIntOrNull() ?: 0
            DungeonClass.Berserk -> minTerminalsBerserk.toIntOrNull() ?: 0
            DungeonClass.Mage -> minTerminalsMage.toIntOrNull() ?: 0
            else -> 0
        }
        return secrets to terminals
    }
}
