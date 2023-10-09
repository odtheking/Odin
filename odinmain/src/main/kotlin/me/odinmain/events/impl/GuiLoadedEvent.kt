package me.odinmain.events.impl

import net.minecraft.inventory.ContainerChest
import net.minecraftforge.fml.common.eventhandler.Event

class GuiLoadedEvent(val name: String, val gui: ContainerChest) : Event()