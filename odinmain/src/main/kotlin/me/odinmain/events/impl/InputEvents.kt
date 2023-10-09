package me.odinmain.events.impl

import net.minecraftforge.fml.common.eventhandler.Event

/**
 * @see me.odin.mixin.MixinMinecraft.keyPresses
 */
class PreKeyInputEvent(val keycode: Int) : Event()

/**
 * @see me.odin.mixin.MixinMinecraft.mouseKeyPresses
 */
class PreMouseInputEvent(val button: Int) : Event()