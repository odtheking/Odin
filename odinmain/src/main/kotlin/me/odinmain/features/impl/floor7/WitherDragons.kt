package me.odinmain.features.impl.floor7

import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.events.impl.ReceivePacketEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.DragonBoxes.renderBoxes
import me.odinmain.features.impl.floor7.DragonCheck.dragonJoinWorld
import me.odinmain.features.impl.floor7.DragonCheck.dragonLeaveWorld
import me.odinmain.features.impl.floor7.DragonCheck.lastDragonDeath
import me.odinmain.features.impl.floor7.DragonCheck.onChatPacket
import me.odinmain.features.impl.floor7.DragonHealth.renderHP
import me.odinmain.features.impl.floor7.DragonTimer.renderTime
import me.odinmain.features.impl.floor7.DragonTimer.updateTime
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.DualSetting
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.font.OdinFont
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.ui.hud.HudElement
import me.odinmain.ui.util.getTextWidth
import me.odinmain.ui.util.roundedRectangle
import me.odinmain.ui.util.text
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.Color
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraftforge.client.event.RenderLivingEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.max


object WitherDragons : Module(
    "Wither Dragons",
    description = "Various features for Wither dragons (boxes, timer, HP, priority and more).",
    category = Category.FLOOR7
) {

    private val dragonTimer: Boolean by BooleanSetting("Dragon Timer", true, description = "Displays a timer for when M7 dragons spawn.")
    val textScale: Float by NumberSetting(name = "Text Scale", default = 0.8f, min = 0.1f, max = 5f, increment = 0.1f).withDependency { dragonTimer }
    private val timerBackground: Boolean by BooleanSetting("HUD Timer Background", true, description = "Displays a background for the timer.").withDependency { dragonTimer && hud.displayToggle }
    private val hud: HudElement by HudSetting("Display", 10f, 10f, 1f, true) {
        if (it) {
            if (timerBackground) roundedRectangle(1f, 1f, getTextWidth("Purple spawning in 4500ms", 12f) + 1f, 32f, Color.DARK_GRAY.withAlpha(.75f), 3f)

            text("§5Purple spawning in §a4500ms", 2f, 10f, Color.WHITE, 12f, OdinFont.REGULAR)
            text("§cRed spawning in §e1200ms", 2f, 26f, Color.WHITE, 12f, OdinFont.REGULAR)
            max(
                getTextWidth("Purple spawning in 4500ms", 12f),
                getTextWidth("Red spawning in 1200ms", 12f)
            ) + 2f to 33f
        } else if (DragonTimer.toRender.size != 0) {
            if (!dragonTimer) return@HudSetting 0f to 0f
            var width = 0f
            DragonTimer.toRender.forEachIndexed { index, triple ->
                text(triple.first, 1f, 9f + index * 17f, Color.WHITE, 12f, OdinFont.REGULAR)
                width = max(width, getTextWidth(triple.first.noControlCodes, 19f))
            }
            roundedRectangle(1f, 1f, width + 2f, DragonTimer.toRender.size * 12f, Color.DARK_GRAY.withAlpha(.75f), 4f)
            width to DragonTimer.toRender.size * 17f
        } else 0f to 0f
    }

    private val dragonBoxes: Boolean by BooleanSetting("Dragon Boxes", true, description = "Displays boxes for where M7 dragons spawn.")
    val lineThickness: Float by NumberSetting("Line Width", 2f, 1.0, 5.0, 0.5).withDependency { dragonBoxes }

    val sendNotification: Boolean by BooleanSetting("Send Dragon Confirmation", true, description = "Sends a confirmation message when a dragon dies.")
    val sendTime: Boolean by BooleanSetting("Send Dragon Time Alive", true, description = "Sends a message when a dragon dies with the time it was alive.")
    val sendSpawning: Boolean by BooleanSetting("Send Dragon Spawning", true, description = "Sends a message when a dragon is spawning.")
    val sendSpawned: Boolean by BooleanSetting("Send Dragon Spawned", true, description = "Sends a message when a dragon has spawned.")

    private val dragonHealth: Boolean by BooleanSetting("Dragon Health", true, description = "Displays the health of M7 dragons.")

    val dragonPriorityToggle: Boolean by BooleanSetting("Dragon Priority", false, description = "Displays the priority of dragons spawning.")
    val normalPower: Double by NumberSetting("Normal Power", 10.0, 10.0, 29.0, description = "Power needed to split.").withDependency { dragonPriorityToggle }
    val easyPower: Double by NumberSetting("Easy Power", 10.0, 10.0, 29.0, description = "Power needed when its Purple and another dragon.").withDependency { dragonPriorityToggle }
    val soloDebuff: Boolean by DualSetting("Purple Solo Debuff", "Tank", "Healer", false, description = "Displays the debuff of the config.The class that solo debuffs purple, the other class helps b/m.").withDependency { dragonPriorityToggle }
    val soloDebuffOnAll: Boolean by BooleanSetting("Solo Debuff on All Splits", false, description = "Same as Purple Solo Debuff but for all dragons (A will only have 1 debuff).").withDependency { dragonPriorityToggle }
    val paulBuff: Boolean by BooleanSetting("Paul Buff", false, description = "Multiplies the power in your run by 1.25").withDependency { dragonPriorityToggle }

    val redPB = +NumberSetting("Panes PB", 1000.0, increment = 0.01, hidden = true)
    val orangePB = +NumberSetting("Color PB", 1000.0, increment = 0.01, hidden = true)
    val greenPB = +NumberSetting("Numbers PB", 1000.0, increment = 0.01, hidden = true)
    val bluePB = +NumberSetting("Melody PB", 1000.0, increment = 0.01, hidden = true)
    val purplePB = +NumberSetting("Starts With PB", 1000.0, increment = 0.01, hidden = true)

    @SubscribeEvent
    fun onReceivePacket(event: ReceivePacketEvent) {
        if (DungeonUtils.getPhase() != 5) return
        handleSpawnPacket(event)
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        WitherDragonsEnum.entries.forEach {
            it.particleSpawnTime = 0L
            it.timesSpawned = 0
            it.spawning = false
            it.entity = null
            it.spawnTime()
        }
        DragonTimer.toRender = ArrayList()
        lastDragonDeath = ""
        DragonPriority.firstDragons = false
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (DungeonUtils.getPhase() != 5) return

        if (dragonTimer) {
            updateTime()
            renderTime()
        }
        if (dragonBoxes) renderBoxes()
    }

    @SubscribeEvent
    fun onEntityJoin(event: EntityJoinWorldEvent) {
        if (DungeonUtils.getPhase() != 5) return
        dragonJoinWorld(event)
    }

    @SubscribeEvent
    fun onEntityLeave(event: LivingDeathEvent) {
        if (DungeonUtils.getPhase() != 5) return
        dragonLeaveWorld(event)
    }

    @SubscribeEvent
    fun onChat(event: ChatPacketEvent) {
        if (DungeonUtils.getPhase() != 5) return
        onChatPacket(event)
    }

    @SubscribeEvent
    fun onRenderLivingPost(event: RenderLivingEvent.Post<*>) {
        if (dragonHealth) renderHP(event)
    }
}