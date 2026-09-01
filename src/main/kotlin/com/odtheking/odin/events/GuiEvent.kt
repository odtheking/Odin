package com.odtheking.odin.events

import com.odtheking.odin.events.core.CancellableEvent
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.world.inventory.Slot

abstract class GuiEvent(val screen: Screen) : CancellableEvent() {

    class Render(screen: Screen, val guiGraphics: GuiGraphicsExtractor, val mouseX: Int, val mouseY: Int) : GuiEvent(screen)

    class SlotClick(screen: Screen, val slotIndex: Int, val button: Int) : GuiEvent(screen)

    class RenderSlot(screen: Screen, val guiGraphics: GuiGraphicsExtractor, val slot: Slot) : GuiEvent(screen)

    class CustomTermGuiClick(screen: Screen, val slot: Int, val button: Int) : GuiEvent(screen)

    class DrawTooltip(screen: Screen, val guiGraphics: GuiGraphicsExtractor, val mouseX: Int, val mouseY: Int) : GuiEvent(screen)
}