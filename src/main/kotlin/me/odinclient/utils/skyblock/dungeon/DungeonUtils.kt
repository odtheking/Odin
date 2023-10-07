package me.odinclient.utils.skyblock.dungeon

import me.odinclient.ModCore.Companion.mc
import me.odinclient.dungeonmap.core.map.Room
import me.odinclient.events.impl.ChatPacketEvent
import me.odinclient.utils.Utils.noControlCodes
import me.odinclient.utils.clock.Executor
import me.odinclient.utils.clock.Executor.Companion.register
import me.odinclient.utils.render.Color
import me.odinclient.utils.skyblock.ItemUtils
import me.odinclient.utils.skyblock.LocationUtils
import me.odinclient.utils.skyblock.LocationUtils.currentDungeon
import me.odinclient.utils.skyblock.PlayerUtils.posY
import me.odinclient.utils.skyblock.dungeon.map.MapUtils
import me.odinclient.utils.skyblock.dungeon.map.ScanUtils
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.event.entity.living.LivingEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object DungeonUtils {

    inline val inDungeons get() =
        LocationUtils.inSkyblock && currentDungeon != null

    inline val inBoss get() =
        currentDungeon?.inBoss ?: false

    private var lastRoomPos: Pair<Int, Int>? = null
    var currentRoom: Room? = null

    private var inp5 = false

    fun isFloor(vararg options: Int): Boolean {
        return options.any { currentDungeon?.floor?.floorNumber == it }
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

    @SubscribeEvent
    fun onMove(event: LivingEvent.LivingUpdateEvent) {
        if (mc.theWorld == null ||! inDungeons ||! event.entity.equals(mc.thePlayer) || inBoss) return
        ScanUtils.getRoomCentre(mc.thePlayer.posX.toInt(), mc.thePlayer.posZ.toInt()).run {
            if (this != lastRoomPos) {
                lastRoomPos = this
                currentRoom = ScanUtils.getRoomFromPos(this) ?: return
            }
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
    val isGhost: Boolean get() = ItemUtils.getItemSlot("Haunt", true) != null
    var teammates: List<Pair<EntityPlayer, Classes>> = emptyList()

    init {
        Executor(1000) { if (inDungeons) teammates = getDungeonTeammates() }.register()
    }

    @SubscribeEvent
    fun onPacket(event: ChatPacketEvent) {
        if (event.message == "[BOSS] Wither King: You.. again?")
            inp5 = true
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        inp5 = false
        currentRoom = null
    }

    private val tablistRegex = Regex("^\\[(\\d+)] (?:\\[\\w+] )*(\\w+) (?:.)*?\\((\\w+)(?: (\\w+))*\\)\$")

    private fun getDungeonTeammates(): List<Pair<EntityPlayer, Classes>> {
        val teammates = mutableListOf<Pair<EntityPlayer, Classes>>()
        MapUtils.getDungeonTabList()?.forEach { (_, line) ->

            val match = tablistRegex.matchEntire(line.noControlCodes) ?: return@forEach
            val (_, sbLevel, name, clazz, level) = match.groupValues
            if (name == mc.thePlayer.name) return@forEach
            mc.theWorld.playerEntities.find { player ->
                player.name == name
            }?.let { player ->
                teammates.add(Pair(player, Classes.entries.find { classes -> classes.name == clazz }!!))
            }

        }
        //ChatUtils.modMessage(teammates)
        return teammates
    }
}