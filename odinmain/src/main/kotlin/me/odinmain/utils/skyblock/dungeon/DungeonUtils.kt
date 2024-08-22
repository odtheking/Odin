package me.odinmain.utils.skyblock.dungeon

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.DungeonEvents.RoomEnterEvent
import me.odinmain.events.impl.PacketReceivedEvent
import me.odinmain.features.impl.dungeon.MapInfo.togglePaul
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.skyblock.*
import me.odinmain.utils.skyblock.LocationUtils.currentDungeon
import me.odinmain.utils.skyblock.PlayerUtils.posY
import me.odinmain.utils.skyblock.dungeon.tiles.FullRoom
import net.minecraft.block.BlockSkull
import net.minecraft.block.state.IBlockState
import net.minecraft.client.network.NetworkPlayerInfo
import net.minecraft.init.Blocks
import net.minecraft.tileentity.TileEntitySkull
import net.minecraft.util.BlockPos
import net.minecraft.util.ResourceLocation
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.ceil
import kotlin.math.floor

object DungeonUtils {

    inline val inDungeons: Boolean get() =
        LocationUtils.currentArea.isArea(Island.Dungeon)

    inline val floorNumber: Int get() =
        currentDungeon?.floor?.floorNumber ?: 0

    inline val floor: Floor get() =
        currentDungeon?.floor ?: Floor.E

    inline val inBoss: Boolean get() =
        currentDungeon?.inBoss ?: false

    inline val secretCount: Int get() =
        currentDungeon?.dungeonStats?.secretsFound ?: 0

    inline val knownSecrets: Int get() =
        currentDungeon?.dungeonStats?.knownSecrets ?: 0

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
        else floor((completedRoomCount/((percentCleared * 0.01).toFloat())) + 0.4).toInt()
    }

    inline val puzzles get() =
        currentDungeon?.puzzles ?: emptyList()

    inline val puzzleCount get() =
        currentDungeon?.puzzles?.size ?: 0

    inline val dungeonTime: String get() =
        currentDungeon?.dungeonStats?.elapsedTime ?: "00m 00s"

    inline val isGhost: Boolean get() =
        getItemSlot("Haunt", true) != null

    inline val currentRoomName get() =
        currentDungeon?.currentFullRoom?.room?.data?.name ?: "Unknown"

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

    inline val currentFullRoom: FullRoom? get() =
        currentDungeon?.currentFullRoom

    inline val passedRooms get() =
        currentDungeon?.passedRooms ?: emptyList()

    inline val isPaul: Boolean get() =
         currentDungeon?.paul ?: false

    inline val getBonusScore: Int get() {
        var score = 0
        score += cryptCount.coerceAtMost(5)
        if (mimicKilled) score += 2
        if ((isPaul && togglePaul == 0) || togglePaul == 2) score += 10
        return score
    }

    inline val bloodDone: Boolean get() =
        currentDungeon?.dungeonStats?.bloodDone ?: false

    inline val score: Int get() {
        val completed: Float = completedRoomCount.toFloat() + (if (!bloodDone) 1f else 0f) + (if (!inBoss) 1f else 0f)
        val total: Float = if (totalRooms != 0) totalRooms.toFloat() else 36f

        val exploration = floor((secretPercentage/floor.secretPercentage)/100f * 40f).coerceIn(0f, 40f).toInt() +
                floor(completed/total * 60f).coerceIn(0f, 60f).toInt()

        val skillRooms = floor(completed/total * 80f).coerceIn(0f, 80f).toInt()
        val puzzlePenalty = puzzles.filter { it.status != PuzzleStatus.Completed }.size * 10
        val skill = (20 + skillRooms - puzzlePenalty - (deathCount * 2 - 1).coerceAtLeast(0)).coerceIn(20, 100)

        return exploration + skill + getBonusScore + 100
    }

    inline val neededSecretsAmount: Int get() {
        val scoreFactor = 40 - getBonusScore + (deathCount * 2 - 1).coerceAtLeast(0)
        return ceil((totalSecrets * floor.secretPercentage) * scoreFactor / 40.0).toInt()
    }

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
    fun onRoomEnter(event: RoomEnterEvent) {
        if (inDungeons) currentDungeon?.enterDungeonRoom(event)
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        Blessing.entries.forEach { it.current = 0 }
    }

    private val puzzleRegex = Regex("^§r (\\w+(?: \\w+)*|\\?\\?\\?): §r§7\\[(§r§c§l✖|§r§a§l✔|§r§6§l✦)§r§7] ?(?:§r§f\\(§r§[a-z](\\w+)§r§f\\))?§r$")

    fun getDungeonPuzzles(list: List<String> = listOf()): List<Puzzle> {
        return list.mapNotNull { text ->
            val matchGroups = puzzleRegex.find(text)?.groupValues ?: return@mapNotNull null
            val puzzle = Puzzle.allPuzzles.find { it.name == matchGroups[1] }?.copy() ?: return@mapNotNull null

            puzzle.status = when {
                puzzles.find { it.name == puzzle.name }?.status == PuzzleStatus.Completed -> PuzzleStatus.Completed
                matchGroups[2] == "§r§c§l✖" -> PuzzleStatus.Failed
                matchGroups[2] == "§r§a§l✔" -> PuzzleStatus.Completed
                matchGroups[2] == "§r§6§l✦" -> PuzzleStatus.Incomplete
                else -> {
                    modMessage(text.replace("§", "&"), false)
                    return@mapNotNull null
                }
            }
            puzzle
        }
    }

    private val tablistRegex = Regex("^\\[(\\d+)] (?:\\[\\w+] )*(\\w+) .*?\\((\\w+)(?: (\\w+))*\\)$")

    fun getDungeonTeammates(previousTeammates: List<DungeonPlayer>, tabList: List<Pair<NetworkPlayerInfo, String>>): List<DungeonPlayer> {
        val teammates = mutableListOf<DungeonPlayer>()

        for ((networkPlayerInfo, line) in tabList) {

            val (_, _, name, clazz, _) = tablistRegex.find(line.noControlCodes)?.groupValues ?: continue

            addTeammate(name, clazz, teammates, networkPlayerInfo.locationSkin) // will fail to find the EMPTY or DEAD class and won't add them to the list
            if (clazz == "DEAD" || clazz == "EMPTY") {
                val previousClass = previousTeammates.find { it.name == name }?.clazz ?: continue
                addTeammate(name, previousClass.name, teammates, networkPlayerInfo.locationSkin) // will add the player with the previous class
            }
            teammates.find { it.name == name }?.isDead = clazz == "DEAD" // set the player as dead if they are
        }
        return teammates
    }

    private fun addTeammate(name: String, clazz: String, teammates: MutableList<DungeonPlayer>, locationSkin: ResourceLocation) {
        DungeonClass.entries.find { it.name == clazz }?.let { foundClass ->
            mc.theWorld?.getPlayerEntityByName(name)?.let { player ->
                teammates.add(DungeonPlayer(name, foundClass, locationSkin, player))
            } ?: teammates.add(DungeonPlayer(name, foundClass, locationSkin, null))
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
        if (state.block.equalsOneOf(Blocks.chest, Blocks.trapped_chest, Blocks.lever, Blocks.lever)) return true
        else if (state.block is BlockSkull) {
            val tile = mc.theWorld?.getTileEntity(pos) ?: return false
            if (tile !is TileEntitySkull) return false
            return tile.playerProfile?.id.toString().equalsOneOf(WITHER_ESSENCE_ID, REDSTONE_KEY)
        }
        return false
    }
}