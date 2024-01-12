package me.odinmain.features.impl.floor7

import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.events.impl.ReceivePacketEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.floor7.DragonBoxes.renderBoxes
import me.odinmain.features.impl.floor7.DragonDeathCheck.dragonJoinWorld
import me.odinmain.features.impl.floor7.DragonDeathCheck.dragonLeaveWorld
import me.odinmain.features.impl.floor7.DragonDeathCheck.onChatPacket
import me.odinmain.features.impl.floor7.DragonHealth.renderHP
import me.odinmain.features.impl.floor7.DragonPriority.firstDragonsSpawned
import me.odinmain.features.impl.floor7.DragonTimer.renderTime
import me.odinmain.features.impl.floor7.DragonTimer.updateTime
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.ui.hud.HudElement
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.gui.nvg.Fonts
import me.odinmain.utils.render.gui.nvg.getTextWidth
import me.odinmain.utils.render.gui.nvg.rect
import me.odinmain.utils.render.gui.nvg.textWithControlCodes
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.math.max


object WitherDragons : Module(
    "Wither Dragons",
    description = "Displays a timer for when M7 dragons spawn.",
    category = Category.FLOOR7
) {

    private val dragonTimer: Boolean by BooleanSetting("Dragon Timer", true, description = "Displays a timer for when M7 dragons spawn.")
    val textScale: Float by NumberSetting(name = "Text Scale", default = 1f, min = 0f, max = 10f, increment = 0.1f).withDependency { dragonTimer}

    private val dragonBoxes: Boolean by BooleanSetting("Dragon Boxes", true, description = "Displays boxes for where M7 dragons spawn.")
    val lineThickness: Float by NumberSetting("Line Width", 2f, 1.0, 5.0, 0.5).withDependency { dragonBoxes }

    val sendNotif: Boolean by BooleanSetting("Send Dragon Confirmation", true)
    val sendTime: Boolean by BooleanSetting("Send Dragon Time Alive", true)
    val sendSpawning: Boolean by BooleanSetting("Send Dragon Spawning", true)

    private val dragonHealth: Boolean by BooleanSetting("Dragon Health", true, description = "Displays the health of M7 dragons.")

    val dragPrioSpawnToggle: Boolean by BooleanSetting("Dragon Priority Spawn", true, description = "Displays the priority of dragons spawning.")
    val configPower: Double by NumberSetting("Config Power", 21.0, 10.0, 29.0, description = "Displays the power of the config.")
    val configEasyPower: Double by NumberSetting("Config Power", 19.0, 10.0, 29.0, description = "Displays the power of the config.")
    val configSoloDebuff: Double by NumberSetting("Config Solo Debuff", 2.0, 1.0, 2.0, description = "Displays the debuff of the config.")
    val paulBuff: Boolean by BooleanSetting("Paul Buff", false, description = "Should the mod calculate with paul's blessing buff?")

    private val hud: HudElement by HudSetting("Display", 10f, 10f, 1f, true) {
        if (it) {
            rect(1f, 1f, getTextWidth("Purple spawning in 4500ms", 17f, Fonts.REGULAR), 35f, Color.DARK_GRAY.withAlpha(.75f), 5f)

            textWithControlCodes("§5Purple spawning in §a4500ms", 2f, 10f, 16f, Fonts.REGULAR)
            textWithControlCodes("§cRed spawning in §e1200ms", 2f, 26f, 16f, Fonts.REGULAR)

            max(
                getTextWidth("Purple spawning in 4500ms", 16f, Fonts.REGULAR),
                getTextWidth("Red spawning in 1200ms", 16f, Fonts.REGULAR)
            ) + 2f to 33f
        } else if (DragonTimer.toRender.size != 0) {
            var width = 0f
            DragonTimer.toRender.forEachIndexed { index, triple ->
                textWithControlCodes(triple.first, 1f, 9f + index * 17f, 16f, Fonts.REGULAR)
                width = max(width, getTextWidth(triple.first.noControlCodes, 16f, Fonts.REGULAR))
            }
            rect(1f, 1f, width + 2f, DragonTimer.toRender.size * 17f + 1, Color.DARK_GRAY.withAlpha(.5f), 5f)
            width to DragonTimer.toRender.size * 17f
        } else 0f to 0f
    }

    val redPB = +NumberSetting("Panes PB", 1000.0, increment = 0.01, hidden = true)
    val orangePB = +NumberSetting("Color PB", 1000.0, increment = 0.01, hidden = true)
    val greenPB = +NumberSetting("Numbers PB", 1000.0, increment = 0.01, hidden = true)
    val bluePB = +NumberSetting("Melody PB", 1000.0, increment = 0.01, hidden = true)
    val purplePB = +NumberSetting("Starts With PB", 1000.0, increment = 0.01, hidden = true)

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        if (DungeonUtils.getPhase() != 5) return
        WitherDragonsEnum.entries.forEach { it.checkAlive() }
        updateTime()
    }

    @SubscribeEvent
    fun onReceivePacket(event: ReceivePacketEvent) {
        if (DungeonUtils.getPhase() != 5) return
        handleSpawnPacket(event)
    }
    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        WitherDragonsEnum.entries.forEach { it.particleSpawnTime = 0L }
        DragonTimer.toRender = ArrayList()
        DragonDeathCheck.dragonMap = HashMap()
        DragonDeathCheck.deadDragonMap = HashMap()
        firstDragonsSpawned = false
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (DungeonUtils.getPhase() != 5) return

        if (dragonTimer) renderTime()
        if (dragonBoxes) renderBoxes()
        if (dragonHealth) renderHP()
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
        onChatPacket(event)
    }

}