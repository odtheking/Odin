package com.odtheking.odin.features.impl.floor7

import com.odtheking.odin.clickgui.settings.Setting.Companion.withDependency
import com.odtheking.odin.clickgui.settings.impl.BooleanSetting
import com.odtheking.odin.clickgui.settings.impl.DropdownSetting
import com.odtheking.odin.clickgui.settings.impl.NumberSetting
import com.odtheking.odin.clickgui.settings.impl.SelectorSetting
import com.odtheking.odin.events.*
import com.odtheking.odin.events.core.on
import com.odtheking.odin.events.core.onReceive
import com.odtheking.odin.features.Module
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.PersonalBest
import com.odtheking.odin.utils.modMessage
import com.odtheking.odin.utils.render.drawText
import com.odtheking.odin.utils.render.drawTracer
import com.odtheking.odin.utils.render.drawWireFrameBox
import com.odtheking.odin.utils.render.textDim
import com.odtheking.odin.utils.skyblock.dungeon.DungeonUtils
import com.odtheking.odin.utils.skyblock.dungeon.M7Phases
import com.odtheking.odin.utils.toFixed
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket

object WitherDragons : Module(
    name = "Wither Dragons",
    description = "Tools for managing M7 dragons timers, boxes, priority, health and alerts."
) {
    private val dragonTimerDropDown by DropdownSetting("Dragon Timer Dropdown")
    private val dragonTimer by BooleanSetting("Dragon Timer", true, desc = "Displays a timer for when M7 dragons spawn.").withDependency { dragonTimerDropDown }
    private val dragonTimerStyle by SelectorSetting("Timer Style", "Milliseconds", arrayListOf("Milliseconds", "Seconds", "Ticks"), desc = "The style of the dragon timer.").withDependency { dragonTimer && dragonTimerDropDown }
    private val showSymbol by BooleanSetting("Timer Symbol", true, desc = "Displays a symbol for the timer.").withDependency { dragonTimer && dragonTimerDropDown }
    private val hud by HUD("Dragon Timer HUD", "Displays the dragon timer in the HUD.") { example ->
        if (example) textDim("§5P §a4.5s", 0, 0, Colors.WHITE)
        else {
            priorityDragon?.let { dragon ->
                if (dragon.timeToSpawn <= 0) return@HUD 0 to 0
                textDim("§${dragon.colorCode}${dragon.name.first()}: ${getDragonTimer(dragon.timeToSpawn)}", 0, 0, Colors.WHITE)
            } ?: (0 to 0)
        }
    }.withDependency { dragonTimerDropDown }

    private val dragonBoxes by BooleanSetting("Dragon Boxes", true, desc = "Displays boxes for where M7 dragons spawn.")

    private val dragonTitleDropDown by DropdownSetting("Dragon Spawn Dropdown")
    val dragonTitle by BooleanSetting("Dragon Title", true, desc = "Displays a title for spawning dragons.").withDependency { dragonTitleDropDown }
    private val dragonTracers by BooleanSetting("Dragon Tracer", false, desc = "Draws a line to spawning dragons.").withDependency { dragonTitleDropDown }

    private val dragonAlerts by DropdownSetting("Dragon Alerts Dropdown")
    private val sendNotification by BooleanSetting("Send Dragon Confirmation", true, desc = "Sends a confirmation message when a dragon dies.").withDependency { dragonAlerts }
    val sendTime by BooleanSetting("Send Dragon Time Alive", true, desc = "Sends a message when a dragon dies with the time it was alive.").withDependency { dragonAlerts }
    val sendSpawning by BooleanSetting("Send Dragon Spawning", true, desc = "Sends a message when a dragon is spawning.").withDependency { dragonAlerts }
    val sendSpawned by BooleanSetting("Send Dragon Spawned", true, desc = "Sends a message when a dragon has spawned.").withDependency { dragonAlerts }
    val sendSpray by BooleanSetting("Send Ice Sprayed", true, desc = "Sends a message when a dragon has been ice sprayed.").withDependency { dragonAlerts }

    private val dragonHealth by BooleanSetting("Dragon Health", true, desc = "Displays the health of M7 dragons.")

    private val dragonPriorityDropDown by DropdownSetting("Dragon Priority Dropdown")
    val dragonPriorityToggle by BooleanSetting("Dragon Priority", true, desc = "Displays the priority of dragons spawning.").withDependency { dragonPriorityDropDown }
    val normalPower by NumberSetting("Normal Power", 0, 0, 32, desc = "Power needed to split.").withDependency { dragonPriorityToggle && dragonPriorityDropDown }
    val easyPower by NumberSetting("Easy Power", 0, 0, 32, desc = "Power needed when its Purple and another dragon.").withDependency { dragonPriorityToggle && dragonPriorityDropDown }
    val soloDebuff by SelectorSetting("Purple Solo Debuff", "Tank", arrayListOf("Tank", "Healer"), desc = "The class that solo debuffs purple, the other class helps b/m.").withDependency { dragonPriorityToggle && dragonPriorityDropDown }
    val soloDebuffOnAll by BooleanSetting("Solo Debuff on All Splits", false, desc = "Same as Purple Solo Debuff but for all dragons (A will only have 1 debuff).").withDependency { dragonPriorityToggle && dragonPriorityDropDown }
    val paulBuff by BooleanSetting("Paul Buff", false, desc = "Multiplies the power in your run by 1.25.").withDependency { dragonPriorityToggle && dragonPriorityDropDown }

    val witherKingRegex = Regex("^\\[BOSS] Wither King: (Oh, this one hurts!|I have more of those\\.|My soul is disposable\\.)$")
    var priorityDragon: WitherDragonsEnum? = null
    var currentTick = 0L

    val dragonPBs = PersonalBest(this, "DragonPBs")

    init {
        onReceive<ClientboundLevelParticlesPacket> {
            if (DungeonUtils.getF7Phase() == M7Phases.P5) handleSpawnPacket(this)
        }

        onReceive<ClientboundSetEquipmentPacket> {
            if (DungeonUtils.getF7Phase() == M7Phases.P5) DragonCheck.dragonSprayed(this)
        }

        onReceive<ClientboundAddEntityPacket> {
            if (DungeonUtils.getF7Phase() == M7Phases.P5) DragonCheck.dragonSpawn(this)
        }

        onReceive<ClientboundSetEntityDataPacket> {
            if (DungeonUtils.getF7Phase() == M7Phases.P5) DragonCheck.dragonUpdate(this)
        }

        on<BlockUpdateEvent> {
            if (DungeonUtils.getF7Phase() == M7Phases.P5 && updated.isAir)
                WitherDragonsEnum.entries.find { it.statuePos == pos }?.setDead(false)
        }

        on<ChatPacketEvent> {
            if (DungeonUtils.getF7Phase() != M7Phases.P5 || !witherKingRegex.matches(value)) return@on
            (DragonCheck.lastDragonDeath ?: WitherDragonsEnum.entries.find { it.state != WitherDragonState.DEAD })
                ?.apply {
                    if (sendNotification) modMessage("§${colorCode}${name} dragon counts.")
                    if (state != WitherDragonState.DEAD) setDead(false)
                    DragonCheck.lastDragonDeath = null
                }
        }

        on<TickEvent.Server> {
            WitherDragonsEnum.entries.forEach {
                if (it.timeToSpawn > 0) it.timeToSpawn--
                else if (it.state == WitherDragonState.SPAWNING) it.setAlive(null)
            }
            currentTick++
        }

        on<RenderEvent.Extract> {
            if (DungeonUtils.getF7Phase() != M7Phases.P5) return@on

            WitherDragonsEnum.entries.forEach { dragon ->
                if (dragonHealth) {
                    DragonCheck.dragonHealthMap.toList().forEach { (_, data) ->
                        if (data.second > 0) drawText(colorHealth(data.second), data.first, 5f, false)
                    }
                }

                if (dragonTimer && dragon.timeToSpawn > 0) {
                    drawText(
                        "§${dragon.colorCode}${dragon.name.first()}: ${getDragonTimer(dragon.timeToSpawn)}",
                        dragon.spawnPos.center, 5f, false
                    )
                }

                if (dragonBoxes && dragon.state != WitherDragonState.DEAD)
                    drawWireFrameBox(dragon.aabbDimensions, dragon.color, depth = true)
            }

            priorityDragon?.let { dragon ->
                if (dragonTracers && dragon.state == WitherDragonState.SPAWNING)
                    mc.player?.let { drawTracer(dragon.spawnPos.center, dragon.color, true) }
            }
        }

        on<WorldEvent.Load> {
            DragonCheck.dragonHealthMap.clear()
            WitherDragonsEnum.reset()
        }
    }

    private fun getDragonTimer(spawnTime: Int): String = when {
        spawnTime <= 20 -> "§c"
        spawnTime <= 60 -> "§e"
        else -> "§a"
    } + when (dragonTimerStyle) {
        0 -> "${spawnTime * 50}${if (showSymbol) "ms" else ""}"
        1 -> "${(spawnTime / 20f).toFixed(1)}${if (showSymbol) "s" else ""}"
        else -> "${spawnTime}${if (showSymbol) "t" else ""}"
    }

    private fun colorHealth(health: Float): String {
        return when {
            health >= 750_000_000 -> "§a${formatHealth(health)}"
            health >= 500_000_000 -> "§e${formatHealth(health)}"
            health >= 250_000_000 -> "§6${formatHealth(health)}"
            else -> "§c${formatHealth(health)}"
        }
    }

    private fun formatHealth(health: Float): String {
        return when {
            health >= 1_000_000_000 -> "${(health / 1_000_000_000).toFixed(1)}b"
            health >= 1_000_000 -> "${(health / 1_000_000).toFixed(1)}m"
            health >= 1_000 -> "${(health / 1_000).toFixed(1)}k"
            else -> "${health.toInt()}"
        }
    }
}