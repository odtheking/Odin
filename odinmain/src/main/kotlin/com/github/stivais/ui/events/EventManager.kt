package com.github.stivais.ui.events

import com.github.stivais.ui.UI
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.utils.loop
import com.github.stivais.ui.utils.reverseLoop

class EventManager(private val ui: UI) {

    var mouseX: Float = 0f
        set(value) {
            field = value * ui.main.scale // maybe make mouse scale depending on current hovered element?
        }

    var mouseY: Float = 0f
        set(value) {
            field = value * ui.main.scale // maybe make mouse scale depending on current hovered element?
        }

    var hoveredElements: ArrayList<Element> = arrayListOf()

    var focused: Element? = null
        private set

    fun check(): Boolean {
        val hovered = hoveredElements.lastOrNull() ?: return false
        return !hovered.isInside(mouseX, mouseY)
    }

    //
    // Mouse Input
    //

    fun onMouseMove(x: Float, y: Float) {
        mouseX = x
        mouseY = y

        // check if an element might be over the current hovered tree
        ui.main.elements?.reverseLoop {
            if (it.isInside(x, y) && !hoveredElements.contains(it)) {
                hoveredElements.clear()
            }
        }

        var last = hoveredElements.lastOrNull()

        while (last != null && !last.isInside(x, y)) {
            last.accept(Mouse.Exited)
            hoveredElements.removeLast()
            last = hoveredElements.lastOrNull()
        }

        getHoveredElements(x, y, last ?: ui.main)
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

    private fun getHoveredElements(x: Float, y: Float, element: Element = ui.main): Boolean {
        var result = false
        if (element.renders && element.isInside(x, y)) {
            if (element.acceptsInput && !hoveredElements.contains(element)) {
                hoveredElements.add(element)
                element.accept(Mouse.Entered)
                result = true
            }
            element.elements?.let {
                for (i in it.size - 1 downTo 0) {
                    if (getHoveredElements(x, y, it[i])) {
                        break
                    }
                }
            }
        }
        return result
    }

    fun dispatch(event: Event): Boolean {
        hoveredElements.reverseLoop {
            if (it.accept(event)) {
                it.redraw = true
                return true
            }
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
}