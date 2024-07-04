package com.github.stivais.ui.events

import com.github.stivais.ui.UI
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.utils.loop


class EventManager(private val ui: UI) {
    var mouseX: Float = 0f
        set(value) {
            field = value * ui.main.scale // maybe make mouse scale depending on current hovered element?
        }

    var mouseY: Float = 0f
        set(value) {
            field = value * ui.main.scale // maybe make mouse scale depending on current hovered element?
        }

    var elementHovered: Element? = null
        set(value) {
            if (field === value) return
            if (field != null) dispatch(Mouse.Exited, field)
            if (value != null) dispatch(Mouse.Entered, value)
            field = value
        }

    var focused: Element? = null
        private set

    fun check(): Boolean {
        val hovered = elementHovered ?: return false
        return !hovered.isInside(mouseX, mouseY)
    }

    //
    // Mouse Input
    //

    fun onMouseMove(x: Float, y: Float) {
        mouseX = x
        mouseY = y
        elementHovered = getHovered(x, y, ui.main)
        dispatchToAll(Mouse.Moved, ui.main)
    }

    fun onMouseClick(button: Int): Boolean {
        if (focused != null) {
            if (focused!!.isInside(mouseX, mouseY)) {
                if (focused!!.accept(Focused.Clicked(button))) {
                    return true
                }
            } else {
                unfocus()
            }
        }
        return dispatch(Mouse.Clicked(button))
    }

    fun onMouseRelease(button: Int) {
        val event = Mouse.Released(button)
        dispatchToAll(event, ui.main)
    }

    fun onMouseScroll(amount: Float) {
        val event = Mouse.Scrolled(amount)
        dispatch(event)
    }

    //
    // Keyboard Input
    //

    fun onKeyType(char: Char): Boolean {
        val event = Key.Typed(char)
        if (focused != null) {
            return focused!!.accept(event)
        }
        return false
    }

    fun onKeycodePressed(code: Int): Boolean {
        val event = Key.CodePressed(code, true)
        if (focused != null) {
            return focused!!.accept(event)
        }
        return false
    }

    fun onKeyReleased(code: Int): Boolean {
        val event = Key.CodePressed(code, false)
        if (focused != null) {
            return focused!!.accept(event)
        }
        return false
    }

    //
    //
    //

    fun focus(element: Element) {
        focused?.accept(Focused.Lost)
        focused = element
        element.accept(Focused.Gained)
    }

    fun unfocus() {
        focused?.accept(Focused.Lost)
        focused = null
    }

    private fun getHovered(x: Float, y: Float, element: Element): Element? {
        var result: Element? = null
        if (element.renders && element.isInside(x, y)) {
            if (element.events != null) result = element // checks if even accepts any input/events
            element.elements?.loop { child ->
                getHovered(x, y, child)?.let {
                    result = it
                }
            }
        }
        return result
    }

    private fun dispatch(event: Event, element: Element? = elementHovered): Boolean {
        var current = element
        while (current != null) {
            if (current.accept(event)) {
                current.redraw = true
                return true
            }
            current = current.parent
        }
        return false
    }

    private fun dispatchToAll(event: Event, element: Element) {
        if (!element.renders) return // idk if this will cause issues, it shouldn't
        element.accept(event)
        element.elements?.loop {
            dispatchToAll(event, it)
        }
    }
}