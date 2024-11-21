package me.odinmain.events.impl

import net.minecraftforge.fml.common.eventhandler.Event

data class PreKeyInputEvent(val keycode: Int) : Event()

data class PreMouseInputEvent(val button: Int) : Event()