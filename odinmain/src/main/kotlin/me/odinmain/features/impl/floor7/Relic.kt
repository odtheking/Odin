package me.odinmain.features.impl.floor7

import me.odinmain.OdinMain.mc
import me.odinmain.features.impl.floor7.WitherDragons.colors
import me.odinmain.features.impl.floor7.WitherDragons.relicAnnounceTime
import me.odinmain.features.impl.floor7.WitherDragons.selected
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.*
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.Vec3
import java.util.Locale

object Relic {
    val currentRelic get() = mc.thePlayer?.heldItem?.itemID ?: ""

    enum class Relic (
        val id: String,
        val colorCode: Char,
        val position: Vec3
    ) {
        Green("GREEN_KING_RELIC", 'a', Vec3(20.5, 6.5, 94.5)),
        Purple("PURPLE_KING_RELIC", '5', Vec3(56.5, 8.5, 132.5)),
        Blue("BLUE_KING_RELIC", 'b', Vec3(91.5, 6.5, 94.5)),
        Orange("ORANGE_KING_RELIC", '6', Vec3(90.5, 6.5, 56.5)),
        Red("RED_KING_RELIC", 'c', Vec3(22.5, 6.5, 59.5)),
    }

    private val relicPBs = PersonalBest("Relics", 5)
    private var timer = 0L
    private var ticks = 0

    fun relicsOnMessage(){
        if (WitherDragons.relicAnnounce) partyMessage("${colors[selected]} Relic")
        timer = System.currentTimeMillis()
        ticks = WitherDragons.relicSpawnTicks
    }

    fun relicsBlockPlace(packet: C08PacketPlayerBlockPlacement) {
        if (timer == 0L || !getBlockAt(packet.position).equalsOneOf(Blocks.cauldron, Blocks.anvil)) return

        Relic.entries.find { it.id == currentRelic }?.let {
            relicPBs.time(it.ordinal, (System.currentTimeMillis() - timer) / 1000.0, "s§7!", "§${it.colorCode}${it.name} relic §7took §6", addPBString = true, addOldPBString = true, sendOnlyPB = false, sendMessage = relicAnnounceTime)
            timer = 0L
        }
    }

    fun relicsOnWorldLast() {
        if (ticks <= 0) return
        Relic.entries.forEach {
            Renderer.drawStringInWorld("§${it.colorCode}${it.name.first()}: ${String.format(Locale.US, "%.2f", ticks / 20.0)}s", it.position, depth = false, scale = 0.02f, shadow = true)
        }
    }

    fun onServerTick() {
        ticks = (ticks - 1).coerceAtLeast(0)
    }
}