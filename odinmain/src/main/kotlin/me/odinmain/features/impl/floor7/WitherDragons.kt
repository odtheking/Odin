package me.odinmain.features.impl.floor7

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.DragonBoxes.renderBoxes
import me.odinmain.features.impl.floor7.DragonCheck.dragonJoinWorld
import me.odinmain.features.impl.floor7.DragonCheck.dragonLeaveWorld
import me.odinmain.features.impl.floor7.DragonCheck.dragonSprayed
import me.odinmain.features.impl.floor7.DragonCheck.lastDragonDeath
import me.odinmain.features.impl.floor7.DragonCheck.onChatPacket
import me.odinmain.features.impl.floor7.DragonHealth.renderHP
import me.odinmain.features.impl.floor7.DragonTimer.renderTime
import me.odinmain.features.impl.floor7.DragonTracer.renderTracers
import me.odinmain.features.impl.floor7.Relic.relicsBlockPlace
import me.odinmain.features.impl.floor7.Relic.relicsOnMessage
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.ui.hud.HudElement
import me.odinmain.utils.max
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.getMCTextWidth
import me.odinmain.utils.render.mcText
import me.odinmain.utils.render.roundedRectangle
import me.odinmain.utils.skyblock.Island
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.S04PacketEntityEquipment
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.network.play.server.S2APacketParticles
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule
import kotlin.math.max


object WitherDragons : Module(
    "Wither Dragons",
    description = "Various features for Wither dragons (boxes, timer, HP, priority and more).",
    category = Category.FLOOR7
) {
    private val dragonTimer: Boolean by BooleanSetting("Dragon Timer", true, description = "Displays a timer for when M7 dragons spawn.")
    val textScale: Float by NumberSetting(name = "Text Scale", default = 0.8f, min = 0.1f, max = 5f, increment = 0.1f).withDependency { dragonTimer }
    private val timerBackground: Boolean by BooleanSetting("HUD Timer Background", false, description = "Displays a background for the timer.").withDependency { dragonTimer && hud.displayToggle }
    private val hud: HudElement by HudSetting("Dragon Timer HUD", 10f, 10f, 1f, true) {
        if (it) {
            if (timerBackground) roundedRectangle(1f, 1f, getMCTextWidth("Purple spawning in 4500ms") + 1f, 32f, Color.DARK_GRAY.withAlpha(.75f), 3f)
            mcText("§5Purple spawning in §a4500ms", 2f, 5f, 1, Color.WHITE, center = false)
            mcText("§cRed spawning in §e1200ms", 2f, 20f, 1, Color.WHITE, center = false)
            max(
                getMCTextWidth("Purple spawning in 4500ms"),
                getMCTextWidth("Red spawning in 1200ms")
            ) + 2f to 33f
        } else if (DragonTimer.toRender.size != 0) {
            if (!dragonTimer) return@HudSetting 0f to 0f
            var width = 0f
            DragonTimer.toRender.forEachIndexed { index, triple ->
                mcText(triple.first, 2, 5f + (index - 1) * 15f, 1, Color.WHITE, center = false)
                width = max(width, getMCTextWidth(triple.first.noControlCodes))
            }
            if (timerBackground) roundedRectangle(1f, 1f, getMCTextWidth("Purple spawning in 4500ms") + 1f, 32f, Color.DARK_GRAY.withAlpha(.75f), 3f)
            width to DragonTimer.toRender.size * 17f
        } else 0f to 0f
    }

    private val dragonBoxes: Boolean by BooleanSetting("Dragon Boxes", true, description = "Displays boxes for where M7 dragons spawn.")
    val lineThickness: Float by NumberSetting("Line Width", 2f, 1.0, 5.0, 0.5).withDependency { dragonBoxes }
    val dragonTitle: Boolean by BooleanSetting("Dragon Title", true, description = "Displays a title for spawning dragons.")
    private val dragonTracers: Boolean by BooleanSetting("Dragon Tracer", false, description = "draws a line to spawning dragons")
    val tracerThickness: Float by NumberSetting("Tracer Width", 5f, 1f, 20f).withDependency { dragonTracers }

    private val dragonAlerts: Boolean by DropdownSetting("Dragon Alerts")
    val sendNotification: Boolean by BooleanSetting("Send Dragon Confirmation", true, description = "Sends a confirmation message when a dragon dies.").withDependency { dragonAlerts }
    val sendTime: Boolean by BooleanSetting("Send Dragon Time Alive", true, description = "Sends a message when a dragon dies with the time it was alive.").withDependency { dragonAlerts }
    val sendSpawning: Boolean by BooleanSetting("Send Dragon Spawning", true, description = "Sends a message when a dragon is spawning.").withDependency { dragonAlerts }
    val sendSpawned: Boolean by BooleanSetting("Send Dragon Spawned", true, description = "Sends a message when a dragon has spawned.").withDependency { dragonAlerts }
    val sendSpray: Boolean by BooleanSetting("Send Ice Sprayed", true, description = "Sends a message when a dragon has been ice sprayed.").withDependency { dragonAlerts }
    val sendArrowHit: Boolean by BooleanSetting("Send Arrows Hit", true, description = "Sends a message when a dragon dies with how many arrows were hit.").withDependency { dragonAlerts }
    private var arrowsHit: Int = 0

    private val dragonHealth: Boolean by BooleanSetting("Dragon Health", true, description = "Displays the health of M7 dragons.")

    val dragonPriorityToggle: Boolean by BooleanSetting("Dragon Priority", false, description = "Displays the priority of dragons spawning.")
    val normalPower: Double by NumberSetting("Normal Power", 10.0, 0.0, 29.0, description = "Power needed to split.").withDependency { dragonPriorityToggle }
    val easyPower: Double by NumberSetting("Easy Power", 10.0, 0.0, 29.0, description = "Power needed when its Purple and another dragon.").withDependency { dragonPriorityToggle }
    val soloDebuff: Boolean by DualSetting("Purple Solo Debuff", "Tank", "Healer", false, description = "Displays the debuff of the config.The class that solo debuffs purple, the other class helps b/m.").withDependency { dragonPriorityToggle }
    val soloDebuffOnAll: Boolean by BooleanSetting("Solo Debuff on All Splits", false, description = "Same as Purple Solo Debuff but for all dragons (A will only have 1 debuff).").withDependency { dragonPriorityToggle }
    val paulBuff: Boolean by BooleanSetting("Paul Buff", false, description = "Multiplies the power in your run by 1.25").withDependency { dragonPriorityToggle }

    val colors = arrayListOf("Green", "Purple", "Blue", "Orange", "Red")
    private val relics: Boolean by DropdownSetting("Relics")
    val relicAnnounce: Boolean by BooleanSetting("Relic Announce", false, description = "Announce your relic to the rest of the party.").withDependency { relics }
    val selected: Int by SelectorSetting("Color", "Green", colors, description = "The color of your relic.").withDependency { relicAnnounce && relics}
    val relicAnnounceTime: Boolean by BooleanSetting("Relic Time", true, description = "Sends how long it took you to get that relic").withDependency { relics }

    val redPB = +NumberSetting("Panes PB", 1000.0, increment = 0.01, hidden = true)
    val orangePB = +NumberSetting("Color PB", 1000.0, increment = 0.01, hidden = true)
    val greenPB = +NumberSetting("Numbers PB", 1000.0, increment = 0.01, hidden = true)
    val bluePB = +NumberSetting("Melody PB", 1000.0, increment = 0.01, hidden = true)
    val purplePB = +NumberSetting("Starts With PB", 1000.0, increment = 0.01, hidden = true)

    lateinit var priorityDragon: WitherDragonsEnum

    init {
        onWorldLoad {
            WitherDragonsEnum.entries.forEach {
                it.particleSpawnTime = 0L
                it.timesSpawned = 0
                it.spawning = false
                it.entity = null
                it.spawnTime()
            }
            DragonTimer.toRender = ArrayList()
            lastDragonDeath = ""
        }

        onPacket(S2APacketParticles::class.java, { DungeonUtils.getPhase() == Island.M7P5 }) {
            handleSpawnPacket(it)
        }

        onPacket(C08PacketPlayerBlockPlacement::class.java) {
            if (relicAnnounce || relicAnnounceTime) relicsBlockPlace(it)
        }

        onPacket(S29PacketSoundEffect::class.java, { DungeonUtils.getPhase() == Island.M7P5 }) {
            if (it.soundName != "random.successful_hit" || !sendArrowHit || !::priorityDragon.isInitialized) return@onPacket
            if (priorityDragon.entity?.isEntityAlive == true && System.currentTimeMillis() - priorityDragon.spawnedTime < priorityDragon.skipKillTime) arrowsHit++
        }

        onPacket(S04PacketEntityEquipment::class.java, { DungeonUtils.getPhase() == Island.M7P5 }) {
            dragonSprayed(it)
        }

        onMessage("[BOSS] Necron: All this, for nothing...", false) {
            if (relicAnnounce || relicAnnounceTime) relicsOnMessage()
        }

        onMessage(Regex(".*")) {
            if (DungeonUtils.getPhase() != Island.M7P5) return@onMessage
            onChatPacket(it)
        }
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (DungeonUtils.getPhase() != Island.M7P5) return

        if (dragonHealth) renderHP()
        if (dragonTimer) renderTime()
        if (dragonBoxes) renderBoxes()
        if (::priorityDragon.isInitialized) {
            if (dragonTracers) renderTracers(priorityDragon)
        }
    }

    @SubscribeEvent
    fun onEntityJoin(event: EntityJoinWorldEvent) {
        if (DungeonUtils.getPhase() != Island.M7P5) return
        dragonJoinWorld(event)
    }

    @SubscribeEvent
    fun onEntityLeave(event: LivingDeathEvent) {
        if (DungeonUtils.getPhase() != Island.M7P5) return
        dragonLeaveWorld(event)
    }

    fun arrowDeath(dragon: WitherDragonsEnum) {
        if (::priorityDragon.isInitialized && dragon == priorityDragon) {
            if (sendArrowHit && System.currentTimeMillis() - dragon.spawnedTime < dragon.skipKillTime) {
                modMessage("§fYou have hit §6$arrowsHit §farrows on §${priorityDragon.colorCode}${priorityDragon.name}")
                arrowsHit = 0
            }
        }
    }

    fun arrowSpawn(dragon: WitherDragonsEnum) {
        if (::priorityDragon.isInitialized && dragon == priorityDragon) {
            arrowsHit = 0
            Timer().schedule(dragon.skipKillTime) {
                if (dragon.entity?.isEntityAlive == true || arrowsHit > 0) {
                    modMessage("§fYou hit §6${arrowsHit} §fon §${dragon.colorCode}${dragon.name}${if (dragon.entity?.isEntityAlive == true) " §fin §c${String.format("%.2f", dragon.skipKillTime.toFloat()/1000)} §fSeconds." else "."}")
                    arrowsHit = 0
                }
            }
        }
    }

}
