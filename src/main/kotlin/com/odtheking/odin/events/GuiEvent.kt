package com.odtheking.odin.events

import com.odtheking.odin.events.core.CancellableEvent
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.Slot

abstract class GuiEvent(val screen: Screen) : CancellableEvent() {

    class Open(screen: Screen) : GuiEvent(screen)

    class Close(screen: Screen) : GuiEvent(screen)

    class SlotClick(screen: Screen, val slotId: Int, val button: Int) : GuiEvent(screen)

    class SlotUpdate(screen: Screen, val packet: ClientboundContainerSetSlotPacket, val menu: AbstractContainerMenu) : GuiEvent(screen)

    class MouseClick(screen: Screen, val click: MouseButtonEvent, val doubled: Boolean) : GuiEvent(screen)

    class MouseRelease(screen: Screen, val click: MouseButtonEvent) : GuiEvent(screen)

    class KeyPress(screen: Screen, val input: KeyEvent) : GuiEvent(screen)

    class Draw(screen: Screen, val guiGraphics: GuiGraphics, val mouseX: Int, val mouseY: Int) : GuiEvent(screen)

    class DrawBackground(screen: Screen, val guiGraphics: GuiGraphics, val mouseX: Int, val mouseY: Int) : GuiEvent(screen)

    class DrawSlot(screen: Screen, val guiGraphics: GuiGraphics, val slot: Slot) : GuiEvent(screen)

    class CustomTermGuiClick(screen: Screen, val slot: Int, val button: Int) : GuiEvent(screen)

    class DrawTooltip(screen: Screen, val guiGraphics: GuiGraphics, val mouseX: Int, val mouseY: Int) : GuiEvent(screen)
}