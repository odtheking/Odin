package me.odinclient.utils.skyblock.dungeon

import me.odinclient.OdinClient.Companion.mc
import me.odinclient.dungeonmap.features.Dungeon
import me.odinclient.events.ReceivePacketEvent
import me.odinclient.utils.Utils.noControlCodes
import me.odinclient.utils.clock.Executor
import me.odinclient.utils.clock.Executor.Companion.register
import me.odinclient.utils.skyblock.ItemUtils
import me.odinclient.utils.skyblock.LocationUtils
import me.odinclient.utils.skyblock.LocationUtils.currentDungeon
import me.odinclient.utils.skyblock.PlayerUtils.posY
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.server.S02PacketChat
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

object DungeonUtils {

    inline val inDungeons get() =
        LocationUtils.inSkyblock && currentDungeon != null

    inline val inBoss get() =
        currentDungeon?.inBoss ?: false

    private var inp5 = false

    fun isFloor(vararg options: Int): Boolean {
        for (option in options) {
            if (currentDungeon?.floor?.floorNumber == option) return true
        }
        return false
    }

    fun getPhase(): Int? {
        if (!isFloor(7) || !inBoss) return null

        return when {
            posY > 210 -> 1
            posY > 155 -> 2
            posY > 100 -> 3
            posY > 45 -> 4
            else -> 5
        }
    }

    enum class Classes(
        val code: String,
        val color: Color
    ) {
        Archer("§6", Color(255, 170, 0)),
        Mage("§5", Color(170, 0, 170)),
        Berserker("§4", Color(170, 0, 0)),
        Healer("§a", Color(85, 255, 85)),
        Tank("§2", Color(0, 170, 0))
    }
    val tablistRegex = Regex("^\\[(\\d+)] (?:\\[\\w+] )*(\\w+) (?:.)*?\\((\\w+)(?: (\\w+))*\\)\$")
    val isGhost: Boolean get() = ItemUtils.getItemSlot("Haunt", true) != null
    var teammates: List<Pair<EntityPlayer, Classes>> = emptyList()

    init {
        Executor(1000) { if (inDungeons) teammates = getDungeonTeammates() }.register()
    }

    @SubscribeEvent
    fun onPacket(event: ReceivePacketEvent) {
        if (event.packet !is S02PacketChat) return
        val message = event.packet.chatComponent.unformattedText.noControlCodes
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
        Dungeon.getDungeonTabList()?.forEach { (_, line) ->
            val match = tablistRegex.matchEntire(line) ?: return@forEach
            val (_, sbLevel, name, clazz, level) = match.groupValues

            mc.theWorld.playerEntities.find { player ->
                player.name == name
            }?.let { player ->
                teammates.add(Pair(player, Classes.values().find { classes -> classes.name == clazz }!!))
            }

        }
        return teammates
    }
}