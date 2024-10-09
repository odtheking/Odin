package com.github.stivais.ui.events

import com.github.stivais.ui.UI
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.utils.loop
import com.github.stivais.ui.utils.reverseLoop

class EventManager(private val ui: UI) {

    var recalculate = false

    var mouseX: Float = 0f
        set(value) {
            field = value * ui.main.scale // maybe make mouse scale depending on current hovered element?
        }

    var mouseY: Float = 0f
        set(value) {
            field = value * ui.main.scale // maybe make mouse scale depending on current hovered element?
        }

    /**
     * Used to dispatch bubbling events
     */
    private var hoveredElement: Element? = null
        set(value) {
            if (value == field) return
            field = value
        }

    var focused: Element? = null
        private set

    fun remove(element: Element?) {
        element?.let {
            if (it == focused) unfocus()
            if (it == hoveredElement) hoveredElement = null
        }
    }

    fun check(): Boolean {
        return recalculate || hoveredElement?.isInside(mouseX, mouseY) == false
    }

    //
    // Mouse Input
    //

    fun onMouseMove(x: Float, y: Float) {
        mouseX = x
        mouseY = y
        hoveredElement = getHoveredElement(x, y, ui.main)
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

    private fun getHoveredElement(x: Float, y: Float, element: Element = ui.main): Element? {
        var result: Element? = null
        if (element.renders && element.isInside(x, y)) {
            element.elements?.reverseLoop { it ->
                if (result == null) {
                    getHoveredElement(x, y, it)?.let {
                        result = it
                        return@reverseLoop // prevent discarding hovered
                    }
                }
                discard(it)
            }
            if (element.acceptsInput) {
                element.hovered = true
                if (result == null) result = element
            }
        }
        return result
    }

    // TODO: Rename
    private fun discard(element: Element) {
        // checks if it isn't hovered but acceptsInput to skip checking its children
        if (!element.hovered && element.acceptsInput) return
        element.hovered = false
        element.elements?.loop { discard(it) }
    }

    fun dispatch(event: Event, element: Element? = hoveredElement): Boolean {
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

    fun dispatchToAll(event: Event, element: Element) {
        if (!element.renders) return
        element.accept(event)
        element.elements?.loop {
            dispatchToAll(event, it)
        }
    }

    // rename i think
    fun dispatchToAllReverse(event: Event, element: Element) {
        if (!element.renders) return
        element.elements?.loop {
            dispatchToAll(event, it)
        }
        element.accept(event)
    }
}