package me.odinmain.features.impl.floor7

import me.odinmain.events.impl.ServerTickEvent
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.DragonCheck.dragonSpawn
import me.odinmain.features.impl.floor7.DragonCheck.dragonSprayed
import me.odinmain.features.impl.floor7.DragonCheck.dragonUpdate
import me.odinmain.features.impl.floor7.DragonCheck.lastDragonDeath
import me.odinmain.features.impl.floor7.KingRelics.relicsBlockPlace
import me.odinmain.features.impl.floor7.KingRelics.relicsOnMessage
import me.odinmain.features.impl.floor7.KingRelics.relicsOnWorldLast
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.addVec
import me.odinmain.utils.render.RenderUtils
import me.odinmain.utils.render.RenderUtils.renderVec
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.render.getMCTextWidth
import me.odinmain.utils.render.mcTextAndWidth
import me.odinmain.utils.runIn
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.M7Phases
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.toFixed
import me.odinmain.utils.ui.Colors
import me.odinmain.utils.ui.clickgui.util.ColorUtil.withAlpha
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.*
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object WitherDragons : Module(
    name = "Wither Dragons",
    description = "Tools for managing M7 dragons timers, boxes, priority, health, relics and alerts."
) {
    private val dragonTimerDropDown by DropdownSetting("Dragon Timer Dropdown")
    private val dragonTimer by BooleanSetting("Dragon Timer", true, description = "Displays a timer for when M7 dragons spawn.").withDependency { dragonTimerDropDown }
    private val hud by HudSetting("Dragon Timer HUD", 10f, 10f, 1f, true) {
        if (it) {
            RenderUtils.drawText("§5P §a4.5s", 2f, 5f, 1.0, Colors.WHITE, center = false)
            getMCTextWidth("§5P §a4.5s")+ 2f to 33f
        } else {
            priorityDragon.takeIf { drag -> drag != WitherDragonsEnum.None }?.let { dragon ->
                if (dragon.state != WitherDragonState.SPAWNING || dragon.timeToSpawn <= 0) return@HudSetting 0f to 0f
                RenderUtils.drawText("§${dragon.colorCode}${dragon.name.first()}: ${colorDragonTimer(dragon.timeToSpawn)}${dragon.timeToSpawn * 50}ms", 2f, 5f, 1.0, Colors.WHITE, center = false)
            }
            getMCTextWidth("§5P §a4.5s")+ 2f to 33f
        }
    }.withDependency { dragonTimerDropDown }

    private val dragonBoxesDropDown by DropdownSetting("Dragon Boxes Dropdown")
    private val dragonBoxes by BooleanSetting("Dragon Boxes", true, description = "Displays boxes for where M7 dragons spawn.").withDependency { dragonBoxesDropDown }
    private val lineThickness by NumberSetting("Line Width", 2f, 1.0, 5.0, 0.5, description = "The thickness of the lines for the boxes.").withDependency { dragonBoxes && dragonBoxesDropDown }

    private val dragonTitleDropDown by DropdownSetting("Dragon Spawn Dropdown")
    val dragonTitle by BooleanSetting("Dragon Title", true, description = "Displays a title for spawning dragons.").withDependency { dragonTitleDropDown }
    private val dragonTracers by BooleanSetting("Dragon Tracer", false, description = "Draws a line to spawning dragons.").withDependency { dragonTitleDropDown }
    private val tracerThickness by NumberSetting("Tracer Width", 5f, 1f, 20f, 0.5, description = "The thickness of the tracers.").withDependency { dragonTracers && dragonTitleDropDown }

    private val dragonAlerts by DropdownSetting("Dragon Alerts Dropdown")
    private val sendNotification by BooleanSetting("Send Dragon Confirmation", true, description = "Sends a confirmation message when a dragon dies.").withDependency { dragonAlerts }
    val sendTime by BooleanSetting("Send Dragon Time Alive", true, description = "Sends a message when a dragon dies with the time it was alive.").withDependency { dragonAlerts }
    val sendSpawning by BooleanSetting("Send Dragon Spawning", true, description = "Sends a message when a dragon is spawning.").withDependency { dragonAlerts }
    val sendSpawned by BooleanSetting("Send Dragon Spawned", true, description = "Sends a message when a dragon has spawned.").withDependency { dragonAlerts }
    val sendSpray by BooleanSetting("Send Ice Sprayed", true, description = "Sends a message when a dragon has been ice sprayed.").withDependency { dragonAlerts }
    val sendArrowHit by BooleanSetting("Send Arrows Hit", true, description = "Sends a message when a dragon dies with how many arrows were hit.").withDependency { dragonAlerts }
    private var arrowsHit: Int = 0

    private val dragonHealth by BooleanSetting("Dragon Health", true, description = "Displays the health of M7 dragons.")

    private val dragonPriorityDropDown by DropdownSetting("Dragon Priority Dropdown")
    val dragonPriorityToggle by BooleanSetting("Dragon Priority", false, description = "Displays the priority of dragons spawning.").withDependency { dragonPriorityDropDown }
    val normalPower by NumberSetting("Normal Power", 22.0, 0.0, 32.0, description = "Power needed to split.").withDependency { dragonPriorityToggle && dragonPriorityDropDown }
    val easyPower by NumberSetting("Easy Power", 19.0, 0.0, 32.0, description = "Power needed when its Purple and another dragon.").withDependency { dragonPriorityToggle && dragonPriorityDropDown }
    val soloDebuff by SelectorSetting("Purple Solo Debuff", "Tank", arrayListOf("Tank", "Healer"), description = "Displays the debuff of the config. The class that solo debuffs purple, the other class helps b/m.").withDependency { dragonPriorityToggle && dragonPriorityDropDown }
    val soloDebuffOnAll by BooleanSetting("Solo Debuff on All Splits", true, description = "Same as Purple Solo Debuff but for all dragons (A will only have 1 debuff).").withDependency { dragonPriorityToggle && dragonPriorityDropDown }
    val paulBuff by BooleanSetting("Paul Buff", false, description = "Multiplies the power in your run by 1.25.").withDependency { dragonPriorityToggle && dragonPriorityDropDown }

    private val colors = arrayListOf("Green", "Purple", "Blue", "Orange", "Red")
    private val relicDropDown by DropdownSetting("Relics Dropdown")
    val relicAnnounce by BooleanSetting("Relic Announce", false, description = "Announce your relic to the rest of the party.").withDependency { relicDropDown }
    val selected by SelectorSetting("Color", "Green", colors, description = "The color of your relic.").withDependency { relicAnnounce && relicDropDown}
    val relicAnnounceTime by BooleanSetting("Relic Time", true, description = "Sends how long it took you to get that relic.").withDependency { relicDropDown }
    val relicSpawnTicks by NumberSetting("Relic Spawn Ticks", 42, 0, 100, description = "The amount of ticks for the relic to spawn.").withDependency {  relicDropDown }
    private val cauldronHighlight by BooleanSetting("Cauldron Highlight", true, description = "Highlights the cauldron for held relic.").withDependency { relicDropDown }

    private val relicHud by HudSetting("Relic Hud", 10f, 10f, 1f, true) {
        if (it) return@HudSetting mcTextAndWidth("§3Relics: 4.30s", 2, 5f, 1, Colors.WHITE, center = false) + 2f to 16f
        if (DungeonUtils.getF7Phase() != M7Phases.P5 || KingRelics.relicTicksToSpawn <= 0) return@HudSetting 0f to 0f
        mcTextAndWidth("§3Relics: ${(KingRelics.relicTicksToSpawn / 20f).toFixed()}s", 2, 5f, 1, Colors.WHITE, center = false) + 2f to 16f
    }.withDependency { relicDropDown }

    var priorityDragon = WitherDragonsEnum.None
    var currentTick: Long = 0

    init {
        onWorldLoad {
            WitherDragonsEnum.reset()
        }

        onPacket<S2APacketParticles> ({ DungeonUtils.getF7Phase() == M7Phases.P5 }) {
            handleSpawnPacket(it)
        }

        onPacket<C08PacketPlayerBlockPlacement> {
            if (relicAnnounce || relicAnnounceTime) relicsBlockPlace(it)
        }

        onPacket<S29PacketSoundEffect> ({ DungeonUtils.getF7Phase() == M7Phases.P5 }) {
            if (it.soundName != "random.successful_hit" || !sendArrowHit || priorityDragon == WitherDragonsEnum.None) return@onPacket
            if (priorityDragon.entity?.isEntityAlive == true && currentTick - priorityDragon.spawnedTime < priorityDragon.skipKillTime) arrowsHit++
        }

        onPacket<S04PacketEntityEquipment> ({ DungeonUtils.getF7Phase() == M7Phases.P5 && enabled }) {
            dragonSprayed(it)
        }

        onPacket<S0FPacketSpawnMob> ({ DungeonUtils.getF7Phase() == M7Phases.P5 && enabled }) {
            if (it.entityType == 63) dragonSpawn(it)
        }

        onPacket<S1CPacketEntityMetadata> ({ DungeonUtils.getF7Phase() == M7Phases.P5 && enabled }) {
            dragonUpdate(it)
        }

        onMessage(Regex("\\[BOSS] Necron: All this, for nothing...")) {
            relicsOnMessage()
        }

        onMessage(Regex("^\\[BOSS] Wither King: (Oh, this one hurts!|I have more of those\\.|My soul is disposable\\.)$"), { enabled && DungeonUtils.getF7Phase() == M7Phases.P5 } ) {
            WitherDragonsEnum.entries.find { lastDragonDeath == it && lastDragonDeath != WitherDragonsEnum.None }?.let {
                if (sendNotification) modMessage("§${it.colorCode}${it.name} dragon counts.")
            }
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (DungeonUtils.getF7Phase() != M7Phases.P5 || !enabled) return

        if (dragonHealth) {
            DragonCheck.dragonEntityList.forEach {
                if (it.health > 0) Renderer.drawStringInWorld(colorHealth(it.health), it.renderVec.addVec(y = 1.5), Colors.WHITE, depth = false, scale = 0.2f, shadow = true)
            }
        }
        if (dragonTimer) {
            WitherDragonsEnum.entries.forEach { dragon ->
                if (dragon.state == WitherDragonState.SPAWNING && dragon.timeToSpawn > 0) Renderer.drawStringInWorld(
                    "§${dragon.colorCode}${dragon.name.first()}: ${colorDragonTimer(dragon.timeToSpawn)}${dragon.timeToSpawn * 50}ms", dragon.spawnPos,
                    color = Colors.WHITE, depth = false, scale = 0.16f
                )
            }
        }
        if (dragonBoxes)
            WitherDragonsEnum.entries.forEach {
                if (it.state != WitherDragonState.DEAD) Renderer.drawBox(it.boxesDimensions, it.color.withAlpha(0.5f), lineThickness, depth = false, fillAlpha = 0)
            }

        if (cauldronHighlight) relicsOnWorldLast()
        if (priorityDragon != WitherDragonsEnum.None && dragonTracers && priorityDragon.state == WitherDragonState.SPAWNING)
            Renderer.drawTracer(priorityDragon.spawnPos.addVec(0.5, 3.5, 0.5), color = priorityDragon.color, lineWidth = tracerThickness)
    }

    @SubscribeEvent
    fun onServerTick(event: ServerTickEvent) {
        currentTick++
        WitherDragonsEnum.entries.forEach { if (it.state == WitherDragonState.SPAWNING && it.timeToSpawn > 0) it.timeToSpawn-- }
        KingRelics.onServerTick()
    }

    fun arrowDeath(dragon: WitherDragonsEnum) {
        if (!sendArrowHit || currentTick - dragon.spawnedTime >= dragon.skipKillTime) return
        modMessage("§fYou hit §6$arrowsHit §farrows on §${dragon.colorCode}${dragon.name}.")
        arrowsHit = 0
    }

    fun arrowSpawn(dragon: WitherDragonsEnum) {
        if (priorityDragon == WitherDragonsEnum.None || dragon != priorityDragon) return
        arrowsHit = 0
        runIn(dragon.skipKillTime, true) {
            if (dragon.entity?.isEntityAlive != true && arrowsHit <= 0) return@runIn
            modMessage("§fYou hit §6${arrowsHit} §farrows on §${dragon.colorCode}${dragon.name}${if (dragon.entity?.isEntityAlive == true) " §fin §c${(dragon.skipKillTime / 20f).toFixed()} §fSeconds." else "."}")
            arrowsHit = 0
        }
    }

    private fun colorDragonTimer(spawnTime: Int): String {
        return when {
            spawnTime <= 20 -> "§c"
            spawnTime <= 60 -> "§e"
            else -> "§a"
        }
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
            health >= 1_000_000_000 -> "${(health / 1_000_000_000).toFixed()}b"
            health >= 1_000_000     -> "${(health / 1_000_000    ).toFixed()}m"
            health >= 1_000         -> "${(health / 1_000        ).toFixed()}k"
            else -> "${health.toInt()}"
        }
    }
}
