package me.odinmain.features.impl.floor7

import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.floor7.WitherDragons.colors
import me.odinmain.features.impl.floor7.WitherDragons.relicAnnounceTime
import me.odinmain.features.impl.floor7.WitherDragons.selected
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.skyblock.*
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement

object Relic {
    val currentRelic get() = mc.thePlayer?.heldItem?.itemID ?: ""

    enum class Relics (
        val id: String,
        val colorCode: String
    ) {
        Green("GREEN_KING_RELIC", "§a"),
        Purple("PURPLE_KING_RELIC", "§5"),
        Blue("BLUE_KING_RELIC", "§b"),
        Orange("ORANGE_KING_RELIC", "§6"),
        Red("RED_KING_RELIC", "§c")
    }

    private val relicPBs = PersonalBest("Relics", 5)
    private var timer = 0L

    fun relicsOnMessage(){
        if (WitherDragons.relicAnnounce) partyMessage("${colors[selected]} Relic")
        timer = System.currentTimeMillis()
    }

    fun relicsBlockPlace(packet: C08PacketPlayerBlockPlacement) {
        if (timer == 0L) return
        val block = mc.theWorld?.getBlockState(packet.position)?.block ?: return
        if (!block.equalsOneOf(Blocks.cauldron, Blocks.anvil) || !currentRelic.equalsOneOf("GREEN_KING_RELIC", "PURPLE_KING_RELIC", "BLUE_KING_RELIC", "ORANGE_KING_RELIC", "RED_KING_RELIC")) return
        val relic = Relics.entries.find { it.id == currentRelic } ?: return modMessage("Relic not found")
        val hasPassed = (System.currentTimeMillis() - timer) / 1000.0

        relicPBs.time(relic.ordinal, hasPassed, "s§7!", "§${relic.colorCode}${relic.name} §7took §6", addPBString = true, addOldPBString = true, relicAnnounceTime)
        timer = 0L
    }
}