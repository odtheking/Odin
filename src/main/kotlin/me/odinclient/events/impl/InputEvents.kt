package me.odinclient.events.impl

import net.minecraftforge.fml.common.eventhandler.Cancelable
import net.minecraftforge.fml.common.eventhandler.Event

/**
 * @see me.odinclient.mixin.MixinMinecraft.keyPresses
 */
class PreKeyInputEvent(val keycode: Int) : Event()

/**
 * @see me.odinclient.mixin.MixinMinecraft.mouseKeyPresses
 */
class PreMouseInputEvent(val button: Int) : Event()

open class ClickEvent : Event() {
    @Cancelable
    class LeftClickEvent : ClickEvent()

    @Cancelable
    class RightClickEvent : ClickEvent()
}