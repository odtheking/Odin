package me.odinclient.features.impl.floor7.p3

import me.odinclient.utils.skyblock.PlayerUtils.rightClick
import me.odinmain.clickgui.settings.Setting.Companion.withDependency
import me.odinmain.clickgui.settings.impl.BooleanSetting
import me.odinmain.clickgui.settings.impl.ColorSetting
import me.odinmain.clickgui.settings.impl.NumberSetting
import me.odinmain.clickgui.settings.impl.SelectorSetting
import me.odinmain.events.impl.BlockChangeEvent
import me.odinmain.events.impl.PostEntityMetadata
import me.odinmain.features.Module
import me.odinmain.utils.*
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.render.Color.Companion.withAlpha
import me.odinmain.utils.render.Colors
import me.odinmain.utils.render.Renderer
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.M7Phases
import me.odinmain.utils.skyblock.getBlockIdAt
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.block.BlockButtonStone
import net.minecraft.entity.item.EntityItem
import net.minecraft.init.Blocks
import net.minecraft.item.Item
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object SimonSays : Module(
    name = "Simon Says",
    description = "Different features for the Simon Says device."
) {
    private val firstColor by ColorSetting("First Color", Colors.MINECRAFT_GREEN.withAlpha(0.5f), allowAlpha = true, desc = "The color of the first button.")
    private val secondColor by ColorSetting("Second Color", Colors.MINECRAFT_GOLD.withAlpha(0.5f), allowAlpha = true, desc = "The color of the second button.")
    private val thirdColor by ColorSetting("Third Color", Colors.MINECRAFT_RED.withAlpha(0.5f), allowAlpha = true, desc = "The color of the buttons after the second.")
    private val style by SelectorSetting("Style", Renderer.DEFAULT_STYLE, Renderer.styles, desc = Renderer.STYLE_DESCRIPTION)
    private val lineWidth by NumberSetting("Line Width", 2f, 0.1f, 10f, 0.1f, desc = "The width of the box's lines.")
    private val depthCheck by BooleanSetting("Depth check", false, desc = "Boxes show through walls.")
    private val start by BooleanSetting("Start", false, desc = "Automatically starts the device when it can be started.")
    private val startClicks by NumberSetting("Start Clicks", 3, 1, 10, desc = "Amount of clicks to start the device.").withDependency { start }
    private val startClickDelay by NumberSetting("Start Click Delay", 3, 1, 25, unit = "ticks", desc = "Delay between each start click.").withDependency { start }
    private val triggerBot by BooleanSetting("Triggerbot", false, desc = "Automatically clicks the correct button when you look at it.")
    private val triggerBotDelay by NumberSetting("Triggerbot Delay", 200L, 70, 500, unit = "ms", desc = "The delay between each click.").withDependency { triggerBot }
    private val autoSS by BooleanSetting("Auto SS", false, desc = "Automatically clicks the correct button when you are in range.")
    private val autoSSDelay by NumberSetting("Delay Between Clicks", 200L, 50, 500, unit = "ms", desc = "The delay between each click.").withDependency { autoSS }
    private val autoSSRotateTime by NumberSetting("Rotate Time", 150, 0, 400, unit = "ms", desc = "The time it takes to rotate to the correct button.").withDependency { autoSS }
    private val blockWrong by BooleanSetting("Block Wrong Clicks", false, desc = "Blocks Any Wrong Clicks (sneak to disable).")
    private val optimizeSolution by BooleanSetting("Optimized Solution", true, desc = "Use optimized solution, might fix ss-skip")
    private val faceToFirst by BooleanSetting("Face To First", false, desc = "Face to the first button after the last button is click (except the last phase was clicked)").withDependency { autoSS && optimizeSolution }

    private val triggerBotClock = Clock(triggerBotDelay)
    private val firstClickClock = Clock(800)
    private val autoSSClock = Clock(autoSSDelay)
    private var autoSSClickInQueue = false
    private val autoSSLastClickClock = Clock(1000)

    private val startButton = BlockPos(110, 121, 91)
    private val clickInOrder = ArrayList<BlockPos>()
    private var clickNeeded = 0
    private var firstButton: BlockPos? = null

    private fun start() {
        if (mc.objectMouseOver?.blockPos == startButton)
            repeat(startClicks) {
                runIn(it * startClickDelay) {
                    rightClick()
                }
            }
    }

    private fun resetSolution(keepFirst: Boolean = false) {
        clickInOrder.clear()
        clickNeeded = 0

        if (keepFirst) return
        firstButton = null
    }

    init {
        onMessage(Regex("^\\[BOSS] Goldor: Who dares trespass into my domain\\?$"), { start && enabled }) {
            start()
            modMessage("Starting Simon Says")
        }

        onWorldLoad {
            resetSolution()
        }
    }

    override fun onKeybind() = start()

    @SubscribeEvent
    fun onBlockChange(event: BlockChangeEvent) = with (event) {
        if (DungeonUtils.getF7Phase() != M7Phases.P3) return

        if (pos == startButton && updated.block == Blocks.stone_button && updated.getValue(BlockButtonStone.POWERED)) {
            if (!optimizeSolution) resetSolution()
            return
        }

        if (pos.y !in 120..123 || pos.z !in 92..95) return

        when (pos.x) {
            111 ->
                if (optimizeSolution) {
                    if (updated.block == Blocks.sea_lantern && old.block == Blocks.obsidian) {
                        if (clickInOrder.isEmpty()) {
                            firstButton = pos
                            clickInOrder.add(pos)
                        } else if (pos !in clickInOrder) clickInOrder.add(pos)
                    }
                } else if (updated.block == Blocks.obsidian && old.block == Blocks.sea_lantern && pos !in clickInOrder) clickInOrder.add(pos)

            110 ->
                if (updated.block == Blocks.air) {
                    if (!optimizeSolution) resetSolution()
                } else if (old.block == Blocks.stone_button && updated.getValue(BlockButtonStone.POWERED)) {
                    clickNeeded = clickInOrder.indexOf(pos.add(1, 0, 0)) + 1
                    if (clickNeeded >= clickInOrder.size) if (optimizeSolution) resetSolution(clickNeeded < 5) else clickNeeded = 0
                }
        }
    }

    @SubscribeEvent
    fun onPostMetadata(event: PostEntityMetadata) {
        if (DungeonUtils.getF7Phase() != M7Phases.P3) return
        val (x, y, z) = (mc.theWorld?.getEntityByID(event.packet.entityId) as? EntityItem)?.takeIf { Item.getIdFromItem(it.entityItem?.item) == 77 }?.positionVector?.floorVec() ?: return
        val index = clickInOrder.indexOf(BlockPos(x, y, z).east())
        if (index == 2 && clickInOrder.size == 3) clickInOrder.removeFirst()
        else if (index == 0 && clickInOrder.size == 2) clickInOrder.reverse()
    }

    private fun triggerBot() {
        if (!triggerBotClock.hasTimePassed(triggerBotDelay) || clickInOrder.isEmpty() || mc.currentScreen != null) return
        val pos = mc.objectMouseOver?.blockPos ?: return
        if (clickInOrder.getOrNull(clickNeeded) != pos.east()) return
        if (clickNeeded == 0) { // Stops spamming the first button and breaking the puzzle.
            if (!firstClickClock.hasTimePassed()) return
            firstClickClock.update()
            rightClick()
            return
        }
        triggerBotClock.update()
        rightClick()
    }

    private fun autoSS() {
        val isInSSRange = mc.thePlayer.getDistanceSqToCenter(BlockPos(108, 120, 93)) <= 1.45 * 1.45
        Renderer.drawCylinder(
            Vec3(108.5, 120.0, 93.5), 1.45f, 1.45f, .6f, 35,
            1, 0f, 90f, 90f, (if (isInSSRange) Colors.MINECRAFT_GREEN else Colors.MINECRAFT_GOLD).withAlpha(.5f)
        )

        if (!isInSSRange || !autoSSClock.hasTimePassed(autoSSDelay) || mc.currentScreen != null || autoSSClickInQueue || !autoSSLastClickClock.hasTimePassed()) return

        if (clickInOrder.isEmpty() || clickNeeded >= clickInOrder.size) {
            if (!faceToFirst) return
            firstButton?.let {
                firstButton = null
                val (_, yaw, pitch) = getDirectionToVec3(it.toVec3().addVec(x = -0.1, y = .5, z = .5))
                autoSSClickInQueue = true
                smoothRotateTo(yaw, pitch, autoSSRotateTime) {
                    autoSSClickInQueue = false
                }
            }
            return
        }

        val buttonToClick = clickInOrder[clickNeeded].takeIf { getBlockIdAt(it.west()) == 77 } ?: return
        val (_, yaw, pitch) = getDirectionToVec3(buttonToClick.toVec3().addVec(x = -0.1, y = .5, z = .5))
        autoSSClickInQueue = true
        smoothRotateTo(yaw, pitch, autoSSRotateTime) {
            if (clickNeeded == 4) autoSSLastClickClock.update()
            autoSSClickInQueue = false
            autoSSClock.update()
            rightClick()
        }
    }

    @SubscribeEvent
    fun onInteract(event: PlayerInteractEvent) {
        if (event.pos == null || event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK || event.world != mc.theWorld) return

        if (event.pos == startButton) {
            if (optimizeSolution) resetSolution()
            return
        }

        if (
            blockWrong &&
            mc.thePlayer?.isSneaking == false &&
            event.pos.x == 110 && event.pos.y in 120..123 && event.pos.z in 92..95 &&
            event.pos.east() != clickInOrder.getOrNull(clickNeeded)
        ) event.isCanceled = true
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (DungeonUtils.getF7Phase() != M7Phases.P3) return

        if (autoSS) autoSS()
        if (clickNeeded >= clickInOrder.size) return

        if (triggerBot) triggerBot()

        for (index in clickNeeded until clickInOrder.size) {
            with(clickInOrder[index]) {
                Renderer.drawStyledBox(
                    AxisAlignedBB(x + 0.05, y + 0.37, z + 0.3, x - 0.15, y + 0.63, z + 0.7), when (index) {
                        clickNeeded -> firstColor
                        clickNeeded + 1 -> secondColor
                        else -> thirdColor
                    }, style, lineWidth, depthCheck
                )
            }
        }
        clickInOrder.forEachIndexed { index, pos ->
            with(pos) {
                Renderer.drawStringInWorld(index.toString(), Vec3(x + 0.05, y + 0.5, z + 0.5), Colors.WHITE)
            }
        }
    }
}