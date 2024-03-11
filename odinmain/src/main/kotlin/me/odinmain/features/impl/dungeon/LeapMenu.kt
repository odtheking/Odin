package me.odinmain.features.impl.dungeon

import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.events.impl.DrawGuiScreenEvent
import me.odinmain.events.impl.GuiClickEvent
import me.odinmain.events.impl.GuiKeyPressEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.dungeon.LeapHelper.getPlayer
import me.odinmain.features.impl.dungeon.LeapHelper.leapHelperBossChatEvent
import me.odinmain.features.impl.dungeon.LeapHelper.leapHelperClearChatEvent
import me.odinmain.features.impl.dungeon.LeapHelper.worldLoad
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.ui.clickgui.animations.impl.EaseInOut
import me.odinmain.ui.clickgui.util.ColorUtil
import me.odinmain.ui.util.MouseUtils.getQuadrant
import me.odinmain.utils.equalsOneOf
import me.odinmain.utils.name
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.*
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.EMPTY
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.leapTeammates
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.inventory.ContainerChest
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.lwjgl.input.Keyboard

object LeapMenu : Module(
    name = "Leap Menu",
    description = "Renders a custom leap menu when in the Spirit Leap gui.",
    category = Category.DUNGEON
) {
    val type: Int by SelectorSetting("Sorting", "Odin Sorting", arrayListOf("Odin Sorting", "A-Z Class (BetterMap)", "A-Z Name", "No Sorting"), description = "How to sort the leap menu.")
    private val colorStyle: Boolean by DualSetting("Color Style", "Gray", "Color", default = false, description = "Which color style to use")
    private val roundedRect: Boolean by BooleanSetting("Rounded Rect", true, description = "Toggles the rounded rect for the gui.")
    private val useNumberKeys: Boolean by BooleanSetting("Use Number Keys", false, description = "Use keyboard keys to leap to the player you want, going from left to right, top to bottom.")
    private val topLeftKeybind: Keybinding by KeybindSetting("Top Left", Keyboard.KEY_1, "Used to click on the first person in the leap menu.").withDependency { useNumberKeys }
    private val topRightKeybind: Keybinding by KeybindSetting("Top Right", Keyboard.KEY_2, "Used to click on the second person in the leap menu.").withDependency { useNumberKeys }
    private val bottomLeftKeybind: Keybinding by KeybindSetting("Bottom Left", Keyboard.KEY_3, "Used to click on the third person in the leap menu.").withDependency { useNumberKeys }
    private val bottomRightKeybind: Keybinding by KeybindSetting("Bottom right", Keyboard.KEY_4, "Used to click on the fourth person in the leap menu.").withDependency { useNumberKeys }
    private val leapHelperToggle: Boolean by BooleanSetting("Leap Helper", true)
    private val leapHelperColor: Color by ColorSetting("Leap Helper Color", default = Color.WHITE, description = "Color of the Leap Helper highlight").withDependency { leapHelperToggle }
    val delay: Int by NumberSetting("Reset Leap Helper Delay", 30, 10.0, 120.0, 1.0, description = "Delay for clearing the leap helper highlight").withDependency { leapHelperToggle }

    private val hoveredAnims = List(4) { EaseInOut(200L) }
    private var hoveredQuadrant = -1
    private var previouslyHoveredQuadrant = -1


    @SubscribeEvent
    fun onDrawScreen(event: DrawGuiScreenEvent) {
        val chest = (event.gui as? GuiChest)?.inventorySlots ?: return
        if (chest !is ContainerChest || chest.name != "Spirit Leap" || leapTeammates.isEmpty()) return
        hoveredQuadrant = getQuadrant()
        if (hoveredQuadrant != previouslyHoveredQuadrant && previouslyHoveredQuadrant != -1) {
            hoveredAnims[hoveredQuadrant - 1].start()
            hoveredAnims[previouslyHoveredQuadrant - 1].start(true)
        }
        previouslyHoveredQuadrant = hoveredQuadrant

        leapTeammates.forEachIndexed { index, it ->
            if (it == EMPTY) return@forEachIndexed
            GlStateManager.pushMatrix()
            GlStateManager.enableAlpha()
            val currentRatio = 1920f / 1080f
            val newRatio = mc.displayWidth.toFloat() / mc.displayHeight.toFloat()

            if (currentRatio > newRatio) {
                val scaleFactor = mc.displayHeight.toFloat() / 1080f
                val scaledWidth = 1920f * scaleFactor
                val xOffset = (mc.displayWidth.toFloat() - scaledWidth) / 2
                scale(scaleFactor, scaleFactor)
                translate(xOffset, 0f)
            } else {
                val scaleFactor = mc.displayWidth.toFloat() / 1920f
                val scaledHeight = 1080f * scaleFactor
                val yOffset = (mc.displayHeight.toFloat() - scaledHeight) / 2
                scale(scaleFactor, scaleFactor)
                translate(0f, yOffset)
            }
            scale(1f / scaleFactor,  1f / scaleFactor)
            GlStateManager.color(255f, 255f, 255f, 255f)
            translate(
                (120f + (index % 2 * 910f)),
                (if (index >= 2) 615f else 165f),
                0f)
            mc.textureManager.bindTexture(it.locationSkin)
            val color = if (colorStyle) it.clazz.color else Color.DARK_GRAY
            if (it.name == (if (DungeonUtils.inBoss) LeapHelper.leapHelperBoss else LeapHelper.leapHelperClear) && leapHelperToggle)
                roundedRectangle(-25, -25, 830, 350, leapHelperColor, if (roundedRect) 12f else 0f)
            val box = Box(0, 0, 780, 300).expand(hoveredAnims[index].get(0f, 15f, hoveredQuadrant - 1 != index))
            dropShadow(box, 10f, 15f, if (getQuadrant() - 1 != index) ColorUtil.moduleButtonColor else Color.WHITE)
            roundedRectangle(box, color, if (roundedRect) 12f else 0f)

            GlStateManager.color(255f, 255f, 255f, 255f)
            Gui.drawScaledCustomSizeModalRect(30, 30, 8f, 8f, 8, 8, 240, 240, 64f, 64f)

            text(it.name, 265f, 155f, if (!colorStyle) it.clazz.color else Color.DARK_GRAY, 48f)
            text(it.clazz.name, 270f, 210f, Color.WHITE, 30f, shadow = true)
            rectangleOutline(30, 30, 240, 240, color, 25f, 15f, 100f)

            GlStateManager.disableAlpha()
            GlStateManager.popMatrix()
        }
        event.isCanceled = true
    }

    @SubscribeEvent
    fun mouseClicked(event: GuiClickEvent) {
        if (event.gui !is GuiChest || event.container !is ContainerChest || event.container.name != "Spirit Leap" || leapTeammates.isEmpty())  return

        val quadrant = getQuadrant()
        if ((type == 1 || type == 2 || type == 3) && leapTeammates.size < quadrant) return

        val playerToLeap = leapTeammates[quadrant - 1]
        if (playerToLeap.clazz.isDead) return modMessage("This player is dead, can't leap.")

        leapTo(playerToLeap.name, event.container)

        event.isCanceled = true
    }

    @SubscribeEvent
    fun keyTyped(event: GuiKeyPressEvent) {
        if (
            event.container !is ContainerChest ||
            event.container.name != "Spirit Leap" ||
            !event.keyCode.equalsOneOf(topLeftKeybind.key, topRightKeybind.key,
                bottomLeftKeybind.key, bottomRightKeybind.key) ||
            leapTeammates.isEmpty() ||
            !useNumberKeys
        ) return
        val keyCodeNumber = when (event.keyCode) {
            topLeftKeybind.key -> 1
            topRightKeybind.key -> 2
            bottomLeftKeybind.key -> 3
            bottomRightKeybind.key -> 4
            else -> return
        }
        val playerToLeap = if (keyCodeNumber - 1 > leapTeammates.size) return else leapTeammates[keyCodeNumber - 2]

        if (playerToLeap.clazz.isDead) return modMessage("This player is dead, can't leap.")

        leapTo(playerToLeap.name, event.container)

        event.isCanceled = true
    }

    private fun leapTo(name: String, containerChest: ContainerChest) {
        val index = containerChest.inventorySlots.subList(11, 16)
            .indexOfFirst { it?.stack?.displayName?.noControlCodes == name }
            .takeIf { it != -1 } ?: return modMessage("Cant find player $name. This shouldn't be possible!")
        modMessage("Teleporting to $name.")
        mc.playerController.windowClick(containerChest.windowId, 11 + index, 2, 3, mc.thePlayer)
    }

    @SubscribeEvent
    fun onChatPacket(event: ChatPacketEvent) {
        leapHelperClearChatEvent(event)
        leapHelperBossChatEvent(event)
    }

    @SubscribeEvent
    fun onWorldLoad(event: WorldEvent.Load) {
        worldLoad()
    }

    @SubscribeEvent
    fun onTick(event: TickEvent.ClientTickEvent) {
        getPlayer(event)
    }

    /*private val leapTeammates: MutableList<DungeonUtils.DungeonPlayer> = mutableListOf(
        DungeonUtils.DungeonPlayer("Stiviaisd", DungeonUtils.Classes.Healer),
        DungeonUtils.DungeonPlayer("Odtheking", DungeonUtils.Classes.Archer),
        DungeonUtils.DungeonPlayer("Bonzi", DungeonUtils.Classes.Mage),
        DungeonUtils.DungeonPlayer("Cezar", DungeonUtils.Classes.Tank)
    )*/
}