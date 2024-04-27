package me.odinmain.utils.skyblock

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.PacketReceivedEvent
import me.odinmain.utils.noControlCodes
import net.minecraft.network.play.server.S02PacketChat
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object SkyblockPlayer {
    /*
    in module there should be:
    health display current/Max
    health bar
    defense display
    mana display current/Max
    mana bar
    current speed
    current ehp
     */

    val currentHealth: Int get() = (maxHealth * (mc.thePlayer?.health ?: 0f) / 40f).toInt()
    var maxHealth: Int = 0
    var currentMana: Int = 0
    var maxMana: Int = 0
    var currentSpeed: Int = 0
    var currentDefense: Int = 0

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onPacket(event: PacketReceivedEvent) {
        if (event.packet !is S02PacketChat || event.packet.type != 2.toByte()) return
        val msg = event.packet.chatComponent.unformattedText.noControlCodes
        val (currentHp, maxHp, middleRegion, cMana, mMana) = Regex("(.+)/(.+)❤ {5}(.+) {5}(.+)/(.+)✎ Mana").find(msg)?.destructured ?: return

        maxHealth = maxHp.replace(",", "").toIntOrNull() ?: return
        currentMana = cMana.replace(",", "").toIntOrNull() ?: return
        maxMana = mMana.replace(",", "").toIntOrNull() ?: return

        currentDefense = Regex("([\\d|,]+)❈ Defense").find(middleRegion)?.groupValues?.get(1)?.replace(",", "")?.toIntOrNull() ?: return

        devMessage("""
            current Health = $currentHealth
            maxHealth = $maxHealth
            currentMana = $currentMana
            maxMana = $maxMana
            currentDefense = $currentDefense
        """.trimIndent())
    }
}