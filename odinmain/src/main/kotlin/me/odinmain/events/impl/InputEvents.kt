package me.odinmain.events.impl

import net.minecraftforge.fml.common.eventhandler.Event

/**
 * @see me.odinmain.mixin.MixinMinecraft.keyPresses
 */
class PreKeyInputEvent(val keycode: Int) : Event()

/**
 * @see me.odinmain.mixin.MixinMinecraft.mouseKeyPresses
 */
class PreMouseInputEvent(val button: Int) : Event()