package me.odinmain.utils.skyblock

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.PacketReceivedEvent
import me.odinmain.utils.noControlCodes
import net.minecraft.network.play.server.S02PacketChat
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.floor

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
    current overflow mana
     */

    val currentHealth: Int get() = (mc.thePlayer?.let { player -> (maxHealth * player.health / player.maxHealth).toInt() } ?: 0)
    var maxHealth: Int = 0
    var currentMana: Int = 0
    var maxMana: Int = 0
    var currentSpeed: Int = 0
    var currentDefense: Int = 0
    var overflowMana: Int = 0
    var effectiveHP: Int = 0

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onPacket(event: PacketReceivedEvent) {
        if (event.packet !is S02PacketChat || event.packet.type != 2.toByte()) return
        val msg = event.packet.chatComponent.unformattedText.noControlCodes
        // https://regex101.com/r/3IFer3/1
        val (currentHp, maxHp, middleRegion, cMana, mMana, oMana) = Regex(".*?([\\d|,]+)/([\\d|,]+)❤ {5}(.+) {5}([\\d|,]+)/([\\d|,]+)✎ (Mana|§?[\\d|,]+ʬ).*").find(msg)?.destructured ?: return
        maxHealth = maxHp.replace(",", "").toIntOrNull() ?: return
        currentMana = cMana.replace(",", "").toIntOrNull() ?: return
        maxMana = mMana.replace(",", "").toIntOrNull() ?: return
        overflowMana = oMana.replace(",", "").replace("ʬ","").toIntOrNull() ?: 0
        currentDefense = Regex("([\\d|,]+)❈ Defense").find(middleRegion)?.groupValues?.get(1)?.replace(",", "")?.toIntOrNull() ?: return
        effectiveHP = (currentHealth * (1 + currentDefense / 100))
        currentSpeed = floor((mc.thePlayer?.capabilities?.walkSpeed ?: 0f) * 1000f).toInt()
    }
}