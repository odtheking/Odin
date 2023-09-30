package me.odinclient.features.impl.floor7.p3

import me.odinclient.events.impl.BlockChangeEvent
import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.Setting.Companion.withDependency
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.features.settings.impl.NumberSetting
import me.odinclient.ui.clickgui.util.ColorUtil.withAlpha
import me.odinclient.utils.clock.Clock
import me.odinclient.utils.render.Color
import me.odinclient.utils.render.world.RenderUtils
import me.odinclient.utils.skyblock.ChatUtils.devMessage
import me.odinclient.utils.skyblock.ChatUtils.modMessage
import me.odinclient.utils.skyblock.PlayerUtils.rightClick
import me.odinclient.utils.skyblock.dungeon.DungeonUtils
import net.minecraft.block.BlockButtonStone
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.item.EntityItem
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.EntityJoinWorldEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object SimonSays : Module(
    name = "Simon Says",
    description = "Different features for the Simon Says puzzle in f7/m7.",
    category = Category.FLOOR7,
    tag = TagType.NEW
) {
    private val solver: Boolean by BooleanSetting("Solver")
    private val start: Boolean by BooleanSetting("Start", default = true, description = "Starts the device when it can be started.")
    private val startClicks: Int by NumberSetting("Start Clicks", 1, 1, 10).withDependency { start }
    private val startClickDelay: Int by NumberSetting("Start Click Delay", 3, 1, 5).withDependency { start }
    private val triggerBot: Boolean by BooleanSetting("Triggerbot")
    private val delay: Long by NumberSetting<Long>("Delay", 200, 70, 500).withDependency { triggerBot }
    private val fullBlock: Boolean by BooleanSetting("Full Block (needs SBC)", false).withDependency { triggerBot }
    private val clearAfter: Boolean by BooleanSetting("Clear After", false, description = "Clears the clicks when showing next, should work better with ss skip, but will be less consistent")

    private val triggerBotClock = Clock(delay)
    private val firstClickClock = Clock(800)

    private val firstButton = BlockPos(110, 121, 91)
    private val clickInOrder = ArrayList<BlockPos>()
    private var clickNeeded = 0
    private var currentPhase = 0
    private val phaseClock = Clock(500)

    init {
        onMessage("\\[BOSS] Goldor: Who dares tresspass into my domain\\?".toRegex(), { solver && enabled}) {
            if (mc.objectMouseOver?.blockPos == firstButton)
                repeat(startClicks) { rightClick() }
        }
    }

    override fun onKeybind() {
        if (mc.objectMouseOver?.blockPos == firstButton)
            repeat(startClicks) {
                runIn(it * startClickDelay) {
                    rightClick()
                }
            }
    }

    @SubscribeEvent
    fun onBlockChange(event: BlockChangeEvent) {
        if (DungeonUtils.getPhase() != 3) return
        val pos = event.pos
        val old = event.old
        val state = event.update

        if (pos == firstButton && state.block == Blocks.stone_button && state.getValue(BlockButtonStone.POWERED)) {
            clickInOrder.clear()
            clickNeeded = 0
            currentPhase = 0
            return
        }

        if (pos.y !in 120..123 || pos.z !in 92..95) return

        if (pos.x == 111 && state.block == Blocks.sea_lantern && pos !in clickInOrder) {
            clickInOrder.add(pos)
        } else if (pos.x == 110) {
            if (state.block == Blocks.air) {
                clickNeeded = 0
                if (phaseClock.hasTimePassed()) {
                    currentPhase++
                    phaseClock.update()
                }
                modMessage(currentPhase)
                if (clearAfter) clickInOrder.clear()
            } else if (state.block == Blocks.stone_button) {
                if (old.block == Blocks.air && clickInOrder.size > currentPhase) {
                    modMessage("was skipped!?!?!")
                }
                if (old.block == Blocks.stone_button && state.getValue(BlockButtonStone.POWERED)) {
                    val index = clickInOrder.indexOf(pos.add(1, 0, 0)) + 1
                    clickNeeded = if (index >= clickInOrder.size) 0 else index
                }
            }
        }
    }

    @SubscribeEvent
    fun onEntityJoin(event: EntityJoinWorldEvent) {
        if (event.entity !is EntityItem) return
        val item = event.entity as EntityItem
        if (Item.getIdFromItem(item.entityItem.item) != 77) return
        devMessage("AAA")
        item.thrower
    }

    private fun triggerBot() {
        if (!triggerBotClock.hasTimePassed(delay) || clickInOrder.size == 0) return
        val pos = mc.objectMouseOver?.blockPos ?: return
        if (clickInOrder[clickNeeded] != pos.add(1, 0, 0) && !(fullBlock && clickInOrder[clickNeeded] == pos)) return
        if (clickNeeded == 0) { // Stops spamming the first button and breaking the puzzle.
            if (!firstClickClock.hasTimePassed()) return
            rightClick()
            firstClickClock.update()
            return
        }
        rightClick()
        triggerBotClock.update()
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (clickNeeded >= clickInOrder.size) return

        if (triggerBot) triggerBot()

        if (!solver) return
        GlStateManager.disableCull()
        val (viewerX, viewerY, viewerZ) = RenderUtils.viewerPos

        for (index in clickNeeded until clickInOrder.size) {
            val pos = clickInOrder[index]
            val x = pos.x - viewerX - .125
            val y = pos.y - viewerY + .3125
            val z = pos.z - viewerZ + .25
            val color = when (index) {
                clickNeeded -> Color(0, 170, 0)
                clickNeeded + 1 -> Color(255, 170, 0)
                else -> Color(170, 0, 0)
            }.withAlpha(.5f)
            RenderUtils.drawFilledBox(AxisAlignedBB(x, y, z, x + .1875, y + .375, z + .5), color)
        }
        GlStateManager.enableCull()
    }

    @SubscribeEvent
    fun onWorldChange(event: WorldEvent.Load) {
        clickInOrder.clear()
        clickNeeded = 0
        currentPhase = 0
    }
}