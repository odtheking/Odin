package com.github.stivais.ui.elements

import com.github.stivais.ui.UI
import com.github.stivais.ui.UI.Companion.logger
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.constraints.Type
import com.github.stivais.ui.constraints.measurements.Undefined
import com.github.stivais.ui.constraints.positions.Center
import com.github.stivais.ui.elements.scope.ElementScope
import com.github.stivais.ui.events.Event
import com.github.stivais.ui.events.Lifetime
import com.github.stivais.ui.events.Mouse
import com.github.stivais.ui.operation.UIOperation
import com.github.stivais.ui.renderer.Renderer
import com.github.stivais.ui.transforms.Transforms
import com.github.stivais.ui.utils.loop

abstract class Element(constraints: Constraints?, var color: Color? = null) {

    lateinit var ui: UI

    // rework for constraints to be more flexible?
    val constraints: Constraints = constraints ?: Constraints(Undefined, Undefined, Undefined, Undefined)

    val renderer: Renderer
        get() = ui.renderer

    // element "hierarchy"

    var parent: Element? = null

    var elements: ArrayList<Element>? = null

    //

    // events

    var acceptsInput = false

    var events: HashMap<Event, ArrayList<(Event) -> Boolean>>? = null

    //

    // Position

    var x: Float = 0f
    var y: Float = 0f

    var width: Float = 0f
        set(value) {
            field = value.coerceAtLeast(0f)
        }

    var height: Float = 0f
        set(value) {
            field = value.coerceAtLeast(0f)
        }

    private var transforms: ArrayList<Transforms>? = null

    // this is needed to track current scale
    var scale = 1f

    //

    open var enabled: Boolean = true

    var scissors: Boolean = false

    var renders: Boolean = true
        get() {
            return enabled && field
        }
        set(value) {
            if (!value) hovered = false
            field = value
        }

    abstract fun draw()

    fun size() {
        if (!enabled) return
        preSize()
        if (!constraints.width.reliesOnChild()) width = constraints.width.get(this, Type.W)
        if (!constraints.height.reliesOnChild()) height = constraints.height.get(this, Type.H)
        elements?.loop { it.size() }
    }

    fun position(newX: Float, newY: Float) {
        x = constraints.x.get(this, Type.X) + newX
        y = constraints.y.get(this, Type.Y) + newY
    }

    open fun positionChildren() {
        if (!enabled) return
        elements?.loop {
            it.position(x, y)
            it.positionChildren()
        }
        // this causes it to redraw every frame, it will be fixed when official repo comes out with code reorganization
        val widthRelies = constraints.width.reliesOnChild()
        val heightRelies = constraints.height.reliesOnChild()
        if (widthRelies) width = constraints.width.get(this, Type.W)
        if (heightRelies) height = constraints.height.get(this, Type.H)
        if (widthRelies || heightRelies) parent?.redrawInternal = true
    }

    var redrawInternal = true

    var redraw: Boolean
        get() = redrawInternal
        set(value) {
            if (value) {
                val element = getElementToRedraw()
                element.redrawInternal = true
            }
        }

    var hovered = false
        set(value) {
            if (value == field) return
            if (value) accept(Mouse.Entered) else accept(Mouse.Exited)
            field = value
        }

    // rename
    private fun getElementToRedraw(): Element {
        val p = parent ?: return this
        return if (p.constraints.width.reliesOnChild() || p.constraints.height.reliesOnChild()) p.getElementToRedraw() else p
    }

    fun clip() {
        elements?.loop {
            it.renders = it.intersects(x, y, width, height) && !(it.width == 0f && it.height == 0f)
            if (it.renders) {
                it.clip()
            }
        }
    }

    open fun preSize() {}

    // IDK is it worth
    fun preRender() {
        if (redrawInternal) {
            redrawInternal = false
            size()
            positionChildren()
            clip()
        }
        preDraw()
        elements?.loop {
            it.preRender()
        }
    }

    open fun preDraw() {

    }

    fun render() {
        if (!renders) return
        renderer.push()
        transforms?.loop {
            it.apply(this, renderer)
        }
        draw()
        if (scissors) renderer.pushScissor(x, y, width, height)
        elements?.loop { element ->
            element.render()
        }
        if (scissors) renderer.popScissor()
        renderer.pop()
    }

    open fun accept(event: Event): Boolean {
        if (events != null) {
            events!![event]?.let { actions -> actions.loop { if (it(event)) return true } }
            if (event is Lifetime) events!!.remove(event)
        }
        return false
    }

    @Suppress("UNCHECKED_CAST")
    fun <E : Event> registerEvent(event: E, block: E.() -> Boolean) {
        if (event !is Lifetime) acceptsInput = true
        if (events == null) events = HashMap()
        events!!.getOrPut(event) { arrayListOf() }.add(block as (Event) -> Boolean)
    }

    infix fun <E : Event> E.register(block: (E) -> Boolean) = registerEvent(this, block)

    fun addOperation(operation: UIOperation) {
        if (ui.operations == null) ui.operations = arrayListOf()
        ui.operations!!.add(operation)
    }

    fun addTransform(transform: Transforms) {
        if (transforms == null) transforms = arrayListOf()
        transforms!!.add(transform)
    }

    fun addElement(element: Element) {
        onElementAdded(element)
        if (elements == null) elements = arrayListOf()
        elements!!.add(element)
        element.parent = this
        if (::ui.isInitialized) {
            element.initialize(ui)
        }
    }

    fun removeElement(element: Element?) {
        if (element == null) return logger.warning("Tried removing element, but it doesn't exist")
        if (elements.isNullOrEmpty()) return logger.warning("Tried calling \"removeElement\" while there is no elements")
        ui.eventManager.remove(element)
        element.accept(Lifetime.Uninitialized)
        elements!!.remove(element)
        element.parent = null
    }

    fun removeAll() {
        if (elements == null) return
        elements?.removeIf { element ->
            ui.eventManager.remove(element)
            element.accept(Lifetime.Uninitialized)
            element.parent = null
            true
        }
        elements = null
        if (::ui.isInitialized) redraw = true
    }

    fun initialize(ui: UI) {
        this.ui = ui
        elements?.loop { it.initialize(ui) }
        accept(Lifetime.Initialized)
    }

    open fun createScope(): ElementScope<*> {
        return ElementScope(this)
    }

    // sets up position if element being added has an undefined position
    open fun onElementAdded(element: Element) {
        val c = element.constraints
        if (c.x is Undefined) c.x = Center
        if (c.y is Undefined) c.y = Center
    }

    fun isInside(x: Float, y: Float): Boolean {
        val tx = this.x
        val ty = this.y
        return x in tx..tx + (width) * scale && y in ty..ty + (height) * scale
    }

    fun intersects(other: Element): Boolean {
        return intersects(other.x, other.y, other.width, other.height)
    }

    private fun intersects(x: Float, y: Float, width: Float, height: Float): Boolean {
        val tx = this.x
        val ty = this.y
        val tw = this.width
        val th = this.height
        return (x < tx + tw && tx < x + width) && (y < ty + th && ty < y + height)
    }

    fun screenWidth(): Float {
        return width * scale
    }

    fun screenHeight(): Float {
        return height * scale
    }
}