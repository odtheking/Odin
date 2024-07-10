package com.github.stivais.ui.elements

import com.github.stivais.ui.UI
import com.github.stivais.ui.UI.Companion.logger
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.constraints.Type
import com.github.stivais.ui.constraints.measurements.Animatable
import com.github.stivais.ui.constraints.measurements.Undefined
import com.github.stivais.ui.constraints.positions.Center
import com.github.stivais.ui.elements.scope.ElementScope
import com.github.stivais.ui.events.Event
import com.github.stivais.ui.events.Lifetime
import com.github.stivais.ui.operation.UIOperation
import com.github.stivais.ui.renderer.Renderer
import com.github.stivais.ui.utils.loop

abstract class Element(constraints: Constraints?, var color: Color? = null) {

    val constraints: Constraints = constraints ?: Constraints(Undefined, Undefined, Undefined, Undefined)

    lateinit var ui: UI

    val renderer: Renderer
        get() = ui.renderer

    var parent: Element? = null

    var elements: ArrayList<Element>? = null

    var acceptsInput = false

    var events: HashMap<Event, ArrayList<(Event) -> Boolean>>? = null

//    var scaledCentered = true

    val initialized
        get() = ::ui.isInitialized

    var x: Float = 0f
    var y: Float = 0f
    var width: Float = 0f
    var height: Float = 0f

    var internalX: Float = 0f

    var internalY: Float = 0f

    var scrollY: Animatable.Raw? = null

    var sy = 0f
        set(value) {
            if (field == value) return
            redraw = true
            field = value
        }

    var alphaAnim: Animatable? = null

    var rotateAnim: Animatable? = null

    var alpha = 1f
        set(value) {
            field = value.coerceIn(0f, 1f)
        }

    var scale = 1f
        set(value) {
            field = value.coerceAtLeast(0f)
        }

    var rotation = 0f

    open var enabled: Boolean = true

    var scissors: Boolean = true

    var renders: Boolean = true
        get() {
            return enabled && field
        }

    abstract fun draw()

    fun size() {
        if (!enabled) return
        preSize()
        if (!constraints.width.reliesOnChild()) width = constraints.width.get(this, Type.W)
        if (!constraints.height.reliesOnChild()) height = constraints.height.get(this, Type.H)
        elements?.loop { it.size() }
    }

    fun position() {
        if (!enabled) return

        internalX = constraints.x.get(this, Type.X)
        internalY = constraints.y.get(this, Type.Y)
        x = internalX + (parent?.x ?: 0f) //+ (parent?.x ?: 0f)
        y = internalY + (parent?.y ?: 0f) + (parent?.sy ?: 0f)

        elements?.loop { it.position() }

        // resize after position because of Constraints like Bounding and Linked
        if (constraints.width.reliesOnChild()) width = constraints.width.get(this, Type.W)
        if (constraints.height.reliesOnChild()) height = constraints.height.get(this, Type.H)
    }

    var redraw = true

    // rename
    fun getElementToRedraw(): Element {
        val p = parent ?: return this
        return if (p.constraints.width.reliesOnChild() || p.constraints.height.reliesOnChild()) p.getElementToRedraw() else this
    }

    fun clip() {
        elements?.loop {
            it.renders = it.intersects(x, y, width, height)// && it.width != 0f && it.height != 0f
            if (it.renders) {
                it.clip()
            }
        }
    }

    open fun preSize() {}

    fun render() {
        if (redraw) {
            redraw = false
            val test = getElementToRedraw()
            test.size()
            test.position()
            test.clip()
        }
        if (!renders) return
        renderer.push()
//        if (scrollY != null) {
//            renderer.hollowRect(x, y, width, height, 3f, Color.WHITE.rgba)
//            sy = scrollY!!.get(this, Type.H)
//        }
        if (alphaAnim != null) {
            alpha = alphaAnim!!.get(this, Type.X)
        }
        if (rotateAnim != null) {
            rotation = rotateAnim!!.get(this, Type.X)
        }
        if (alpha != 1f) {
            renderer.globalAlpha(alpha)
        }
        if (scale != 1f) {
//            var x = x
//            var y = y
//            if (scaledCentered) {
//                x += width / 2f
//                y += height / 2f
//            }
            renderer.translate(x + width / 2f, y + height / 2f)
            renderer.scale(scale, scale)
            renderer.translate(-(x + width / 2f), -(y + height / 2f))
        }
        if (rotation != 0f) {
            renderer.translate(x + width / 2f, y + height / 2f)
            renderer.rotate(rotation)
            renderer.translate(-(x + width / 2f), -(y + height / 2f))
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

    fun addElement(element: Element) {
        onElementAdded(element)
        if (elements == null) elements = arrayListOf()
        elements!!.add(element)
        element.parent = this
        if (::ui.isInitialized) element.initialize(ui)
    }

    fun removeElement(element: Element?) {
        if (element == null) return logger.warning("Tried removing element, but it doesn't exist")
        if (elements.isNullOrEmpty()) return logger.warning("Tried calling \"removeElement\" while there is no elements")
        element.accept(Lifetime.Uninitialized)
        elements!!.remove(element)
        element.parent = null
    }

    fun removeAll() {
        elements?.loop { it.parent = null }
        elements?.clear()
        elements = null
        if (::ui.isInitialized) {
            redraw = true
        }
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
        return x in tx..tx + (width) * scale && y in ty..ty + (height - sy) * scale
    }

    private fun intersects(x: Float, y: Float, width: Float, height: Float): Boolean {
        val tx = this.x
        val ty = this.y
        val tw = this.width
        val th = this.height
        return (x < tx + tw && tx < x + width) && (y < ty + th && ty < y + height)
    }
}