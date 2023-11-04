package me.odinmain.features.impl.skyblock

import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.containsOneOf
import me.odinmain.utils.skyblock.ChatUtils.modMessage
import me.odinmain.utils.skyblock.PlayerUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object Reminders : Module(
    "Reminders",
    description = "Helpful reminders for dungeons.",
    category = Category.SKYBLOCK
) {
    private val ultReminder: Boolean by BooleanSetting("Ult Reminder")
    private val dragReminder: Boolean by BooleanSetting("Drag Reminder")
    private val readyReminder: Boolean by BooleanSetting("Ready Reminder")
    private val maskAlert: Boolean by BooleanSetting("Mask Alert")
    private val wishAlert: Boolean by BooleanSetting("Wish Alert")
    private val healthPrecentage: Int by NumberSetting("Health Precentage", 40, 0, 80, 1)

    private var firstLaser = false
    private var playerReady = false
    private var canWish = true

    private val alertMap = mapOf(
        "[BOSS] Wither King: You.. again?" to "§3Swap to edrag!",
        "[BOSS] Maxor: YOU TRICKED ME!" to "§3Use ult!",
        "[BOSS] Maxor: THAT BEAM! IT HURTS! IT HURTS!!" to "§3Use ult!",
        "[BOSS] Goldor: You have done it, you destroyed the factory…" to "§3Use ult!",
        "[BOSS] Sadan: My giants! Unleashed!" to "§3Use ult!"
        // Add more pairs here as needed
    )

    @SubscribeEvent
    fun onClientChatReceived(event: ChatPacketEvent) {
        if (!DungeonUtils.inDungeons) return
        val msg = event.message

        if (maskAlert) {
            if (Regex("^(Second Wind Activated!)? ?Your (.+) saved your life!\$").matches(msg)) {
                PlayerUtils.alert("Mask used!")
                modMessage("Mask used!")
            }
        }

        if ((msg.contains("Wish is ready to use!") || msg.contains("Your Healer ULTIMATE wish is now available!")) && !DungeonUtils.inBoss && !DungeonUtils.isGhost)
            canWish = true
        else if (DungeonUtils.inBoss && canWish && !DungeonUtils.isGhost)
            canWish = false

        if (msg == "${mc.thePlayer.name} is now ready!") {
            playerReady = true
            mc.thePlayer.closeScreen()
            return
        }

        val alert = alertMap[msg] ?: return

        if (msg.startsWith("[BOSS] Maxor:")) if (!firstLaser) firstLaser = true else return
        if (msg.startsWith("[BOSS] Wither King:") && !dragReminder) return
        if (!ultReminder && msg.containsOneOf("Maxor", "Goldor", "Sadan")) return

        PlayerUtils.alert(alert)
        modMessage(alert)
    }

    @SubscribeEvent
    fun onClientTick(event: TickEvent.ClientTickEvent) {
        if (!wishAlert || DungeonUtils.inBoss || !DungeonUtils.inDungeons || !canWish) return
        DungeonUtils.teammates.forEach { entityPlayer ->
            val currentHp = entityPlayer.entity?.health ?: 40f
            if (currentHp < 40 * (healthPrecentage / 100) && !DungeonUtils.isGhost) {
                modMessage("§7${entityPlayer.name}§a is at less than §c$healthPrecentage% §aHP!")
                PlayerUtils.alert("USE WISH")
                canWish = false
            }
        }
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        playerReady = false
        firstLaser = false
    }

    init {
        execute(10000, 0) {
            if (!readyReminder || !DungeonUtils.inDungeons) return@execute
            if (playerReady) return@execute

            PlayerUtils.alert("§3Ready up!")
            modMessage("Ready up!")
        }
    }
}