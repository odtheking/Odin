package me.odinmain.features.impl.dungeon

import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.events.impl.DrawGuiScreenEvent
import me.odinmain.events.impl.GuiClickEvent
import me.odinmain.events.impl.GuiClosedEvent
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.impl.dungeon.LeapHelper.getPlayer
import me.odinmain.features.impl.dungeon.LeapHelper.leapHelper
import me.odinmain.features.impl.dungeon.LeapHelper.leapHelperBossChatEvent
import me.odinmain.features.impl.dungeon.LeapHelper.leapHelperClearChatEvent
import me.odinmain.features.impl.dungeon.LeapHelper.worldLoad
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.ui.clickgui.ClickGUI
import me.odinmain.utils.name
import me.odinmain.utils.noControlCodes
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.gui.MouseUtils.getQuadrant
import me.odinmain.utils.render.gui.rect
import me.odinmain.utils.render.gui.scale
import me.odinmain.utils.render.gui.translate
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.Classes
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.EMPTY
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.leapTeammates
import me.odinmain.utils.skyblock.dungeon.DungeonUtils.teammatesNoSelf
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.entity.player.EntityPlayer
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
    private val blur: Boolean by BooleanSetting("Blur", false, description = "Toggles the background blur for the gui.")
    private val roundedRect: Boolean by BooleanSetting("Rounded Rect", true, description = "Toggles the rounded rect for the gui.")
    val priority: Int by SelectorSetting("Leap Helper Priority", "Berserker", arrayListOf("Archer", "Berserker", "Healer", "Mage", "Tank"), description = "Which player to prioritize in the leap helper.")

    private val leapHelperToggle: Boolean by BooleanSetting("Leap Helper", true)
    private val color: Color by ColorSetting("Leap Helper Color", default = Color.WHITE).withDependency { leapHelperToggle }
    val delay: Int by NumberSetting("Reset Leap Helper Delay", 30, 10.0, 120.0, 1.0).withDependency { leapHelperToggle }

    @SubscribeEvent
    fun onDrawScreen(event: DrawGuiScreenEvent) {
        val chest = (event.gui as? GuiChest)?.inventorySlots ?: return
        if (chest !is ContainerChest || chest.name != "Spirit Leap" || teammatesNoSelf.isEmpty()) return

        val width = mc.displayWidth / 1920f
        val height = mc.displayHeight / 1080f
        val sr = ScaledResolution(mc)

        leapTeammates.forEachIndexed { index, it ->
            if (it == EMPTY) return@forEachIndexed
            GlStateManager.pushMatrix()
            GlStateManager.enableAlpha()
            scale(width, height)
            scale(6f / sr.scaleFactor,  6f / sr.scaleFactor)
            GlStateManager.color(255f, 255f, 255f, 255f)
            translate(
                (30f + (index % 2 * 145f)),
                (if (index >= 2) 120f else 40f),
                0f)
            mc.textureManager.bindTexture(it.locationSkin)
            if (it.name == leapHelper && leapHelperToggle) rect(-5, -25, 230, 110, color, 9f)

            //Gui.drawRect(-5, -15, 120, 35, if (!colorStyle) Color.DARK_GRAY.rgba else it.clazz.color.rgba)
            if (roundedRect) rect(-10, -30, 250, 100, if (!colorStyle) Color.DARK_GRAY else it.clazz.color, 9f)
            else rect(-10, -30, 250, 100, if (!colorStyle) Color.DARK_GRAY else it.clazz.color, 0f)

            GlStateManager.color(255f, 255f, 255f, 255f)
            Gui.drawScaledCustomSizeModalRect(0, -10, 8f, 8f, 8, 8, 40, 40, 64f, 64f)

            mc.fontRendererObj.drawString(it.name, 44, 2, if (!colorStyle) it.clazz.color.rgba else Color.DARK_GRAY.rgba)
            mc.fontRendererObj.drawString(it.clazz.name, 44, 16, Color.WHITE.rgba )

            GlStateManager.disableAlpha()
            GlStateManager.popMatrix()

        }
        if (OpenGlHelper.shadersSupported && mc.renderViewEntity is EntityPlayer && blur) {
            ClickGUI.mc.entityRenderer.stopUseShader()
            ClickGUI.mc.entityRenderer.loadShader(ResourceLocation("shaders/post/blur.json"))
        }
        event.isCanceled = true
    }
    @SubscribeEvent
    fun onGuiClose(event: GuiClosedEvent) {
        mc.entityRenderer.stopUseShader()
    }

    @SubscribeEvent
    fun mouseClicked(event: GuiClickEvent) {
        if (event.gui !is GuiChest || event.gui.inventorySlots !is ContainerChest || (event.gui.inventorySlots as ContainerChest).name != "Spirit Leap")  return

        val quadrant = getQuadrant(event.x, event.y)

        if (leapTeammates.isEmpty()) return
        if ((type == 1 || type == 0) && leapTeammates.size < quadrant) return

        val playerToLeap = leapTeammates[quadrant - 1]

        if (playerToLeap.clazz == Classes.DEAD) return modMessage("This player is dead, can't leap.")

        val index = event.gui.inventorySlots.inventorySlots.subList(11, 16)
            .indexOfFirst { it?.stack?.displayName?.noControlCodes == playerToLeap.name }
                .takeIf { it != -1 } ?: return
        modMessage("Teleporting to ${playerToLeap.name}.")

        mc.playerController.windowClick(event.gui.inventorySlots.windowId, 11 + index, 1, 2, mc.thePlayer)
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