package com.github.stivais.ui.elements.impl

import com.github.stivais.ui.color.alpha
import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.constraints.measurements.Percent
import com.github.stivais.ui.constraints.measurements.Pixel
import com.github.stivais.ui.constraints.measurements.Undefined
import com.github.stivais.ui.constraints.positions.Center
import com.github.stivais.ui.constraints.positions.Linked
import com.github.stivais.ui.constraints.sizes.Bounding
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.utils.replaceUndefined
import org.jetbrains.annotations.MustBeInvokedByOverriders

// todo: delete
open class Layout(
    constraints: Constraints?,
    protected val padding: Size?,
) : Element(constraints?.replaceUndefined(w = Bounding, h = Bounding)) {

    protected var lastLink: Linked? = null

    class Row(constraints: Constraints?, padding: Size?) : Layout(constraints, padding) {

        override fun onElementAdded(element: Element) {
            val c = element.constraints
            if (c.x is Undefined) {
                val link = Linked(element, lastLink)
                c.x = link
                lastLink = link

                if (padding != null && element !is Divider) {
                    val padding = if (padding !is Percent) padding else percentFix(padding)
                    createDivider(amount = padding)
                }
            }
            if (c.y is Undefined) {
                c.y = if (constraints.height !is Bounding) Center else Pixel(0f)
            }
            super.onElementAdded(element)
        }
    }

    class Column(constraints: Constraints?, padding: Size?) : Layout(constraints, padding) {

        override fun onElementAdded(element: Element) {
            val c = element.constraints
            if (c.x is Undefined) {
                c.x = if (constraints.width !is Bounding) Center else Pixel(0f)
            }
            if (c.y is Undefined) {
                val link = Linked(element, lastLink)
                c.y = link
                lastLink = link

                if (padding != null && element !is Divider && elements != null) {
                    val padding = if (padding !is Percent) padding else percentFix(padding)
                    createDivider(amount = padding)
                }
            }
            super.onElementAdded(element)
        }
    }

    override fun draw() {
        if (color != null && color!!.rgba.alpha != 0) {
            renderer.rect(x, y, width, height, color!!.get(this))
        }
    }

    @MustBeInvokedByOverriders
    override fun onElementAdded(element: Element) {
        val c = element.constraints
        if (constraints.width.reliesOnChild() && c.width is Percent) {
            c.width = percentFix(c.width as Percent)
        }
        if (constraints.height.reliesOnChild() && c.height is Percent) {
            c.height = percentFix(c.height as Percent)
        }
    }

    protected fun percentFix(size: Percent, target: Element = parent!!): Size {
        val c = target.constraints
        if (c.width.reliesOnChild() || c.height.reliesOnChild()) {
            return percentFix(size, target.parent!!)
        }
        val percent = size.percent
        return object : Size {
            override fun get(element: Element, type: Type): Float {
                return (if (type.axis == Constraint.HORIZONTAL) target.width else target.height) * percent
            }
        }
    }

    fun createDivider(amount: Size) = addElement(Divider(amount))

    internal inner class Divider(amount: Size) : Element(
        if (this is Row) size(w = amount) else size(h = amount)
    ) {
        override fun draw() { /**/ }
    }
}