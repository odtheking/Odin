package me.odinclient.features.impl.floor7.p3

import me.odinclient.utils.skyblock.PlayerUtils.rightClick
import me.odinmain.events.impl.BlockChangeEvent
import me.odinmain.events.impl.PostEntityMetadata
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.ui.clickgui.util.ColorUtil.withAlpha
import me.odinmain.utils.*
import me.odinmain.utils.clock.Clock
import me.odinmain.utils.render.Color
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
    description = "Different features for the Simon Says device.",
    category = Category.FLOOR7,
    tag = TagType.RISKY
) {
    private val firstColor by ColorSetting("First Color", Color.GREEN.withAlpha(0.5f), allowAlpha = true, description = "The color of the first button.")
    private val secondColor by ColorSetting("Second Color", Color.ORANGE.withAlpha(0.5f), allowAlpha = true, description = "The color of the second button.")
    private val thirdColor by ColorSetting("Third Color", Color.RED.withAlpha(0.5f), allowAlpha = true, description = "The color of the buttons after the second.")
    private val style by SelectorSetting("Style", Renderer.DEFAULT_STYLE, Renderer.styles, description = Renderer.STYLE_DESCRIPTION)
    private val lineWidth by NumberSetting("Line Width", 2f, 0.1f, 10f, 0.1f, description = "The width of the box's lines.")
    private val depthCheck by BooleanSetting("Depth check", false, description = "Boxes show through walls.")
    private val start by BooleanSetting("Start", false, description = "Automatically starts the device when it can be started.")
    private val startClicks by NumberSetting("Start Clicks", 3, 1, 10, description = "Amount of clicks to start the device.").withDependency { start }
    private val startClickDelay by NumberSetting("Start Click Delay", 3, 1, 5, description = "Delay between each start click.").withDependency { start }
    private val triggerBot by BooleanSetting("Triggerbot", false, description = "Automatically clicks the correct button when you look at it.")
    private val triggerBotDelay by NumberSetting("Triggerbot Delay", 200L, 70, 500, unit = "ms", description = "The delay between each click.").withDependency { triggerBot }
    private val autoSS by BooleanSetting("Auto SS", false, description = "Automatically clicks the correct button when you are in range.")
    private val autoSSDelay by NumberSetting("Delay Between Clicks", 200L, 50, 500, unit = "ms", description = "The delay between each click.").withDependency { autoSS }
    private val autoSSRotateTime by NumberSetting("Rotate Time", 150, 0, 400, unit = "ms", description = "The time it takes to rotate to the correct button.").withDependency { autoSS }
    private val blockWrong by BooleanSetting("Block Wrong Clicks", false, description = "Blocks Any Wrong Clicks (sneak to disable).")

    private val triggerBotClock = Clock(triggerBotDelay)
    private val firstClickClock = Clock(800)
    private val autoSSClock = Clock(autoSSDelay)
    private var autoSSClickInQueue = false
    private val autoSSLastClickClock = Clock(1000)

    private val firstButton = BlockPos(110, 121, 91)
    private val clickInOrder = ArrayList<BlockPos>()
    private var clickNeeded = 0

    private fun start() {
        if (mc.objectMouseOver?.blockPos == firstButton)
            repeat(startClicks) {
                runIn(it * startClickDelay) {
                    rightClick()
                }
            }
    }

    init {
        onMessage(Regex("\\[BOSS] Goldor: Who dares trespass into my domain\\?"), { start && enabled }) {
            start()
            modMessage("Starting Simon Says")
        }

        onWorldLoad {
            clickInOrder.clear()
            clickNeeded = 0
        }
    }

    override fun onKeybind() = start()

    @SubscribeEvent
    fun onBlockChange(event: BlockChangeEvent) = with (event) {
        if (DungeonUtils.getF7Phase() != M7Phases.P3) return

        if (pos == firstButton && updated.block == Blocks.stone_button && updated.getValue(BlockButtonStone.POWERED)) {
            clickInOrder.clear()
            clickNeeded = 0
            return
        }

        if (pos.y !in 120..123 || pos.z !in 92..95) return

        when (pos.x) {
            111 ->
                if (updated.block == Blocks.obsidian && old.block == Blocks.sea_lantern && pos !in clickInOrder) clickInOrder.add(pos)

            110 ->
                if (updated.block == Blocks.air) {
                    clickInOrder.clear()
                    clickNeeded = 0
                } else if (old.block == Blocks.stone_button && updated.getValue(BlockButtonStone.POWERED)) {
                    val index = clickInOrder.indexOf(pos.add(1, 0, 0)) + 1
                    clickNeeded = if (index >= clickInOrder.size) 0 else index
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
        if (clickInOrder[clickNeeded] != pos.east()) return
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
            1, 0f, 90f, 90f, (if (isInSSRange) Color.GREEN else Color.ORANGE).withAlpha(.5f)
        )

        if (
            !isInSSRange ||
            !autoSSClock.hasTimePassed(autoSSDelay) ||
            clickInOrder.isEmpty() ||
            mc.currentScreen != null ||
            autoSSClickInQueue ||
            clickNeeded >= clickInOrder.size ||
            !autoSSLastClickClock.hasTimePassed()
        ) return

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
        if (
            event.pos == null ||
            event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK ||
            event.world != mc.theWorld ||
            !blockWrong ||
            mc.thePlayer?.isSneaking == true ||
            event.pos.x != 110 || event.pos.y !in 120..123 || event.pos.z !in 92..95
        ) return

        if (event.pos.east() != clickInOrder.getOrNull(clickNeeded)) event.isCanceled = true
    }

    @SubscribeEvent
    fun onRenderWorld(event: RenderWorldLastEvent) {
        if (DungeonUtils.getF7Phase() != M7Phases.P3) return

        if (autoSS) autoSS()
        if (clickNeeded >= clickInOrder.size) return

        if (triggerBot) triggerBot()

        for (index in clickNeeded until clickInOrder.size) {
            with(clickInOrder[index]) {
                Renderer.drawStyledBox(AxisAlignedBB(x + 0.05, y + 0.37, z + 0.3, x - 0.15, y + 0.63, z + 0.7), when (index) {
                    clickNeeded -> firstColor
                    clickNeeded + 1 -> secondColor
                    else -> thirdColor
                }, style, lineWidth, depthCheck)
            }
        }
    }
}