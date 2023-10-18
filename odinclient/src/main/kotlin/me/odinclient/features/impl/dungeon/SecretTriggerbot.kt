package me.odinclient.features.impl.dungeon

import me.odinclient.utils.skyblock.PlayerUtils.rightClick
import me.odinmain.OdinMain
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.floor
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.block.BlockSkull
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.tileentity.TileEntitySkull
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object SecretTriggerbot : Module(
    name = "Secret Triggerbot",
    description = "Automatically gets secrets when looking at them.",
    category = Category.DUNGEON,
    tag = TagType.NEW
) {
    private val delay: Long by NumberSetting<Long>("Delay", 200, 70, 500)
    private val crystalHollowsChests: Boolean by BooleanSetting("Crystal Hollows Chests", true, description = "Opens chests in crystal hollows when looking at them")
    private val inBoss: Boolean by BooleanSetting("In Boss", true, description = "Makes the triggerbot work in dungeon boss aswell.")
    private val triggerBotClock = Clock(delay)
    private var clickedPositions = mutableMapOf<BlockPos, Long>()
    private const val WITHER_ESSENCE_ID = "26bb1a8d-7c66-31c6-82d5-a9c04c94fb02"
    private val debugMsgs: Boolean by BooleanSetting("Debug Messages", false)

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (
            !triggerBotClock.hasTimePassed(delay) ||
            DungeonUtils.currentRoom?.data?.name.equalsOneOf("Water Board", "Three Weirdos") ||
            mc.currentScreen != null
        ) return
        val pos = mc.objectMouseOver?.blockPos ?: return
        val state = mc.theWorld.getBlockState(pos) ?: return
        clickedPositions = clickedPositions.filter { it.value + 1000L < System.currentTimeMillis() }.toMutableMap()
        if (pos.x in 58..62 && pos.y in 133..136 && pos.z == 142) return // looking at lights device

        if (crystalHollowsChests && LocationUtils.currentArea == "Crystal Hollows" && state.block == Blocks.chest && !clickedPositions.containsKey(pos)) {
            rightClick()
            triggerBotClock.update()
            clickedPositions.plus(pos to System.currentTimeMillis())
            return
        } else if (!DungeonUtils.inDungeons || (!inBoss && DungeonUtils.inBoss)) return
        else if (isSecret(state, pos) && !clickedPositions.containsKey(pos)) {
            rightClick()
            triggerBotClock.update()
            clickedPositions.plus(pos to System.currentTimeMillis())
            if (debugMsgs) {
                val x = ((OdinMain.mc.thePlayer.posX + 200) / 32).floor().toInt()
                val z = ((OdinMain.mc.thePlayer.posZ + 200) / 32).floor().toInt()
                val xPos = -185 + x * 32
                val zPos = -185 + z * 32
                DungeonUtils.scanRoom(xPos, zPos, printDebug = true)
            }
        }
    }

    private fun isSecret(state: IBlockState, pos: BlockPos): Boolean {
        if (state.block == Blocks.chest || state.block == Blocks.trapped_chest || state.block == Blocks.lever) return true
        else if (state.block is BlockSkull) {
            val tile = mc.theWorld.getTileEntity(pos) ?: return false
            if (tile !is TileEntitySkull) return false
            return tile.playerProfile?.id.toString() == WITHER_ESSENCE_ID
        }
        return false
    }
}