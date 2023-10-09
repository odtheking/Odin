package me.odinmain.events.impl

import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraftforge.fml.common.eventhandler.Event

class GuiClosedEvent(val gui: GuiContainer) : Event()