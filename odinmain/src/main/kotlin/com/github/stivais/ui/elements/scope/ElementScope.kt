@file:Suppress("UNCHECKED_CAST")

package com.github.stivais.ui.elements.scope

import com.github.stivais.ui.UI
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.constraints.Measurement
import com.github.stivais.ui.constraints.percent
import com.github.stivais.ui.constraints.px
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.elements.impl.Block
import com.github.stivais.ui.elements.impl.Column
import com.github.stivais.ui.elements.impl.RoundedBlock
import com.github.stivais.ui.elements.impl.Text
import com.github.stivais.ui.events.Event
import com.github.stivais.ui.events.Focused
import com.github.stivais.ui.events.Key
import com.github.stivais.ui.events.Mouse

open class ElementScope<E: Element>(internal val element: E) {

    val x
        get() = element.constraints.x

    val y
        get() = element.constraints.y

    val width
        get() = element.constraints.width

    val height
        get() = element.constraints.height

    var color: Color?
        get() = element.color
        set(value) {
            element.color = value
        }

    val ui: UI
        get() = element.ui

    val parent: Element?
        get() = element.parent

    fun sibling(distance: Int = 1): ElementScope<*>? {
        if (element.parent != null) {
            val currIndex = element.parent!!.elements!!.indexOf(element)
            val sibling = element.parent!!.elements!!.getOrNull(currIndex + distance) ?: return null
            return sibling.createScope()
        }
        return null
    }

    /**
     * Dangerous
     */
    fun <E : ElementScope<*>> cast(): E {
        return this as E
    }

    @DSL
    fun column(
        constraints: Constraints? = null,
        block: ColumnScope.() -> Unit = {}
    ) {
        val element = Column(constraints)
        create(ColumnScope(element), block)
    }

    @DSL
    fun block(
        constraints: Constraints? = null,
        color: Color,
        radius: FloatArray? = null,
        block: BlockScope.() -> Unit = {}
    ) {
        val element = if (radius != null) RoundedBlock(constraints, color, radius) else Block(constraints, color)
        create(BlockScope(element), block)
    }

    @DSL
    fun text(
        text: String,
        pos: Constraints? = null,
        size: Measurement = 50.percent,
        color: Color = Color.WHITE,
        block: ElementScope<Text>.() -> Unit = {}
    ): Text = create(ElementScope(Text(text, color, pos, size)), block) as Text

    fun onInitialization(action: () -> Unit) {
        if (element.initialized) return UI.logger.warning("Tried calling \"onInitialization\" after init has already been done")
        if (element.initializationTasks == null) element.initializationTasks = arrayListOf()
        element.initializationTasks!!.add(action)
    }

    fun onClick(button: Int? = 0, block: (Mouse.Clicked) -> Boolean) {
        element.registerEvent(Mouse.Clicked(button), block as Event.() -> Boolean)
    }

    fun onRelease(button: Int = 0, block: (Mouse.Released) -> Unit) {
        element.registerEvent(Mouse.Released(button)) {
            block(this as Mouse.Released)
            true
        }
    }

    fun onKeyPressed(block: (Key.CodePressed) -> Boolean) {
        element.registerEvent(Key.CodePressed(-1, true), block as Event.() -> Boolean)
    }

    fun onKeyRelease(block: (Key.CodePressed) -> Boolean) {
        element.registerEvent(Key.CodePressed(-1, false), block as Event.() -> Boolean)
    }

    fun onMouseEnterExit(block: (Event) -> Boolean) {
        element.registerEvent(Mouse.Entered, block)
        element.registerEvent(Mouse.Exited, block)
    }

    fun onMouseMove(block: (Mouse.Moved) -> Boolean) {
        element.registerEvent(Mouse.Moved, block as Event.() -> Boolean)
    }

    fun onFocusGain(block: (Event) -> Unit) {
        element.registerEvent(Focused.Gained) {
            block(this)
            true
        }
    }

    fun onFocusLost(block: (Event) -> Unit) {
        element.registerEvent(Focused.Lost) {
            block(this)
            true
        }
    }

    fun redraw() {
        element.ui.needsRedraw = true
    }

    fun scissors() {
        element.scissors = true
    }

    fun focusThis() {
        ui.eventManager?.focus(element)
    }

    fun <E : Element, S : ElementScope<E>> create(scope: S, dsl: S.() -> Unit) : Element {
        this.element.addElement(scope.element)
        scope.dsl()
        return scope.element
    }
}

open class BlockScope(block: Block) : ElementScope<Block>(block) {

    val outlineColor: Color?
        get() = element.outlineColor

    val outline: Measurement?
        get() = element.outline

    @DSL
    fun outline(color: Color, thickness: Measurement = 1.px) {
        element.outlineColor = color
        element.outline = thickness
    }
}

open class ColumnScope(column: Column) : ElementScope<Column>(column) {
    @DSL
    fun background(color: Color) {
        element.color = color
    }
}

@DslMarker
private annotation class DSL

typealias ElementDSL = ElementScope<*>
