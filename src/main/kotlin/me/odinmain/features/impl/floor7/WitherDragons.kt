package me.odinmain.features.impl.floor7

import me.odinmain.events.impl.RealServerTick
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.DragonBoxes.renderBoxes
import me.odinmain.features.impl.floor7.DragonCheck.dragonSpawn
import me.odinmain.features.impl.floor7.DragonCheck.dragonSprayed
import me.odinmain.features.impl.floor7.DragonCheck.dragonUpdate
import me.odinmain.features.impl.floor7.DragonCheck.onChatPacket
import me.odinmain.features.impl.floor7.DragonHealth.renderHP
import me.odinmain.features.impl.floor7.DragonTimer.colorDragonTimer
import me.odinmain.features.impl.floor7.DragonTimer.renderTime
import me.odinmain.features.impl.floor7.DragonTracer.renderTracers
import me.odinmain.features.impl.floor7.KingRelics.relicsBlockPlace
import me.odinmain.features.impl.floor7.KingRelics.relicsOnMessage
import me.odinmain.features.impl.floor7.KingRelics.relicsOnWorldLast
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.render.*
import me.odinmain.utils.runIn
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.M7Phases
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.*
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*

object WitherDragons : Module(
    name = "Wither Dragons",
    description = "Various features for Wither dragons (boxes, timer, HP, priority and more).",
    category = Category.FLOOR7
) {
    private val dragonTimerDropDown by DropdownSetting("Dragon Timer Dropdown")
    private val dragonTimer by BooleanSetting("Dragon Timer", true, description = "Displays a timer for when M7 dragons spawn.").withDependency { dragonTimerDropDown }
    val addUselessDecimal by BooleanSetting("Add Useless Decimal", false, description = "Adds a decimal to the timer.").withDependency { dragonTimer && dragonTimerDropDown }
    private val hud by HudSetting("Dragon Timer HUD", 10f, 10f, 1f, true) {
        if (it) {
            mcText("§5P §a4.5s", 2f, 5f, 1, Color.WHITE, center = false)
            mcText("§cR §e1.2s", 2f, 20f, 1, Color.WHITE, center = false)

            getMCTextWidth("§5P §a4.5s")+ 2f to 33f
        } else {
            if (!dragonTimer) return@HudSetting 0f to 0f
            WitherDragonsEnum.entries.forEachIndexed { index, dragon ->
                if (dragon.state != WitherDragonState.SPAWNING) return@forEachIndexed
                mcText("§${dragon.colorCode}${dragon.name.first()}: ${colorDragonTimer(dragon.timeToSpawn)}${String.format(Locale.US, "%.2f", dragon.timeToSpawn / 20.0)}${if (addUselessDecimal) "0" else ""}", 2, 5f + (index - 1) * 15f, 1, Color.WHITE, center = false)
            }
            getMCTextWidth("§5P §a4.5s")+ 2f to 33f
        }
    }.withDependency { dragonTimer && dragonTimerDropDown }

    private val dragonBoxesDropDown by DropdownSetting("Dragon Boxes Dropdown")
    private val dragonBoxes by BooleanSetting("Dragon Boxes", true, description = "Displays boxes for where M7 dragons spawn.").withDependency { dragonBoxesDropDown }
    val lineThickness by NumberSetting("Line Width", 2f, 1.0, 5.0, 0.5, description = "The thickness of the lines for the boxes.").withDependency { dragonBoxes && dragonBoxesDropDown }

    private val dragonTitleDropDown by DropdownSetting("Dragon Spawn Dropdown")
    val dragonTitle by BooleanSetting("Dragon Title", true, description = "Displays a title for spawning dragons.").withDependency { dragonTitleDropDown }
    private val dragonTracers by BooleanSetting("Dragon Tracer", false, description = "Draws a line to spawning dragons.").withDependency { dragonTitleDropDown }
    val tracerThickness by NumberSetting("Tracer Width", 5f, 1f, 20f, 0.5, description = "The thickness of the tracers.").withDependency { dragonTracers && dragonTitleDropDown }

    private val dragonAlerts by DropdownSetting("Dragon Alerts Dropdown")
    val sendNotification by BooleanSetting("Send Dragon Confirmation", true, description = "Sends a confirmation message when a dragon dies.").withDependency { dragonAlerts }
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
    val soloDebuff by SelectorSetting("Purple Solo Debuff", "Tank", arrayListOf("Tank", "Healer"), false, description = "Displays the debuff of the config. The class that solo debuffs purple, the other class helps b/m.").withDependency { dragonPriorityToggle && dragonPriorityDropDown }
    val soloDebuffOnAll by BooleanSetting("Solo Debuff on All Splits", true, description = "Same as Purple Solo Debuff but for all dragons (A will only have 1 debuff).").withDependency { dragonPriorityToggle && dragonPriorityDropDown }
    val paulBuff by BooleanSetting("Paul Buff", false, description = "Multiplies the power in your run by 1.25.").withDependency { dragonPriorityToggle && dragonPriorityDropDown }

    private val colors = arrayListOf("Green", "Purple", "Blue", "Orange", "Red")
    private val relicDropDown by DropdownSetting("Relics Dropdown")
    val relicAnnounce by BooleanSetting("Relic Announce", false, description = "Announce your relic to the rest of the party.").withDependency { relicDropDown }
    val selected by SelectorSetting("Color", "Green", colors, description = "The color of your relic.").withDependency { relicAnnounce && relicDropDown}
    val relicAnnounceTime by BooleanSetting("Relic Time", true, description = "Sends how long it took you to get that relic.").withDependency { relicDropDown }
    val relicSpawnTicks by NumberSetting("Relic Spawn Ticks", 42, 0, 100, description = "The amount of ticks for the relic to spawn.").withDependency {  relicDropDown }
    val cauldronHighlight by BooleanSetting("Cauldron Highlight", true, description = "Highlights the cauldron for held relic.").withDependency { relicDropDown }

    private val relicHud by HudSetting("Relic Hud", 10f, 10f, 1f, true) {
        if (it) return@HudSetting mcTextAndWidth("§3Relics: 4.30s", 2, 5f, 1, Color.WHITE, center = false) + 2f to 16f
        if (DungeonUtils.getF7Phase() != M7Phases.P5 || KingRelics.relicTicksToSpawn <= 0) return@HudSetting 0f to 0f
        mcTextAndWidth("§3Relics: ${String.format(Locale.US, "%.2f", KingRelics.relicTicksToSpawn / 20.0)}s", 2, 5f, 1, Color.WHITE, center = false) + 2f to 16f
    }.withDependency { relicDropDown }

    var priorityDragon = WitherDragonsEnum.None
    var currentTick: Long = 0

    init {
        onWorldLoad {
            WitherDragonsEnum.reset()
        }

        onPacket(S2APacketParticles::class.java, { DungeonUtils.getF7Phase() == M7Phases.P5 }) {
            handleSpawnPacket(it)
        }

        onPacket(C08PacketPlayerBlockPlacement::class.java) {
            if (relicAnnounce || relicAnnounceTime) relicsBlockPlace(it)
        }

        onPacket(S29PacketSoundEffect::class.java, { DungeonUtils.getF7Phase() == M7Phases.P5 }) {
            if (it.soundName != "random.successful_hit" || !sendArrowHit || priorityDragon == WitherDragonsEnum.None) return@onPacket
            if (priorityDragon.entity?.isEntityAlive == true && currentTick - priorityDragon.spawnedTime < priorityDragon.skipKillTime) arrowsHit++
        }

        onPacket(S04PacketEntityEquipment::class.java, { DungeonUtils.getF7Phase() == M7Phases.P5 && enabled }) {
            dragonSprayed(it)
        }

        onPacket(S0FPacketSpawnMob::class.java, { DungeonUtils.getF7Phase() == M7Phases.P5 && enabled }) {
            if (it.entityType == 63) dragonSpawn(it)
        }

        onPacket(S1CPacketEntityMetadata::class.java, { DungeonUtils.getF7Phase() == M7Phases.P5 && enabled }) {
            dragonUpdate(it)
        }

        onMessage(Regex("\\[BOSS] Necron: All this, for nothing...")) {
            relicsOnMessage()
        }

        onMessage(Regex("^\\[BOSS] Wither King: (Oh, this one hurts!|I have more of those\\.|My soul is disposable\\.)$"), { enabled && DungeonUtils.getF7Phase() == M7Phases.P5 } ) {
            onChatPacket()
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (DungeonUtils.getF7Phase() != M7Phases.P5 || !enabled) return

        if (dragonHealth) renderHP()
        if (dragonTimer) renderTime()
        if (dragonBoxes) renderBoxes()
        if (cauldronHighlight) relicsOnWorldLast()
        if (priorityDragon != WitherDragonsEnum.None && dragonTracers)
            renderTracers(priorityDragon)
    }

    @SubscribeEvent
    fun onServerTick(event: RealServerTick) {
        currentTick++
        DragonCheck.updateTime()
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
            modMessage("§fYou hit §6${arrowsHit} §farrows on §${dragon.colorCode}${dragon.name}${if (dragon.entity?.isEntityAlive == true) " §fin §c${String.format(Locale.US, "%.2f", dragon.skipKillTime.toFloat()/20)} §fSeconds." else "."}")
            arrowsHit = 0
        }
    }
}
