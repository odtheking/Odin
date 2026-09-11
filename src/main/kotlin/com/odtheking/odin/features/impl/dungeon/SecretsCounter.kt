package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.OdinMod.scope
import com.odtheking.odin.events.LevelEvent
import com.odtheking.odin.events.MessageEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.handlers.schedule
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.network.hypixelapi.RequestUtils
import com.odtheking.odin.utils.skyblock.dungeon.DungeonPlayer
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import kotlinx.coroutines.launch

object SecretsCounter : Module(
    name = "Secrets Counter",
    description = "Counts secrets for each player and shows results at the end of a dungeon run."
) {
    private val secretsBaseline = mutableMapOf<String, Long>()
    private var snapshotDone = false

    private val dungeonStartRegex = Regex("^\\[NPC] Mort: Good luck\\.$")
    private val dungeonEndRegex = Regex("^\\s*(?:Master Mode )?(?:The )?Catacombs - (?:Entrance|Floor .{1,3})$")

    init {
        on<LevelEvent.Load> {
            secretsBaseline.clear()
            snapshotDone = false
        }

        on<MessageEvent.Chat> {
            if (dungeonEndRegex.containsMatchIn(message)) schedule(30) { fetchAndDisplay() }

            if (!dungeonStartRegex.containsMatchIn(message) || snapshotDone) return@on
            val teammates = DungeonUtils.dungeonTeammatesNoSelf.toList().ifEmpty { return@on }
            snapshotDone = true
            scope.launch {
                for ((teammateName) in teammates) {
                    RequestUtils.pullSecrets(teammateName).onSuccess { secrets ->
                        secretsBaseline[teammateName] = secrets
                    }
                }
            }
        }
    }

    private fun fetchAndDisplay() {
        val teammates = DungeonUtils.dungeonTeammatesNoSelf.toList().ifEmpty { return }
        val secretsDelta = mutableMapOf<String, Long?>()

        scope.launch {
            for ((teammateName) in teammates) {
                val baseline = secretsBaseline[teammateName]
                RequestUtils.pullSecrets(teammateName).onSuccess { newSecrets ->
                    secretsDelta[teammateName] = if (baseline != null) (newSecrets - baseline).coerceAtLeast(0L)
                    else null
                }
            }
            display(teammates, secretsDelta)
        }
    }

    private fun display(teammates: List<DungeonPlayer>, secretsDelta: Map<String, Long?>) {
        teammates
            .sortedWith(compareBy<DungeonPlayer> { it.clazz.ordinal }.thenByDescending { secretsDelta[it.name] ?: -1L })
            .forEach { player ->
            val count = if (player.name in secretsDelta) secretsDelta[player.name]?.toString() ?: "N/A" else "N/A"
                modMessage("§${player.clazz.colorCode}${player.name} §7-> §f${count} Secrets")
            }
    }
}