package me.odinmain.events.impl

import net.minecraftforge.fml.common.eventhandler.Event

class PreKeyInputEvent(val keycode: Int) : Event()

class PreMouseInputEvent(val button: Int) : Event()