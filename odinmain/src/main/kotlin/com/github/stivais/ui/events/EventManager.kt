package com.github.stivais.ui.events

import com.github.stivais.ui.UI
import com.github.stivais.ui.elements.Element


class EventManager(private val ui: UI) {

    var mouseX: Float = 0f

    var mouseY: Float = 0f

    var focused: Element? = null
        private set

    private var elementHovered: Element? = null
        private set(value) {
            if (field === value) return
            field?.isHovered = false
            value?.isHovered = true
            field = value
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

    fun onMouseClick(button: Int) {
        val event = Mouse.Clicked(button)
        if (focused != null) {
            if (!dispatchFocused(focused, event) && !focused!!.isInside(mouseX, mouseY)) {
                unfocus()
                updateIfNecessary()
            }
            return
        }
        if (dispatch(event)) {
            updateIfNecessary()
        }
    }

    fun onMouseRelease(button: Int) {
        val event = Mouse.Released(button)
        dispatchToAll(event, ui.main)
    }

    fun onMouseScroll(amount: Float) {
        val event = Mouse.Scrolled(amount)
        if (dispatch(event)) {
            updateIfNecessary()
        }
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
            if (element.elements != null) {
                for (child in element.elements!!) {
                    getHovered(x, y, child)?.let { result = it }
                }
            }
        }
        return result
    }

    private fun dispatch(event: Event, element: Element? = elementHovered): Boolean {
        var current = element
        while (current != null) {
            if (current.accept(event)) return true
            current = current.parent
        }
        return false
    }

    private fun dispatchToAll(event: Event, element: Element) {
        element.accept(event)
        if (element.elements != null) {
            for (child in element.elements!!) {
                dispatchToAll(event, child)
            }
        }
    }

    // todo: maybe find a better way or clean this up? im not quite happy with it
    private fun dispatchFocused(element: Element?, event: Event): Boolean { // could cause unwanted execution of certain inputs
        element?.let {
            for (entry in it.events?.entries ?: return true) {
                if (entry.key::class.java == event::class.java) { // check if java class is the same
                    if (!entry.key.isFocused()) continue
                    for (function in entry.value) {
                        if (function(event)) return true
                    }
                    return true
                }
            }
        }
        return false
    }

    private fun updateIfNecessary() {
        if (ui.settings.repositionOnEvent) {
            ui.needsRedraw = true
        }
    }
}