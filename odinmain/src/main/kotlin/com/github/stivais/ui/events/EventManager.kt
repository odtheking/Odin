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
        dispatchToAll(Mouse.Moved, ui.main)

        if (hoveredElements.size != 0) {
            while (hoveredElements.isNotEmpty() && !hoveredElements.last().isInside(x, y)) {
                hoveredElements.last().accept(Mouse.Exited)
//                modMessage("removed ${hoveredElements.size - 1}")
                hoveredElements.removeLast()
            }
        }
//            hoveredElements.clear()

        getHoveredElements(x, y, hoveredElements.lastOrNull() ?: ui.main)
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
            if (element.events != null && !hoveredElements.contains(element)) {
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

    private fun dispatch(event: Event): Boolean {
        hoveredElements.reverseLoop {
            if (it.accept(event)) {
                it.redraw = true
                return true
            }
        }
        return false
    }

    private fun dispatchToAll(event: Event, element: Element) {
        if (!element.renders) return
        element.accept(event)
        element.elements?.loop {
            dispatchToAll(event, it)
        }
    }
}