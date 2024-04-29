package com.github.stivais.ui.elements

import com.github.stivais.ui.UI
import com.github.stivais.ui.UI.Companion.logger
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.Constraint
import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.constraints.Type
import com.github.stivais.ui.constraints.measurements.Undefined
import com.github.stivais.ui.constraints.positions.Center
import com.github.stivais.ui.events.Event
import com.github.stivais.ui.events.Mouse
import com.github.stivais.ui.utils.forLoop


// Positioning (X or Y) should be: defined with a measurement or aligned by left/top, center, right/bottom (with some padding)
// or undefined where it gets set by the parent's element (by default best option is to center)
//
// Sizing (Width or Height) should be: defined with a measurement, copy the parents size, or wrap around elements children,
// if left undefined, it is up to the element to set what it's default value should be
//
// Required:
//  Measurements (All):
//        Pixels: Pixels on the screen
//        Animatable: Based between 2 constraints values
//        Percents: Based on parent elements position (Maybe?)
//
//  Positions (X or Y):
//        Linked: Gets position based on where the linked element ends, (Used in column)
//        Aligned: Left/Middle/Right Aligning with padding (padding doesn't apply to middle)
//
//  Sizing (Width or Height):
//        Bounding: Wraps around all children element, so they all fit
//        Copying: Copies the parent's sizing
//
// Goal: to avoid assigning positions and sizes as much as possible/mainly avoid magic numbers

abstract class Element(constraints: Constraints?) {

    // todo: maybe bring all values into here?
    val constraints: Constraints = constraints ?: Constraints(Undefined, Undefined, Undefined, Undefined)

    lateinit var ui: UI

    val renderer get() = ui.renderer

    var parent: Element? = null

    var elements: ArrayList<Element>? = null

    open var events: HashMap<Event, ArrayList<Event.() -> Boolean>>? = null

    private var initializationTasks: ArrayList<() -> Unit>? = null

    var x: Float = 0f
    var y: Float = 0f
    var width: Float = 0f
    var height: Float = 0f

    var internalX: Float = 0f
        set(value) {
            field = value
            x = value + (parent?.x ?: 0f)
        }

    var internalY: Float = 0f
        set(value) {
            field = value
            y = value + (parent?.y ?: 0f)
        }

    var color: Color? = null

    var isHovered = false
        set(value) {
            if (value) {
                accept(Mouse.Entered)
            } else {
                accept(Mouse.Exited)
            }
            field = value
        }

    var enabled: Boolean = true

    var renders: Boolean = true
        get() = enabled && field

    abstract fun draw()

    // position needs a rework, make it only reposition if it needs,
    // make it parent place child so it can be customized allowing for wrapping columns/rows
    open fun position() {
        if (!enabled) return
        if (!constraints.width.reliesOnChild()) width = constraints.width.get(this, Type.W)
        if (!constraints.height.reliesOnChild()) height = constraints.height.get(this, Type.H)
        internalX = constraints.x.get(this, Type.X)
        internalY = constraints.y.get(this, Type.Y)

        elements?.forLoop { element ->
            element.position()
            element.renders = element.intersects(this.x, this.y, width, height)
        }
        if (constraints.width.reliesOnChild()) width = constraints.width.get(this, Type.W)
        if (constraints.height.reliesOnChild()) height = constraints.height.get(this, Type.H)
    }

    fun render() {
        if (!renders) return
        draw()
        elements?.forLoop { element ->
            element.render()
        }
    }

    open fun accept(event: Event): Boolean {
        if (events != null) {
            events?.get(event)?.let { actions -> actions.forLoop { if (it(event)) return true } }
        }
        return false
    }

    fun registerEvent(event: Event, block: Event.() -> Boolean) {
        if (events == null) events = HashMap()
        events!!.getOrPut(event) { arrayListOf() }.add(block)

    }

    fun onInitialization(action: () -> Unit) {
        if (::ui.isInitialized) return logger.warning("Tried calling \"onInitialization\" after init has already been done")
        if (initializationTasks == null) initializationTasks = arrayListOf()
        initializationTasks!!.add(action)
    }

    fun addElement(element: Element) {
        if (elements == null) elements = arrayListOf()
        elements!!.add(element)
        element.parent = this
        element.initialize(ui)
        onElementAdded(element)
        if (ui.settings.positionOnAdd) element.position()
    }

    fun initialize(ui: UI) {
        this.ui = ui
        if (initializationTasks != null) {
            initializationTasks!!.forLoop { it() }
            initializationTasks!!.clear()
            initializationTasks = null
        }
    }

    // sets up position if element being added has an undefined position
    open fun onElementAdded(element: Element) {
        element.apply {
            if (constraints.x is Undefined) constraints.x = Center
            if (constraints.y is Undefined) constraints.y = Center
        }
    }

    fun isInside(x: Float, y: Float): Boolean {
        val tx = this.x
        val ty = this.y
        return x in tx..tx + width && y in ty..ty + height
    }

    fun intersects(x: Float, y: Float, width: Float, height: Float): Boolean {
        val tx = this.x
        val ty = this.y
        val tw = this.width
        val th = this.height
        return (x < tx + tw && tx < x + width) && (y < ty + th && ty < y + height)
    }

    fun focused(): Boolean = ui.eventManager?.focused == this

    // todo: dsl, maybe move out of this class?
    fun toggle(value: Boolean = !enabled) {
        enabled = value
    }

    // todo: dsl, maybe move out of this class?
    fun width(): Constraint {
        return constraints.width
    }

    // todo: dsl, maybe move out of this class?
    fun height(): Constraint {
        return constraints.height
    }

    // todo: dsl, maybe move out of this class?
    fun sibling(distance: Int = 1): Element? {
        if (parent != null) {
            val currIndex = parent!!.elements!!.indexOf(this)
            return parent!!.elements!!.getOrNull(currIndex + distance)
        }
        return null
    }

    // todo: dsl, maybe move out of this class?
    fun sendEventTo(event: Event, target: Element): Boolean {
        return target.accept(event)
    }

    // todo: dsl, maybe move out of this class?
    fun sendEventTo(target: Element): Event.() -> Boolean {
        return { target.accept(this) }
    }

    fun afterInitialization(block: () -> Unit) {
        if (ui.afterInit == null) ui.afterInit = arrayListOf()
        ui.afterInit!!.add(block)
    }

    fun takeEvents(from: Element) {
        if (from.events == null) return logger.warning("Tried to take event from an element that doesn't have events")
        if (events != null) {
            events!!.putAll(from.events!!)
        } else {
            events = from.events
        }
        from.events = null
    }

    operator fun invoke(action: Element.() -> Unit) = action()
}
