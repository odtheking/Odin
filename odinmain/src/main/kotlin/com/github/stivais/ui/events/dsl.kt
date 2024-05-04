@file:Suppress("UNCHECKED_CAST")

package com.github.stivais.ui.events

import com.github.stivais.ui.elements.Element


fun <E : Element> E.onClick(button: Int? = 0, block: Mouse.Clicked.() -> Boolean): E {
    registerEvent(Mouse.Clicked(button), block as Event.() -> Boolean)
    return this
}

fun <E : Element> E.onRelease(button: Int, block: Mouse.Released.() -> Unit): E {
    registerEvent(Mouse.Released(button)) {
        (this as Mouse.Released).block()
        true
    }
    return this
}

fun <E : Element> E.onMouseEnter(block: Mouse.Entered.() -> Boolean): E {
    registerEvent(Mouse.Entered, block as Event.() -> Boolean)
    return this
}

fun <E : Element> E.onMouseExit(block: Mouse.Exited.() -> Boolean): E {
    registerEvent(Mouse.Exited, block as Event.() -> Boolean)
    return this
}

fun <E : Element> E.onMouseEnterExit(block: Event.() -> Boolean): E {
    registerEvent(Mouse.Entered, block)
    registerEvent(Mouse.Exited, block)
    return this
}

fun <E : Element> E.onMouseMove(block: Mouse.Moved.() -> Boolean): E {
    registerEvent(Mouse.Moved, block as Event.() -> Boolean)
    return this
}

fun <E : Element> E.onKeycodePressed(keycode: Int = -1, block: Key.CodePressed.() -> Boolean): E {
    registerEvent(Key.CodePressed(keycode, true), block as Event.() -> Boolean)
    return this
}

//fun <E : Element> E.onKeyRelease(keycode: Int? = null, block: Key.CodeReleased.() -> Boolean): E {
//    registerEvent(Key.CodeReleased(keycode), false, block as Event.() -> Boolean)
//    return this
//}

fun <E : Element> E.onFocusGain(block: Focused.Gained.() -> Unit): E {
    registerEvent(Focused.Gained) {
        (this as Focused.Gained).block()
        true
    }
    return this
}

fun <E : Element> E.onFocusLost(block: Focused.Lost.() -> Unit): E {
    registerEvent(Focused.Lost) {
        Focused.Lost.block()
        true
    }
    return this
}