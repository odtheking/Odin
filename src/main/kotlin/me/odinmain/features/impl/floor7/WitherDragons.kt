package me.odinmain.features.impl.floor7

import me.odinmain.events.impl.ArrowEvent
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
import me.odinmain.utils.runOnMCThread
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.M7Phases
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.toFixed
import me.odinmain.utils.ui.Colors
import me.odinmain.utils.ui.clickgui.util.ColorUtil.withAlpha
import net.minecraft.entity.boss.EntityDragonPart
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.*
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object WitherDragons : Module(
    name = "Wither Dragons",
    desc = "Tools for managing M7 dragons timers, boxes, priority, health, relics and alerts."
) {
    private val dragonTimerDropDown by DropdownSetting("Dragon Timer Dropdown")
    private val dragonTimer by BooleanSetting("Dragon Timer", true, desc = "Displays a timer for when M7 dragons spawn.").withDependency { dragonTimerDropDown }
    private val dragonTimerStyle by SelectorSetting("Timer Style", "Milliseconds", arrayListOf("Milliseconds", "Seconds", "Ticks"), desc = "The style of the dragon timer.").withDependency { dragonTimer && dragonTimerDropDown }
    private val showSymbol by BooleanSetting("Timer Symbol", true, desc = "Displays a symbol for the timer.").withDependency { dragonTimer && dragonTimerDropDown }
    private val hud by HudSetting("Dragon Timer HUD", 10f, 10f, 1f, true) {
        if (it) {
            RenderUtils.drawText("§5P §a4.5s", 1f, 1f, 1f, Colors.WHITE, center = false)
            getMCTextWidth("§5P §a4.5s")+ 2f to 12f
        } else {
            priorityDragon.takeIf { drag -> drag != WitherDragonsEnum.None }?.let { dragon ->
                if (dragon.state != WitherDragonState.SPAWNING || dragon.timeToSpawn <= 0) return@HudSetting 0f to 0f
                RenderUtils.drawText("§${dragon.colorCode}${dragon.name.first()}: ${getDragonTimer(dragon.timeToSpawn)}", 1f, 1f, 1f, Colors.WHITE, center = false)
            }
            getMCTextWidth("§5P §a4.5s")+ 2f to 12f
        }
    }.withDependency { dragonTimerDropDown }

    private val dragonBoxesDropDown by DropdownSetting("Dragon Boxes Dropdown")
    private val dragonBoxes by BooleanSetting("Dragon Boxes", true, desc = "Displays boxes for where M7 dragons spawn.").withDependency { dragonBoxesDropDown }
    private val lineThickness by NumberSetting("Line Width", 2f, 1.0, 5.0, 0.5, desc = "The thickness of the lines for the boxes.").withDependency { dragonBoxes && dragonBoxesDropDown }

    private val dragonTitleDropDown by DropdownSetting("Dragon Spawn Dropdown")
    val dragonTitle by BooleanSetting("Dragon Title", true, desc = "Displays a title for spawning dragons.").withDependency { dragonTitleDropDown }
    private val dragonTracers by BooleanSetting("Dragon Tracer", false, desc = "Draws a line to spawning dragons.").withDependency { dragonTitleDropDown }
    private val tracerThickness by NumberSetting("Tracer Width", 5f, 1f, 20f, 0.5, desc = "The thickness of the tracers.").withDependency { dragonTracers && dragonTitleDropDown }

    private val dragonAlerts by DropdownSetting("Dragon Alerts Dropdown")
    private val sendNotification by BooleanSetting("Send Dragon Confirmation", true, desc = "Sends a confirmation message when a dragon dies.").withDependency { dragonAlerts }
    val sendTime by BooleanSetting("Send Dragon Time Alive", true, desc = "Sends a message when a dragon dies with the time it was alive.").withDependency { dragonAlerts }
    val sendSpawning by BooleanSetting("Send Dragon Spawning", true, desc = "Sends a message when a dragon is spawning.").withDependency { dragonAlerts }
    val sendSpawned by BooleanSetting("Send Dragon Spawned", true, desc = "Sends a message when a dragon has spawned.").withDependency { dragonAlerts }
    val sendSpray by BooleanSetting("Send Ice Sprayed", true, desc = "Sends a message when a dragon has been ice sprayed.").withDependency { dragonAlerts }
    val sendArrowHit by BooleanSetting("Send Arrows Hit", true, desc = "Sends a message when a dragon dies with how many arrows were hit.").withDependency { dragonAlerts }
    private val firstArrowHit by BooleanSetting("Send First Hit", false, desc = "Sends a message when a player hits their first arrow.").withDependency { dragonAlerts }

    private val dragonHealth by BooleanSetting("Dragon Health", true, desc = "Displays the health of M7 dragons.")

    private val dragonPriorityDropDown by DropdownSetting("Dragon Priority Dropdown")
    val dragonPriorityToggle by BooleanSetting("Dragon Priority", false, desc = "Displays the priority of dragons spawning.").withDependency { dragonPriorityDropDown }
    val normalPower by NumberSetting("Normal Power", 22.0f, 0.0, 32.0, desc = "Power needed to split.").withDependency { dragonPriorityToggle && dragonPriorityDropDown }
    val easyPower by NumberSetting("Easy Power", 19.0f, 0.0, 32.0, desc = "Power needed when its Purple and another dragon.").withDependency { dragonPriorityToggle && dragonPriorityDropDown }
    val soloDebuff by SelectorSetting("Purple Solo Debuff", "Tank", arrayListOf("Tank", "Healer"), desc = "Displays the debuff of the config. The class that solo debuffs purple, the other class helps b/m.").withDependency { dragonPriorityToggle && dragonPriorityDropDown }
    val soloDebuffOnAll by BooleanSetting("Solo Debuff on All Splits", true, desc = "Same as Purple Solo Debuff but for all dragons (A will only have 1 debuff).").withDependency { dragonPriorityToggle && dragonPriorityDropDown }
    val paulBuff by BooleanSetting("Paul Buff", false, desc = "Multiplies the power in your run by 1.25.").withDependency { dragonPriorityToggle && dragonPriorityDropDown }

    private val colors = arrayListOf("Green", "Purple", "Blue", "Orange", "Red")
    private val relicDropDown by DropdownSetting("Relics Dropdown")
    val relicAnnounce by BooleanSetting("Relic Announce", false, desc = "Announce your relic to the rest of the party.").withDependency { relicDropDown }
    val selected by SelectorSetting("Color", "Green", colors, desc = "The color of your relic.").withDependency { relicAnnounce && relicDropDown}
    val relicAnnounceTime by BooleanSetting("Relic Time", true, desc = "Sends how long it took you to get that relic.").withDependency { relicDropDown }
    val relicSpawnTicks by NumberSetting("Relic Spawn Ticks", 42, 0, 100, desc = "The amount of ticks for the relic to spawn.").withDependency {  relicDropDown }
    private val cauldronHighlight by BooleanSetting("Cauldron Highlight", true, desc = "Highlights the cauldron for held relic.").withDependency { relicDropDown }

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

        onPacket<S32PacketConfirmTransaction> {
            WitherDragonsEnum.entries.forEach { if (it.state == WitherDragonState.SPAWNING && it.timeToSpawn > 0) it.timeToSpawn-- }
            KingRelics.onServerTick()
            currentTick++
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (DungeonUtils.getF7Phase() != M7Phases.P5) return

        if (dragonHealth) DragonCheck.dragonEntityList.forEach {
            if (it.health > 0) Renderer.drawStringInWorld(colorHealth(it.health), it.renderVec.addVec(y = 1.5), Colors.WHITE, depth = false, scale = 0.2f, shadow = true)
        }

        WitherDragonsEnum.entries.forEach { dragon ->
            if (dragonTimer && dragon.state == WitherDragonState.SPAWNING && dragon.timeToSpawn > 0) Renderer.drawStringInWorld(
                "§${dragon.colorCode}${dragon.name.first()}: ${getDragonTimer(dragon.timeToSpawn)}", dragon.spawnPos,
                color = Colors.WHITE, depth = false, scale = 0.16f
            )

            if (dragonBoxes && dragon.state != WitherDragonState.DEAD) Renderer.drawBox(dragon.boxesDimensions, dragon.color.withAlpha(0.5f), lineThickness, depth = false, fillAlpha = 0)
        }

        if (cauldronHighlight) relicsOnWorldLast()
        if (priorityDragon != WitherDragonsEnum.None && dragonTracers && priorityDragon.state == WitherDragonState.SPAWNING)
            Renderer.drawTracer(priorityDragon.spawnPos.addVec(0.5, 3.5, 0.5), color = priorityDragon.color, lineWidth = tracerThickness)
    }

    @SubscribeEvent
    fun onArrowDespawn(event: ArrowEvent.Despawn) = WitherDragonsEnum.entries.forEach { dragon ->
        if (dragon.state != WitherDragonState.ALIVE || currentTick - dragon.spawnedTime >= dragon.skipKillTime) return@forEach
        val hits = event.entitiesHit.filter { ((it as? EntityDragonPart)?.entityDragonObj ?: it) == dragon.entity }.size.takeUnless { it == 0 } ?: return@forEach
        runOnMCThread {
            if (event.owner.name !in dragon.arrowsHit.keys && firstArrowHit)
                modMessage("§a${event.owner.name} §fhit their first arrow on §${dragon.colorCode}${dragon.name}§f after §c${(currentTick - dragon.spawnedTime).let { "$it §ftick${if (it > 1) "s" else ""}" }}.")
            dragon.arrowsHit.merge(event.owner.name, hits, Int::plus)
        }
    }

    private fun getDragonTimer(spawnTime: Int): String = when {
        spawnTime <= 20 -> "§c"
        spawnTime <= 60 -> "§e"
        else            -> "§a"
    } + when (dragonTimerStyle) {
        0    -> "${(spawnTime * 50)}${if (showSymbol) "ms" else ""}"
        1    -> "${(spawnTime / 20f).toFixed()}${if (showSymbol) "s" else ""}"
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
            health >= 1_000_000_000 -> "${(health / 1_000_000_000).toFixed()}b"
            health >= 1_000_000     -> "${(health / 1_000_000    ).toFixed()}m"
            health >= 1_000         -> "${(health / 1_000        ).toFixed()}k"
            else -> "${health.toInt()}"
        }
    }
}
