package me.odinmain.utils.skyblock.dungeon

import me.odinmain.OdinMain.mc
import me.odinmain.events.impl.PacketEvent
import me.odinmain.events.impl.RoomEnterEvent
import me.odinmain.features.impl.dungeon.MapInfo.togglePaul
import me.odinmain.utils.*
import me.odinmain.utils.skyblock.Island
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.LocationUtils.currentDungeon
import me.odinmain.utils.skyblock.PlayerUtils.posY
import me.odinmain.utils.skyblock.dungeon.tiles.Room
import me.odinmain.utils.skyblock.getItemSlot
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.block.BlockSkull
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S38PacketPlayerListItem
import net.minecraft.tileentity.TileEntitySkull
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.*

object DungeonUtils {

    val inDungeons: Boolean
        get() = LocationUtils.currentArea.isArea(Island.Dungeon)

    val floorNumber: Int
        get() = currentDungeon?.floor?.floorNumber ?: 0

    val floor: Floor
        get() = currentDungeon?.floor ?: Floor.E

    val inBoss: Boolean
        get() = currentDungeon?.inBoss == true

    val secretCount: Int
        get() = currentDungeon?.dungeonStats?.secretsFound ?: 0

    val knownSecrets: Int
        get() = currentDungeon?.dungeonStats?.knownSecrets ?: 0

    val secretPercentage: Float
        get() = currentDungeon?.dungeonStats?.secretsPercent ?: 0f

    val totalSecrets: Int
        get() = if (secretCount == 0 || secretPercentage == 0f) 0 else floor(100 / secretPercentage * secretCount + 0.5).toInt()

    val deathCount: Int
        get() = currentDungeon?.dungeonStats?.deaths ?: 0

    val cryptCount: Int
        get() = currentDungeon?.dungeonStats?.crypts ?: 0

    val openRoomCount: Int
        get() = currentDungeon?.dungeonStats?.openedRooms ?: 0

    val completedRoomCount: Int
        get() = currentDungeon?.dungeonStats?.completedRooms ?: 0

    val percentCleared: Int
        get() = currentDungeon?.dungeonStats?.percentCleared ?: 0

    val totalRooms: Int
        get() = if (completedRoomCount == 0 || percentCleared == 0) 0 else floor((completedRoomCount / (percentCleared * 0.01).toFloat()) + 0.4).toInt()

    val puzzles: List<Puzzle>
        get() = currentDungeon?.puzzles.orEmpty()

    val puzzleCount: Int
        get() = currentDungeon?.puzzles?.size ?: 0

    val dungeonTime: String
        get() = currentDungeon?.dungeonStats?.elapsedTime ?: "00m 00s"

    val isGhost: Boolean
        get() = getItemSlot("Haunt", true) != null

    val currentRoomName: String
        get() = currentDungeon?.currentRoom?.data?.name ?: "Unknown"

    val dungeonTeammates: ArrayList<DungeonPlayer>
        get() = currentDungeon?.dungeonTeammates ?: ArrayList()

    val dungeonTeammatesNoSelf: ArrayList<DungeonPlayer>
        get() = currentDungeon?.dungeonTeammatesNoSelf ?: ArrayList()

    val leapTeammates: ArrayList<DungeonPlayer>
        get() = currentDungeon?.leapTeammates ?: ArrayList()

    val currentDungeonPlayer: DungeonPlayer
        get() = dungeonTeammates.find { it.name == mc.thePlayer?.name } ?: DungeonPlayer(mc.thePlayer?.name ?: "Unknown", DungeonClass.Unknown, 0, entity = mc.thePlayer)

    val doorOpener: String
        get() = currentDungeon?.dungeonStats?.doorOpener ?: "Unknown"

    val mimicKilled: Boolean
        get() = currentDungeon?.dungeonStats?.mimicKilled == true

    val currentRoom: Room?
        get() = currentDungeon?.currentRoom

    val passedRooms: Set<Room>
        get() = currentDungeon?.passedRooms.orEmpty()

    val isPaul: Boolean
        get() = currentDungeon?.paul == true

    val getBonusScore: Int
        get() {
            var score = cryptCount.coerceAtMost(5)
            if (mimicKilled) score += 2
            if ((isPaul && togglePaul == 0) || togglePaul == 2) score += 10
            return score
        }

    val bloodDone: Boolean
        get() = currentDungeon?.dungeonStats?.bloodDone == true

    val score: Int
        get() {
            val completed: Float = completedRoomCount.toFloat() + (if (!bloodDone) 1f else 0f) + (if (!inBoss) 1f else 0f)
            val total: Float = if (totalRooms != 0) totalRooms.toFloat() else 36f

            val exploration = floor((secretPercentage / floor.secretPercentage) / 100f * 40f).coerceIn(0f, 40f).toInt() +
                    floor(completed / total * 60f).coerceIn(0f, 60f).toInt()

            val skillRooms = floor(completed / total * 80f).coerceIn(0f, 80f).toInt()
            val puzzlePenalty = puzzles.filter { it.status != PuzzleStatus.Completed }.size * 10

            return exploration + (20 + skillRooms - puzzlePenalty - (deathCount * 2 - 1).coerceAtLeast(0)).coerceIn(20, 100) + getBonusScore + 100
        }

    val neededSecretsAmount: Int
        get() = ceil((totalSecrets * floor.secretPercentage) * (40 - getBonusScore + (deathCount * 2 - 1).coerceAtLeast(0)) / 40.0).toInt()

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
    fun getF7Phase(): M7Phases {
        if ((!isFloor(7) || !inBoss) && !LocationUtils.currentArea.isArea(Island.SinglePlayer)) return M7Phases.Unknown

        return when {
            posY > 210 -> M7Phases.P1
            posY > 155 -> M7Phases.P2
            posY > 100 -> M7Phases.P3
            posY > 45 -> M7Phases.P4
            else -> M7Phases.P5
        }
    }

    fun getMageCooldownMultiplier(): Double {
        return if (currentDungeonPlayer.clazz != DungeonClass.Mage) 1.0
        else 1 - 0.25 - (floor(currentDungeonPlayer.clazzLvl / 2.0) / 100) * if (dungeonTeammates.count { it.clazz == DungeonClass.Mage } == 1) 2 else 1
    }

    /**
     * Gets the new ability cooldown after mage cooldown reductions.
     * @param baseSeconds The base cooldown of the ability in seconds. Eg 10
     * @return The new time
     */
    fun getAbilityCooldown(baseSeconds: Long): Long {
        return (baseSeconds * getMageCooldownMultiplier()).roundToLong()
    }

    @SubscribeEvent
    fun onPacket(event: PacketEvent.Receive) {
        if (inDungeons) currentDungeon?.onPacket(event)
    }

    @SubscribeEvent
    fun onRoomEnter(event: RoomEnterEvent) {
        if (inDungeons) currentDungeon?.enterDungeonRoom(event)
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        currentDungeon?.onWorldLoad()
    }

    @SubscribeEvent
    fun onEntityJoin(event: EntityJoinWorldEvent) {
        currentDungeon?.onEntityJoin(event)
    }

    private val puzzleRegex = Regex("^§r (\\w+(?: \\w+)*|\\?\\?\\?): §r§7\\[(§r§c§l✖|§r§a§l✔|§r§6§l✦)§r§7] ?(?:§r§f\\(§r§[a-z](\\w+)§r§f\\))?§r$")

    fun getDungeonPuzzles(list: List<String> = listOf()): List<Puzzle> {
        return list.mapNotNull { text ->
            val (name, status) = puzzleRegex.find(text)?.destructured ?: return@mapNotNull null
            val puzzle = Puzzle.allPuzzles.find { it.name == name }?.copy() ?: return@mapNotNull null

            puzzle.status = when {
                puzzles.find { it.name == puzzle.name }?.status == PuzzleStatus.Completed -> PuzzleStatus.Completed
                status == "§r§c§l✖" -> PuzzleStatus.Failed
                status == "§r§a§l✔" -> PuzzleStatus.Completed
                status == "§r§6§l✦" -> PuzzleStatus.Incomplete
                else -> {
                    modMessage(text.replace("§", "&"))
                    return@mapNotNull null
                }
            }
            puzzle
        }
    }

    private val tablistRegex = Regex("^\\[(\\d+)] (?:\\[\\w+] )*(\\w+) .*?\\((\\w+)(?: (\\w+))*\\)$")
    var customLeapOrder: List<String> = emptyList()

    fun getDungeonTeammates(previousTeammates: ArrayList<DungeonPlayer>, tabList: List<S38PacketPlayerListItem.AddPlayerData>): ArrayList<DungeonPlayer> {
        for (line in tabList) {
            val displayName = line.displayName?.unformattedText?.noControlCodes ?: continue
            val (_, name, clazz, clazzLevel) = tablistRegex.find(displayName)?.destructured ?: continue

            previousTeammates.find { it.name == name }?.let { player -> player.isDead = clazz == "DEAD" } ?:
            previousTeammates.add(DungeonPlayer(name, DungeonClass.entries.find { it.name == clazz } ?: continue, clazzLvl = romanToInt(clazzLevel), mc.netHandler?.getPlayerInfo(name)?.locationSkin ?: continue, mc.theWorld?.getPlayerEntityByName(name), false))
        }
        return previousTeammates
    }

    const val WITHER_ESSENCE_ID = "e0f3e929-869e-3dca-9504-54c666ee6f23"
    private const val REDSTONE_KEY = "fed95410-aba1-39df-9b95-1d4f361eb66e"

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
        return when {
            state.block.equalsOneOf(Blocks.chest, Blocks.trapped_chest, Blocks.lever) -> true
            state.block is BlockSkull -> {
                val tile = mc.theWorld?.getTileEntity(pos) as? TileEntitySkull ?: return false
                tile.playerProfile?.id.toString().equalsOneOf(WITHER_ESSENCE_ID, REDSTONE_KEY)
            }
            else -> false
        }
    }

    fun Room.getRelativeCoords(pos: Vec3) = pos.subtractVec(x = clayPos.x, z = clayPos.z).rotateToNorth(rotation)
    fun Room.getRealCoords(pos: Vec3) = pos.rotateAroundNorth(rotation).addVec(x = clayPos.x, z = clayPos.z)
    fun Room.getRelativeCoords(pos: BlockPos) = getRelativeCoords(Vec3(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())).toBlockPos()
    fun Room.getRealCoords(pos: BlockPos) = getRealCoords(Vec3(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())).toBlockPos()
    fun Room.getRelativeCoords(x: Int, y: Int, z: Int) = getRelativeCoords(BlockPos(x.toDouble(), y.toDouble(), z.toDouble()))
    fun Room.getRealCoords(x: Int, y: Int, z: Int) = getRealCoords(BlockPos(x.toDouble(), y.toDouble(), z.toDouble()))

    val dungeonItemDrops = listOf(
        "Health Potion VIII Splash Potion", "Healing Potion 8 Splash Potion", "Healing Potion VIII Splash Potion", "Healing VIII Splash Potion", "Healing 8 Splash Potion",
        "Decoy", "Inflatable Jerry", "Spirit Leap", "Trap", "Training Weights", "Defuse Kit", "Dungeon Chest Key", "Treasure Talisman", "Revive Stone", "Architect's First Draft"
    )
}