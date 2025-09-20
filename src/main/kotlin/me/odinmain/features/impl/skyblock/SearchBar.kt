package me.odinmain.features.impl.skyblock

import me.odinmain.clickgui.settings.impl.ColorSetting
import me.odinmain.events.impl.GuiEvent
import me.odinmain.features.Module
import me.odinmain.utils.render.Color.Companion.withAlpha
import me.odinmain.utils.render.Colors
import me.odinmain.utils.skyblock.lore
import me.odinmain.utils.ui.SearchBar
import me.odinmain.utils.ui.mouseX
import me.odinmain.utils.ui.mouseY
import me.odinmain.utils.ui.rendering.NVGRenderer
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object SearchBar: Module(
    name = "Search Bar",
    description = "Search bar for inventories."
) {
    private val highlightColor by ColorSetting("Highlight Color", Colors.MINECRAFT_BLUE.withAlpha(0.5f), true, desc = "Color of the highlight for matching items.")

    private val searchBar = SearchBar()

    @SubscribeEvent
    fun onRenderInventory(event: GuiEvent.DrawGuiForeground) {
        NVGRenderer.beginFrame(mc.displayWidth.toFloat(), mc.displayHeight.toFloat())

        searchBar.draw(mc.displayWidth / 2f - 175f, mc.displayHeight - 110f, 350f, 40f, mouseX, mouseY)

        val scale = ScaledResolution(mc).scaleFactor.toFloat()
        NVGRenderer.scale(scale, scale)

        if (searchBar.currentSearch.isNotEmpty()) {
            for (inventorySlot in event.gui.inventorySlots.inventorySlots) {
                if (inventorySlot?.stack?.displayName?.contains(searchBar.currentSearch, ignoreCase = true) == true || inventorySlot?.stack?.lore?.any { it.contains(searchBar.currentSearch, ignoreCase = true) } == true) {
                    val slotX = inventorySlot.xDisplayPosition.toFloat() + event.guiLeft
                    val slotY = inventorySlot.yDisplayPosition.toFloat() + event.guiTop
                    NVGRenderer.rect(slotX, slotY, 16f, 16f, highlightColor.rgba)
                }
            }
        }

        NVGRenderer.scale(1f / scale, 1f / scale)
        NVGRenderer.endFrame()
    }

    @SubscribeEvent
    fun onGuiMouseClick(event: GuiEvent.MouseClick) {
        if (event.gui !is GuiContainer) return
        if (searchBar.mouseClicked(mouseX, mouseY, event.button)) {
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onGuiMouseRelease(event: GuiEvent.MouseRelease) {
        if (event.gui !is GuiContainer) return
        searchBar.mouseReleased()
    }

    @SubscribeEvent
    fun onGuiKeyPress(event: GuiEvent.KeyPress) {
        if (event.gui !is GuiContainer) return
        if (searchBar.keyTyped(event.char) || searchBar.keyPressed(event.key)) {
            event.isCanceled = true
        }
    }
}