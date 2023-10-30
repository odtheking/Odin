package me.odinmain.utils.skyblock.dungeon

import com.google.common.collect.ComparisonChain
import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.ReceivePacketEvent
import me.odinmain.utils.clock.Executor
import me.odinmain.utils.clock.Executor.Companion.register
import me.odinmain.utils.floor
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.Color
import me.odinmain.utils.skyblock.ChatUtils
import me.odinmain.utils.skyblock.ChatUtils.devMessage
import me.odinmain.utils.skyblock.ChatUtils.modMessage
import me.odinmain.utils.skyblock.ItemUtils
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.LocationUtils.currentDungeon
import me.odinmain.utils.skyblock.PlayerUtils.posY
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.util.EnumFacing
import net.minecraft.world.WorldSettings
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
    val currenRoomName get() = currentRoom?.data?.name ?: "Unknown"

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

    private const val ROOM_SIZE = 32
    private const val START_X = -185
    private const val START_Z = -185

    @SubscribeEvent
    fun onMove(event: LivingEvent.LivingUpdateEvent) {
        if (mc.theWorld == null /*|| !inDungeons ||  inBoss */|| !event.entity.equals(mc.thePlayer)) return
        val x = ((mc.thePlayer.posX + 200) / 32).floor().toInt()
        val z = ((mc.thePlayer.posZ + 200) / 32).floor().toInt()
        val xPos = START_X + x * ROOM_SIZE
        val zPos = START_Z + z * ROOM_SIZE

        currentRoom = scanRoom(xPos, zPos)
        val maxCoreRotation = EnumFacing.HORIZONTALS.maxBy {
            ScanUtils.getCore(xPos + it.frontOffsetX, zPos + it.frontOffsetZ)
        } // will eventually be used to determine the rotation of the room
        //devMessage("rotation: ${currentRoom?.rotation}, maximum core $maxCoreRotation")
    }

    private fun scanRoom(x: Int, z: Int): Room? {
        val height = mc.theWorld.getChunkFromChunkCoords(x shr 4, z shr 4).getHeightValue(x and 15, z and 15)
        if (height == 0) return null

        val roomCore = ScanUtils.getCore(x, z)
        return Room(x, z, ScanUtils.getRoomData(roomCore) ?: return null).apply {
            core = roomCore
        }
    }

    enum class Classes(
        val code: String,
        val color: Color
    ) {
        Archer("§6", Color(255, 170, 0)),
        Mage("§5", Color(170, 0, 170)),
        Berserk("§4", Color(170, 0, 0)),
        Healer("§a", Color(85, 255, 85)),
        Tank("§2", Color(0, 170, 0))
    }
    val isGhost: Boolean get() = ItemUtils.getItemSlot("Haunt", true) != null
    var teammates: List<Pair<EntityPlayer, Classes>> = emptyList()

    init {
        Executor(1000) {
            if (inDungeons) teammates = getDungeonTeammates()
        }.register()
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
        teammates = emptyList()
    }

    private val tablistRegex = Regex("\\[(\\d+)] (?:\\[\\w+] )*(\\w+) (?:.)*?\\((\\w+)(?: (\\w+))*\\)")

    private fun getDungeonTeammates(): List<Pair<EntityPlayer, Classes>> {
        val teammates = mutableListOf<Pair<EntityPlayer, Classes>>()
        val tabList = getDungeonTabList() ?: return emptyList()

        for ((_, line) in tabList) {
            val (_, sbLevel, name, clazz, level) = tablistRegex.find(line.noControlCodes)?.groupValues ?: continue

            mc.theWorld.getPlayerEntityByName(name)?.let { player ->
                Classes.entries.find { it.name == clazz }?.let { foundClass ->
                    teammates.add(Pair(player, foundClass))
                }
            }

        }
        return teammates
    }


    private fun getDungeonTabList(): List<Pair<NetworkPlayerInfo, String>>? {
        val tabEntries = tabList
        if (tabEntries.size < 18 || !tabEntries[0].second.contains("§r§b§lParty §r§f(")) {
            return null
        }
        return tabEntries
    }

    private val tabListOrder = Comparator<NetworkPlayerInfo> { o1, o2 ->
        if (o1 == null) return@Comparator -1
        if (o2 == null) return@Comparator 0
        return@Comparator ComparisonChain.start().compareTrueFirst(
            o1.gameType != WorldSettings.GameType.SPECTATOR,
            o2.gameType != WorldSettings.GameType.SPECTATOR
        ).compare(
            o1.playerTeam?.registeredName ?: "",
            o2.playerTeam?.registeredName ?: ""
        ).compare(o1.gameProfile.name, o2.gameProfile.name).result()
    }

    private val tabList: List<Pair<NetworkPlayerInfo, String>>
        get() = (mc.thePlayer?.sendQueue?.playerInfoMap?.sortedWith(tabListOrder) ?: emptyList())
            .map { Pair(it, mc.ingameGUI.tabList.getPlayerName(it)) }
}