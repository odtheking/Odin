package me.odinmain.utils.skyblock.dungeon

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.EnteredDungeonRoomEvent
import me.odinmain.events.impl.PacketReceivedEvent
import me.odinmain.utils.*
import me.odinmain.utils.skyblock.LocationUtils.currentDungeon
import me.odinmain.utils.skyblock.PlayerUtils.posY
import me.odinmain.utils.skyblock.getItemSlot
import net.minecraft.block.BlockSkull
import net.minecraft.block.state.IBlockState
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.init.Blocks
import net.minecraft.tileentity.TileEntitySkull
import net.minecraft.util.BlockPos
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.floor

object DungeonUtils {

    inline val inDungeons: Boolean get() =
        currentDungeon != null

    inline val floorNumber: Int get() =
        currentDungeon?.floor?.floorNumber ?: 0

    inline val floor: Floor get() =
        currentDungeon?.floor ?: Floor.E

    inline val inBoss: Boolean get() =
        currentDungeon?.inBoss ?: false

    inline val secretCount: Int get() =
        currentDungeon?.dungeonStats?.secretsFound ?: 0

    inline val secretPercentage: Float get() =
        currentDungeon?.dungeonStats?.secretsPercent ?: 0f

    inline val totalSecrets: Int get() {
        return if (secretCount == 0 || secretPercentage == 0f) 0
        else floor(100 / secretPercentage * secretCount + 0.5).toInt()
    }

    inline val deathCount: Int get() =
        currentDungeon?.dungeonStats?.deaths ?: 0

    inline val cryptCount: Int get() =
        currentDungeon?.dungeonStats?.crypts ?: 0

    inline val openRoomCount: Int get() =
        currentDungeon?.dungeonStats?.openedRooms ?: 0

    inline val completedRoomCount: Int get() =
        currentDungeon?.dungeonStats?.completedRooms ?: 0

    inline val percentCleared: Int get() =
        currentDungeon?.dungeonStats?.percentCleared ?: 0

    inline val secretsRemaining: Int get() =
        totalSecrets - secretCount

    inline val totalRooms: Int get() {
        return if (completedRoomCount == 0 || percentCleared == 0) 0
        else floor(100 / percentCleared * completedRoomCount + 0.4).toInt()
    }

    inline val dungeonTime: String get() =
        currentDungeon?.dungeonStats?.elapsedTime ?: "00m 00s"

    inline val isGhost: Boolean get() =
        getItemSlot("Haunt", true) != null

    inline val currentRoomName get() =
        currentDungeon?.currentRoom?.room?.data?.name ?: "Unknown"

    inline val dungeonTeammates get() =
        currentDungeon?.dungeonTeammates ?: emptyList()

    inline val dungeonTeammatesNoSelf get() =
        currentDungeon?.dungeonTeammatesNoSelf ?: emptyList()

    inline val leapTeammates get() =
        currentDungeon?.leapTeammates ?: emptyList()

    inline val currentDungeonPlayer get() =
        dungeonTeammates.find { it.name == mc.thePlayer?.name } ?: DungeonPlayer(mc.thePlayer?.name ?: "Unknown", DungeonClass.Unknown, entity = mc.thePlayer)

    inline val doorOpener: String get() =
        currentDungeon?.dungeonStats?.doorOpener ?: "Unknown"

    inline val mimicKilled: Boolean get() =
        currentDungeon?.dungeonStats?.mimicKilled ?: false

    inline val currentRoom get() =
        currentDungeon?.currentRoom

    inline val passedRooms get() =
        currentDungeon?.passedRooms ?: emptyList()

    /**
     * Checks if the current dungeon floor number matches any of the specified options.
     *
     * @param options The floor number options to compare with the current dungeon floor.
     * @return `true` if the current dungeon floor matches any of the specified options, otherwise `false`.
     */
    fun isFloor(vararg options: Int): Boolean {
        return options.any { it == currentDungeon?.floor?.floorNumber }
    }

    /**
     * Gets the current phase of floor 7 boss.
     *
     * @return The current phase of floor 7 boss, or `null` if the player is not in the boss room.
     */
    fun getPhase(): M7Phases {
        if (!isFloor(7) || !inBoss) return M7Phases.Unknown

        return when {
            posY > 210 -> M7Phases.P1
            posY > 155 -> M7Phases.P2
            posY > 100 -> M7Phases.P3
            posY > 45 -> M7Phases.P4
            else -> M7Phases.P5
        }
    }

    @SubscribeEvent
    fun onPacket(event: PacketReceivedEvent) {
        if (inDungeons) currentDungeon?.onPacket(event)
    }

    @SubscribeEvent
    fun onRoomEnter(event: EnteredDungeonRoomEvent) {
        currentDungeon?.enterDungeonRoom(event)
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        Blessings.entries.forEach { it.current = 0 }
    }

    private val tablistRegex = Regex("^\\[(\\d+)] (?:\\[\\w+] )*(\\w+) .*?\\((\\w+)(?: (\\w+))*\\)$")

    fun getDungeonTeammates(previousTeammates: List<DungeonPlayer>): List<DungeonPlayer> {
        val teammates = mutableListOf<DungeonPlayer>()
        val tabList = getDungeonTabList() ?: return emptyList()

        for ((networkPlayerInfo, line) in tabList) {

            val (_, sbLevel, name, clazz, clazzLevel) = tablistRegex.find(line.noControlCodes)?.groupValues ?: continue

            addTeammate(name, clazz, teammates, networkPlayerInfo) // will fail to find the EMPTY or DEAD class and won't add them to the list
            if (clazz == "DEAD" || clazz == "EMPTY") {
                val previousClass = previousTeammates.find { it.name == name }?.clazz ?: continue
                addTeammate(name, previousClass.name, teammates, networkPlayerInfo) // will add the player with the previous class
            }
            teammates.find { it.name == name }?.isDead = clazz == "DEAD" // set the player as dead if they are
        }
        return teammates
    }

    private fun addTeammate(name: String, clazz: String, teammates: MutableList<DungeonPlayer>, networkPlayerInfo: NetworkPlayerInfo) {
        DungeonClass.entries.find { it.name == clazz }?.let { foundClass ->
            mc.theWorld.getPlayerEntityByName(name)?.let { player ->
                teammates.add(DungeonPlayer(name, foundClass, networkPlayerInfo.locationSkin, player))
            } ?: teammates.add(DungeonPlayer(name, foundClass, networkPlayerInfo.locationSkin, null))
        }
    }

    private const val WITHER_ESSENCE_ID = "26bb1a8d-7c66-31c6-82d5-a9c04c94fb02"
    private const val REDSTONE_KEY = "edb0155f-379c-395a-9c7d-1b6005987ac8"

    /**
     * Determines whether a given block state and position represent a secret location.
     *
     * This function checks if the specified block state and position correspond to a secret location based on certain criteria.
     * It considers blocks such as chests, trapped chests, and levers as well as player skulls with a specific player profile ID.
     *
     * @param state The block state to be evaluated for secrecy.
     * @param pos The position (BlockPos) of the block in the world.
     * @return `true` if the specified block state and position indicate a secret location, otherwise `false`.
     */
    fun isSecret(state: IBlockState, pos: BlockPos): Boolean {
        if (state.block.equalsOneOf(Blocks.chest, Blocks.trapped_chest, Blocks.lever)) return true
        else if (state.block is BlockSkull) {
            val tile = mc.theWorld.getTileEntity(pos) ?: return false
            if (tile !is TileEntitySkull) return false
            return tile.playerProfile?.id.toString().equalsOneOf(WITHER_ESSENCE_ID, REDSTONE_KEY)
        }

        return false
    }
}