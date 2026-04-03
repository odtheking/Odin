package com.odtheking.odin.features.impl.dungeon

import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.OdinMod.scope
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.events.ChatPacketEvent
import com.odtheking.odin.events.TickEvent
import com.odtheking.odin.events.WorldEvent
import com.odtheking.odin.events.core.on
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.handlers.schedule
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.network.hypixelapi.RequestUtils
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent

object SecretsCounter : Module(
    name = "Secrets Counter",
    description = "Counts secrets for each player and shows results at the end of a dungeon run."
) {
    private val secretsEnabled by BooleanSetting("Secrets Counter", true, desc = "Track and display secrets found per player.")

    private val secretsBaseline = mutableMapOf<String, Long>()
    private var snapshotDone = false

    private val dungeonEndRegex = Regex("^\\s*(?:Master Mode )?(?:The )?Catacombs - (?:Entrance|Floor .{1,3})$")

    init {
        on<WorldEvent.Load> {
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
            if (dungeonEndRegex.containsMatchIn(value)) {
                schedule(30) { fetchAndDisplay() }
            }
        }
    }

    private fun fetchAndDisplay() {
        val teammates = DungeonUtils.dungeonTeammates.toList().ifEmpty { return }

        if (!secretsEnabled) return

        scope.launch(Dispatchers.IO) {
            val secretsDelta = mutableMapOf<String, Long?>()
            for (player in teammates) {
                val baseline = secretsBaseline[player.name]
                RequestUtils.pullSecrets(player.name).onSuccess { newSecrets ->
                    secretsDelta[player.name] = if (baseline != null)
                        (newSecrets - baseline).coerceAtLeast(0L)
                    else
                        null
                }
            }
            mc.execute { display(teammates, secretsDelta) }
        }
    }

    private fun display(teammates: List<com.odtheking.odin.utils.skyblock.dungeon.DungeonPlayer>, secretsDelta: Map<String, Long?>) {
        val selfName = mc.player?.name?.string ?: ""
        modMessage("§7===========§bSecrets Counter§7===========", prefix = "")
        teammates.forEach { player ->
            val count = if (player.name in secretsDelta) secretsDelta[player.name]?.toString() ?: "N/A" else "N/A"
            val line = Component.literal("§d${player.name} §7-> §f$count")
            if (player.name != selfName) {
                line.append(
                    Component.literal(" §c[kick]").withStyle { style ->
                        style
                            .withClickEvent(ClickEvent.RunCommand("/party kick ${player.name}"))
                            .withHoverEvent(HoverEvent.ShowText(
                                Component.literal("§eClick to kick §d${player.name} §efrom the party.")
                            ))
                    }
                )
            }
            modMessage(line)
        }
        modMessage("§7=====================================", prefix = "")
    }
}
