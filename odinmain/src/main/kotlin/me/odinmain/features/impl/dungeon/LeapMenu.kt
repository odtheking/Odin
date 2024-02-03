package me.odinmain.features.impl.dungeon

import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.events.impl.DrawGuiScreenEvent
import me.odinmain.events.impl.GuiClickEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.dungeon.LeapHelper.getPlayer
import me.odinmain.features.impl.dungeon.LeapHelper.leapHelperBossChatEvent
import me.odinmain.features.impl.dungeon.LeapHelper.leapHelperClearChatEvent
import me.odinmain.features.impl.dungeon.LeapHelper.worldLoad
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.ui.clickgui.util.ColorUtil
import me.odinmain.ui.util.*
import me.odinmain.ui.util.MouseUtils.getQuadrant
import me.odinmain.utils.name
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.Color
import me.odinmain.utils.skyblock.dungeon.DungeonUtils
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.Classes
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.EMPTY
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.leapTeammates
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.teammatesNoSelf
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.inventory.ContainerChest
import net.minecraft.util.ResourceLocation
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

object LeapMenu : Module(
    name = "Leap Menu",
    description = "Renders a custom leap menu when in the Spirit Leap gui.",
    category = Category.DUNGEON
) {
    val type: Int by SelectorSetting("Sorting", "Odin Sorting", arrayListOf("A-Z Class (BetterMap)", "A-Z Name", "Odin Sorting"), description = "How to sort the leap menu.")
    private val colorStyle: Boolean by DualSetting("Color Style", "Gray", "Color", default = false, description = "Which color style to use")
    private val roundedRect: Boolean by BooleanSetting("Rounded Rect", true, description = "Toggles the rounded rect for the gui.")
    //val priority: Int by SelectorSetting("Leap Helper Priority", "Berserker", arrayListOf("Archer", "Berserker", "Healer", "Mage", "Tank"), description = "Which player to prioritize in the leap helper.")

    private val leapHelperToggle: Boolean by BooleanSetting("Leap Helper", true)
    private val leapHelperColor: Color by ColorSetting("Leap Helper Color", default = Color.WHITE).withDependency { leapHelperToggle }
    val delay: Int by NumberSetting("Reset Leap Helper Delay", 30, 10.0, 120.0, 1.0).withDependency { leapHelperToggle }
    private val testPlayers: MutableList<DungeonUtils.DungeonPlayer> = mutableListOf(
        DungeonUtils.DungeonPlayer("Stiviaisd", Classes.Healer, ResourceLocation("textures/entity/steve.png")),
        DungeonUtils.DungeonPlayer("Odtheking", Classes.Archer, ResourceLocation("textures/entity/steve.png")),
        DungeonUtils.DungeonPlayer("Bonzi", Classes.Mage, ResourceLocation("textures/entity/steve.png")),
        DungeonUtils.DungeonPlayer("Cezar", Classes.Tank, ResourceLocation("textures/entity/steve.png"))
    )
    @SubscribeEvent
    fun onDrawScreen(event: DrawGuiScreenEvent) {
        val chest = (event.gui as? GuiChest)?.inventorySlots ?: return
        if (chest !is ContainerChest || chest.name != "Spirit Leap" || teammatesNoSelf.isEmpty()) return

        leapTeammates.forEachIndexed { index, it ->
            if (it == EMPTY) return@forEachIndexed
            GlStateManager.pushMatrix()
            GlStateManager.enableAlpha()
            scale(mc.displayWidth / 1920f, mc.displayHeight / 1080f)
            scale(1f / scaleFactor,  1f / scaleFactor)
            GlStateManager.color(255f, 255f, 255f, 255f)
            translate(
                (120f + (index % 2 * 910f)),
                (if (index >= 2) 615f else 165f),
                0f)
            mc.textureManager.bindTexture(it.locationSkin)
            val color = if (!colorStyle) it.clazz.color else Color.DARK_GRAY
            if ((it.name == if (DungeonUtils.inBoss) LeapHelper.leapHelperBoss else LeapHelper.leapHelperClear) && leapHelperToggle)
                roundedRectangle(-5, -5, 840, 360, leapHelperColor, if (roundedRect) 12f else 0f)
            dropShadow(0, 0, 780, 300, ColorUtil.moduleButtonColor, 15f, 10f, 10f, 10f, 10f)
            roundedRectangle(0, 0, 780, 300, color, if (roundedRect) 12f else 0f)

            GlStateManager.color(255f, 255f, 255f, 255f)
            Gui.drawScaledCustomSizeModalRect(30, 30, 8f, 8f, 8, 8, 240, 240, 64f, 64f)

            text(it.name, 265f, 155f, color, 48f)
            text(it.clazz.name, 270f, 210f, Color.WHITE, 30f)
            rectangleOutline(30, 30, 240, 240, color, 25f, 15f, 100f)

            GlStateManager.disableAlpha()
            GlStateManager.popMatrix()
        }
        event.isCanceled = true
    }

    @SubscribeEvent
    fun mouseClicked(event: GuiClickEvent) {
        if (event.gui !is GuiChest || event.gui.inventorySlots !is ContainerChest ||
            (event.gui.inventorySlots as ContainerChest).name != "Spirit Leap" || leapTeammates.isEmpty())  return

        val quadrant = getQuadrant()
        if ((type == 1 || type == 0) && leapTeammates.size < quadrant) return

        val playerToLeap = leapTeammates[quadrant - 1]
        if (playerToLeap.clazz == Classes.DEAD) return modMessage("This player is dead, can't leap.")

        val index = event.gui.inventorySlots.inventorySlots.subList(11, 16)
            .indexOfFirst { it?.stack?.displayName?.noControlCodes == playerToLeap.name }
                .takeIf { it != -1 } ?: return
        modMessage("Teleporting to ${playerToLeap.name}.")

        mc.playerController.windowClick(event.gui.inventorySlots.windowId, 11 + index, 2, 3, mc.thePlayer)
        event.isCanceled = true
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
}