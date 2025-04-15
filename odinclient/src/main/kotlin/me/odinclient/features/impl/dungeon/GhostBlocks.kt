package me.odinclient.features.impl.dungeon

import me.odinclient.utils.skyblock.PlayerUtils.leftClick
import me.odinclient.utils.skyblock.PlayerUtils.swapToIndex
import me.odinmain.events.impl.BlockChangeEvent
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.addRange
import me.odinmain.utils.runIn
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.WITHER_ESSENCE_ID
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.getF7Phase
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.inDungeons
import me.odinmain.utils.skyblock.getBlockAt
import me.odinmain.utils.skyblock.getItemSlot
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.block.state.IBlockState
import net.minecraft.enchantment.Enchantment
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.network.play.server.S21PacketChunkData
import net.minecraft.tileentity.TileEntitySkull
import net.minecraft.util.BlockPos
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.input.Keyboard

object GhostBlocks : Module(
    name = "Ghost Blocks",
    desc = "Creates ghost blocks by key, tools, swap stonk, stonk delay and pre configured.",
    key = null,
) {
    // gkey
    private val gkeyDropDown by DropdownSetting("Gkey Dropdown", false)
    private val ghostBlockKey by KeybindSetting("Ghost block Keybind", Keyboard.KEY_NONE, "Makes blocks you're looking at disappear.").withDependency { gkeyDropDown }
    private val ghostBlockSpeed by NumberSetting("Speed", 50L, 0.0, 300.0, 10.0, unit = "ms", desc = "The speed at which ghost blocks are created.").withDependency { gkeyDropDown }
    private val ghostBlockSkulls by BooleanSetting("Ghost Skulls", true, desc = "If enabled skulls will also be turned into ghost blocks.").withDependency { gkeyDropDown }
    private val ghostBlockRange by NumberSetting("Range", 8.0, 4.5, 80.0, 0.5, desc = "Maximum range at which ghost blocks will be created.").withDependency { gkeyDropDown }
    private val onlyDungeon by BooleanSetting("Only In Dungeon", false, desc = "Will only work inside of a dungeon.").withDependency { gkeyDropDown }

    // pre blocks
    private val preGhostBlock by BooleanSetting("F7 Ghost blocks", false, desc = "Will adjust specific blocks in the boss room.")

    // ghost pickaxe
    private val ghostPickDropDown by DropdownSetting("Ghost Tools Dropdown", false)
    private val slot by NumberSetting("Ghost tool slot", 1, 1.0, 9.0, 1.0, desc = "The slot at which the ghost tool will spawn.").withDependency { ghostPickDropDown }
    private val level by NumberSetting("Efficiency level", 10, 1.0, 100.0, 1.0, desc = "The efficiency level the ghost tool will spawn with.").withDependency { ghostPickDropDown }
    private val delay by NumberSetting("Delay to Create", 0, 0, 1000, 10, unit = "ms", desc = "The delay between clicking to the spawning of the ghost tool.").withDependency { ghostPickDropDown }
    private val pickaxeKey by KeybindSetting("Pickaxe Keybind", Keyboard.KEY_NONE, description = "Press this keybind to create a ghost pickaxe.").onPress { giveItem(278) }.withDependency { ghostPickDropDown }
    private val axeKey by KeybindSetting("Axe Keybind", Keyboard.KEY_NONE, description = "Press this keybind to create a ghost axe.").onPress { giveItem(279) }.withDependency { ghostPickDropDown }

    private fun giveItem(id: Int) {
        if (!enabled || mc.thePlayer == null || mc.currentScreen != null) return
        runIn(delay / 50) {
            val item = ItemStack(Item.getItemById(id), 1).apply {
                addEnchantment(Enchantment.getEnchantmentById(32), level)
                tagCompound?.setBoolean("Unbreakable", true)
            }
            mc.thePlayer?.inventory?.mainInventory?.set(slot - 1, item)
        }
    }

    // swap stonk
    private val swapStonkDropDown by DropdownSetting("Swap Stonk Dropdown", false)
    private val swapStonk by BooleanSetting("Swap Stonk", false, desc = "Does a swap stonk when you press the keybind.").withDependency { swapStonkDropDown }
    private val pickaxe by SelectorSetting("Type", "Pickaxe", arrayListOf("Pickaxe", "Stonk"), desc = "The type of pickaxe to use.").withDependency { swapStonk && swapStonkDropDown }
    private val speed by NumberSetting("Swap back speed", 2, 1, 5, desc = "Delay between swapping back.", unit = " ticks").withDependency { swapStonk &&  swapStonkDropDown}
    private val swapStonkKey by KeybindSetting("Swap Stonk Keybind", Keyboard.KEY_NONE, "Press to perform a swap stonk.").withDependency { swapStonkDropDown }
        .onPress {
            if (!enabled) return@onPress
            val slot = getItemSlot(if (pickaxe == 1) "Stonk" else "Pickaxe", true) ?: return@onPress
            if (slot !in 0..8) return@onPress modMessage("Couldn't find pickaxe.")
            val originalItem = mc.thePlayer?.inventory?.currentItem ?: 0
            if (originalItem == slot) return@onPress
            leftClick()
            swapToIndex(slot)
            runIn(speed) { swapToIndex(originalItem) }
        }.withDependency { swapStonk }


    private val blacklist = arrayOf(Blocks.stone_button, Blocks.chest, Blocks.trapped_chest, Blocks.lever)

    init {
        execute({ ghostBlockSpeed }) {
            if (!enabled || mc.currentScreen != null || (onlyDungeon && !inDungeons)) return@execute
            if (!ghostBlockKey.isDown()) return@execute

            toAir(mc.thePlayer?.rayTrace(ghostBlockRange, 1f)?.blockPos ?: return@execute)
        }

        execute(1000) {
            if (!DungeonUtils.isFloor(7) || !DungeonUtils.inBoss || !preGhostBlock || !enabled) return@execute
            val phase = getF7Phase().displayName.drop(1).toIntOrNull() ?: return@execute
            for (i in blocks[phase] ?: return@execute) {
                mc.theWorld?.setBlockToAir(i)
            }
            for (i in enderChests[phase] ?: return@execute) {
                mc.theWorld?.setBlockState(i, Blocks.ender_chest.defaultState)
            }
            for (i in glass[phase] ?: return@execute) {
                mc.theWorld?.setBlockState(i, Blocks.stained_glass.defaultState)
            }
        }

        onWorldLoad {
            sdBlocks.clear()
        }
    }

    private fun toAir(blockPos: BlockPos) {
        getBlockAt(blockPos).let { block ->
            if (block !in blacklist && (block !== Blocks.skull || (ghostBlockSkulls && (mc.theWorld?.getTileEntity(blockPos) as? TileEntitySkull)
                    ?.playerProfile?.id?.toString() != WITHER_ESSENCE_ID))) mc.theWorld?.setBlockToAir(blockPos)
        }
    }

    // stonk delay
    private val stonkDelayDropDown by DropdownSetting("Stonk Delay Dropdown", false)
    private val stonkDelayToggle by BooleanSetting("Stonk Delay Toggle", desc = "Delay mined blocks reset time.").withDependency { stonkDelayDropDown }
    private val sdOnlySB by BooleanSetting("SD Only In SB", true, desc = "Disables Stonk Delay when outside of Skyblock.").withDependency { stonkDelayToggle && stonkDelayDropDown }
    private val stonkDelay by NumberSetting("Stonk Delay Time", 200L, 50L, 10000L, 10L, unit = "ms", desc = "The time before blocks reset.").withDependency { stonkDelayToggle && stonkDelayDropDown }

    private data class BlockData(val pos: BlockPos, var state: IBlockState, val time: Long, var serverReplaced: Boolean)
    private val sdBlocks = mutableListOf<BlockData>()

    private fun BlockData.reset() = mc.theWorld?.setBlockState(pos, state)

    @JvmStatic
    fun breakBlock(pos: BlockPos) {
        if (!stonkDelayToggle || (sdOnlySB && !LocationUtils.isInSkyblock)) return
        sdBlocks.add(BlockData(pos, mc.theWorld.getBlockState(pos), System.currentTimeMillis(), false))
    }

    @SubscribeEvent
    fun onBlockChange(event: BlockChangeEvent) {
        if (event.updated == Blocks.air.defaultState || !stonkDelayToggle || (sdOnlySB && !LocationUtils.isInSkyblock)) return
        sdBlocks.find { event.pos == it.pos }?.let {
            it.state = event.updated
            it.serverReplaced = true
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK || !stonkDelayToggle || (sdOnlySB && !LocationUtils.isInSkyblock)) return
        sdBlocks.removeIf { it.pos == (event.pos.offset(event.face) ?: return@removeIf false) }
    }

    @SubscribeEvent
    fun tick(event: TickEvent.ClientTickEvent) {
        if (event.phase != TickEvent.Phase.START || !stonkDelayToggle || (sdOnlySB && !LocationUtils.isInSkyblock)) return
        val currentMillis = System.currentTimeMillis()
        val blocksToReset = mutableListOf<BlockData>()
        sdBlocks.removeAll {
            val timeExisted = currentMillis - it.time
            val shouldReset = it.serverReplaced && timeExisted >= stonkDelay
            if (shouldReset) blocksToReset.add(it)
            shouldReset || (timeExisted >= 10000)
        }
        blocksToReset.forEach { it.reset() }
    }

    @JvmStatic
    fun postChunkData(packet: S21PacketChunkData) {
        if (!enabled || !stonkDelayToggle || (sdOnlySB && !LocationUtils.isInSkyblock)) return
        sdBlocks.forEach {
            if (it.pos.x in (packet.chunkX shl 4).addRange(15) && it.pos.z in (packet.chunkZ shl 4).addRange(15)) {
                mc.theWorld?.setBlockState(it.pos, Blocks.air.defaultState)
                it.serverReplaced = true
                it.state = mc.theWorld.getBlockState(it.pos)
            }
        }
    }

    // TODO: MAKE THIS JSON PLS ITS SO BAD
    private val enderChests = mapOf(
        1 to arrayOf(
            BlockPos(69, 221, 37),
            BlockPos(69, 221, 36),
        ),
        2 to arrayOf(
            BlockPos(101, 169, 46),
            BlockPos(100, 169, 46),
            BlockPos(99, 169, 46)
        ),
        3 to arrayOf(
            BlockPos(56, 114, 111)
        )
    )

    private val glass = mapOf(
        1 to arrayOf(
            BlockPos(68, 221, 38),
            BlockPos(69, 221, 38)
        ),
        2 to arrayOf(
            BlockPos(102, 169, 47)
        ),
        3 to arrayOf(
            BlockPos(55, 114, 110),
            BlockPos(55, 114, 111)
        )
    )

    private val blocks = mapOf(
        1 to arrayOf(
            BlockPos(88, 220, 61),
            BlockPos(88, 219, 61),
            BlockPos(88, 218, 61),
            BlockPos(88, 217, 61),
            BlockPos(88, 216, 61),
            BlockPos(88, 215, 61),
            BlockPos(88, 214, 61),
            BlockPos(88, 213, 61),
            BlockPos(88, 212, 61),
            BlockPos(88, 211, 61),
            BlockPos(88, 210, 61),
            BlockPos(69, 220, 37),
            BlockPos(68, 220, 37)
        ),
        2 to arrayOf(
            BlockPos(88, 167, 41),
            BlockPos(89, 167, 41),
            BlockPos(91, 167, 41),
            BlockPos(92, 167, 41),
            BlockPos(93, 167, 41),
            BlockPos(94, 167, 41),
            BlockPos(95, 167, 41),
            BlockPos(88, 166, 41),
            BlockPos(89, 166, 41),
            BlockPos(90, 166, 41),
            BlockPos(91, 166, 41),
            BlockPos(92, 166, 41),
            BlockPos(93, 166, 41),
            BlockPos(94, 166, 41),
            BlockPos(95, 166, 41),
            BlockPos(88, 165, 41),
            BlockPos(89, 165, 41),
            BlockPos(90, 165, 41),
            BlockPos(91, 165, 41),
            BlockPos(92, 165, 41),
            BlockPos(93, 165, 41),
            BlockPos(94, 165, 41),
            BlockPos(95, 165, 41),
            BlockPos(88, 167, 40),
            BlockPos(89, 167, 40),
            BlockPos(90, 167, 40),
            BlockPos(91, 167, 40),
            BlockPos(92, 167, 40),
            BlockPos(93, 167, 40),
            BlockPos(94, 167, 40),
            BlockPos(95, 167, 40),
            BlockPos(88, 166, 40),
            BlockPos(89, 166, 40),
            BlockPos(90, 166, 40),
            BlockPos(91, 166, 40),
            BlockPos(92, 166, 40),
            BlockPos(93, 166, 40),
            BlockPos(94, 166, 40),
            BlockPos(95, 166, 40),
            BlockPos(88, 165, 40),
            BlockPos(89, 165, 40),
            BlockPos(90, 165, 40),
            BlockPos(91, 165, 40),
            BlockPos(92, 165, 40),
            BlockPos(93, 165, 40),
            BlockPos(94, 165, 40),
            BlockPos(95, 165, 40),
            BlockPos(101, 168, 47),
            BlockPos(101, 168, 46),
            BlockPos(101, 167, 47),
            BlockPos(101, 166, 47),
            BlockPos(101, 167, 46),
            BlockPos(101, 166, 46)
        ),
        3 to arrayOf(
            BlockPos(51, 114, 52),
            BlockPos(51, 114, 53),
            BlockPos(51, 114, 54),
            BlockPos(51, 114, 55),
            BlockPos(51, 114, 56),
            BlockPos(51, 114, 57),
            BlockPos(51, 114, 58),
            BlockPos(51, 115, 52),
            BlockPos(51, 115, 53),
            BlockPos(51, 115, 54),
            BlockPos(51, 115, 55),
            BlockPos(51, 115, 56),
            BlockPos(51, 115, 57),
            BlockPos(51, 115, 58),
            BlockPos(56, 113, 111),
            BlockPos(56, 112, 110),
            BlockPos(56, 112, 110),
            BlockPos(56, 111, 110)
        ),
        4 to arrayOf(
            BlockPos(54, 64, 72),
            BlockPos(54, 64, 73),
            BlockPos(54, 63, 73),
            BlockPos(54, 64, 74),
            BlockPos(54, 63, 74)
        )
    )


    private val toggleKeybind: Keybinding by KeybindSetting("Toggle Module", Keyboard.KEY_NONE, description = "Keybind to toggle the module on/off.").onPress {
        this.onKeybind()
    }
}