@file:Suppress("UNCHECKED_CAST")

package com.github.stivais.ui.elements.scope

import com.github.stivais.ui.UI
import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.constraints.measurements.Animatable
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.elements.impl.*
import com.github.stivais.ui.events.Event
import com.github.stivais.ui.events.Focused
import com.github.stivais.ui.events.Key
import com.github.stivais.ui.events.Mouse
import com.github.stivais.ui.renderer.Gradient

open class ElementScope<E: Element>(val element: E) {

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

    var enabled: Boolean
        get() = element.enabled
        set(value) {
            element.enabled = value
        }

    val ui: UI
        get() = element.ui

    val parent: Element?
        get() = element.parent

    fun parent(): ElementScope<*>? = parent?.createScope()

    fun child(index: Int): ElementScope<*>? = element.elements?.get(index)?.createScope()

    fun scroll(amount: Float, duration: Float, animation: Animations) {
        val anim = element.scrollY ?: Animatable.Raw(0f).also { element.scrollY = it }
        val curr = anim.current
        anim.animate(
            to = (curr + amount).coerceIn(-(element.height - curr), 0f),
            duration,
            animation
        )
    }

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

    fun <E : Element> castElement(): E {
        return element as E
    }

    @DSL
    fun group(
        constraints: Constraints? = null,
        block: ElementScope<Group>.() -> Unit = {}
    ) = create(ElementScope(Group(constraints)), block)

    @DSL
    fun column(
        constraints: Constraints? = null,
        padding: Size? = null,
        block: LayoutScope.() -> Unit = {}
    ) = create(LayoutScope(Layout.Column(constraints, padding)), block)

    @DSL
    fun row(
        constraints: Constraints? = null,
        padding: Size? = null,
        block: LayoutScope.() -> Unit = {}
    ) = create(LayoutScope(Layout.Row(constraints, padding)), block)

    @DSL
    fun block(
        constraints: Constraints? = null,
        color: Color,
        radius: FloatArray? = null,
        block: BlockScope.() -> Unit = {}
    ) = create(BlockScope(if (radius != null) RoundedBlock(constraints, color, radius) else Block(constraints, color)), block)

    @DSL
    fun block(
        constraints: Constraints? = null,
        colors: Pair<Color, Color>,
        radius: Float = 0f,
        direction: Gradient,
        block: BlockScope.() -> Unit = {}
    ) = create(BlockScope(GradientBlock(constraints, colors.first, colors.second, radius, direction)), block)

    @DSL
    fun text(
        text: String,
        pos: Constraints? = null,
        size: Measurement = 50.percent,
        color: Color = Color.WHITE,
        block: ElementScope<Text>.() -> Unit = {}
    ) = create(TextScope(Text(text, color, pos, size)), block)

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

    fun onScroll(block: (Mouse.Scrolled) -> Boolean) {
        element.registerEvent(Mouse.Scrolled(0f), block as Event.() -> Boolean)
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

    fun <E : Element, S : ElementScope<E>> create(scope: S, dsl: S.() -> Unit = {}) : S {
        this.element.addElement(scope.element)
        scope.dsl()
        return scope
    }

    fun Element.add() {
        this@ElementScope.element.addElement(this)
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

open class LayoutScope(layout: Layout) : ElementScope<Layout>(layout) {
    @DSL
    fun background(color: Color) {
        element.color = color
    }

    // temporary
    @DSL
    fun divider(amount: Size) {
        element.createDivider(amount)
    }
}

@DslMarker
private annotation class DSL

typealias ElementDSL = ElementScope<*>
