package me.odinclient.utils.skyblock.dungeon

import me.odinclient.events.ReceivePacketEvent
import me.odinclient.utils.Executor
import me.odinclient.utils.Wrappers
import me.odinclient.utils.skyblock.ItemUtils
import me.odinclient.utils.skyblock.LocationUtils
import me.odinclient.utils.skyblock.LocationUtils.currentDungeon
import me.odinclient.utils.skyblock.ScoreboardUtils
import me.odinclient.utils.skyblock.ScoreboardUtils.cleanSB
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.util.StringUtils.stripControlCodes
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object DungeonUtils : Wrappers() {

    inline val inDungeons get() =
        LocationUtils.inSkyblock && currentDungeon != null

    inline val inBoss get() =
        currentDungeon?.inBoss ?: false

    var inp5 = false

    fun isFloor(vararg options: Int): Boolean {
        for (option in options) {
            if (currentDungeon?.floor?.floorNumber == option) return true
        }
        return false
    }

    fun getPhase(): Int? {
        //if (inp5) return 5 ???? why this
        if (!isFloor(7) || !inBoss) return null

        return when {
            posY > 210 -> 1
            posY > 155 -> 2
            posY > 100 -> 3
            posY > 45 -> 4
            else -> if (currentDungeon?.floor?.isInMM == true) 5 else 4
        }
    }

    enum class Classes(
        val letter: String,
        val code: String,
        val color: Color
    ) {
        ARCHER("A", "§6", Color(255, 170, 0)),
        MAGE("M", "§5", Color(170, 0, 170)),
        BERSERKER("B", "§4", Color(170, 0, 0)),
        HEALER("H", "§a", Color(85, 255, 85)),
        TANK("T", "§2", Color(0, 170, 0))
    }
    val isGhost: Boolean get() = ItemUtils.getItemIndexInInventory("Haunt", true) != -1
    var teammates: List<Pair<EntityPlayer, Classes>> = emptyList()

    init {
        Executor(1000) { if (inDungeons) teammates = getDungeonTeammates() }
    }

    @SubscribeEvent
    fun onPacket(event: ReceivePacketEvent) {
        if (event.packet !is S02PacketChat) return
        val message = stripControlCodes(event.packet.chatComponent.unformattedText)
        if (message == "[BOSS] Wither King: You.. again?") {
            inp5 = true
        }
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        inp5 = false
    }

    private fun getDungeonTeammates(): List<Pair<EntityPlayer, Classes>> {
        val teammates = mutableListOf<Pair<EntityPlayer, Classes>>()
        ScoreboardUtils.sidebarLines.forEach {
            val line = cleanSB(it)
            if (!line.startsWith("[")) return@forEach
            val symbol = line[1].toString()
            val name = line.substringAfter("] ").substringBefore(" ")

            mc.theWorld.playerEntities.find { player ->
                player.name == name && player != mc.thePlayer
            }?.let { player ->
                teammates.add(Pair(player, Classes.values().find { classes -> classes.letter == symbol }!!))
            }
        }
        return teammates
    }
}