package com.github.stivais.ui.elements.scope

import com.github.stivais.ui.UI
import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.constraints.measurements.Animatable
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.elements.impl.*
import com.github.stivais.ui.events.*
import com.github.stivais.ui.operation.UIOperation
import com.github.stivais.ui.renderer.*
import com.github.stivais.ui.utils.radii

open class ElementScope<E: Element>(val element: E) {

    val ui: UI
        get() = element.ui

    val parent: Element?
        get() = element.parent

    val x: Position
        get() = element.constraints.x

    val y: Position
        get() = element.constraints.y

    val width: Size
        get() = element.constraints.width

    val height: Size
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

    fun parent(): ElementScope<*>? = parent?.createScope()

    fun child(index: Int): ElementScope<*>? = element.elements?.get(index)?.createScope()

    fun scroll(amount: Float, duration: Float, animation: Animations) {
        val anim = element.scrollY ?: Animatable.Raw(0f).also { element.scrollY = it }
        val curr = anim.current
        anim.animate(
            to = (curr + amount).coerceIn(-(element.height), 0f),
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

//    /**
//     * Dangerous
//     */
//    fun <E : ElementScope<*>> cast(): E {
//        return this as E
//    }
//
//    fun <E : Element> castElement(): E {
//        return element as E
//    }

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
        gradient: Gradient,
        block: BlockScope.() -> Unit = {}
    ) = create(BlockScope(GradientBlock(constraints, colors.first, colors.second, radius, gradient)), block)

    @DSL
    fun text(
        text: String,
        font: Font = UI.defaultFont,
        pos: Constraints? = null,
        size: Size = 50.percent,
        color: Color = Color.WHITE,
        block: TextScope.() -> Unit = {}
    ) = create(TextScope(Text(text, font, color, pos, size)), block)

    @DSL
    fun text(
        text: () -> Any?,
        font: Font = UI.defaultFont,
        color: Color = Color.WHITE,
        pos: Constraints? = null,
        size: Size = 50.percent,
        block: TextScope.() -> Unit = {}
    ) = create(TextScope(Text.Supplied(text, font, color, pos, size)), block)

    @DSL
    fun textInput(
        text: String = "",
        placeholder: String = "",
        constraints: Constraints? = null,
        maxWidth: Size? = null,
        onTextChange: (string: String) -> Unit
    ) = create(TextScope(TextInput(text, placeholder, constraints, maxWidth, false, onTextChange = onTextChange)))

    @DSL
    fun image(
        image: Image,
        constraints: Constraints? = null,
        radius: FloatArray = 0.radii(),
        dsl: ElementScope<ImageElement>.() -> Unit = {}
    ) = create(ElementScope(ImageElement(image, constraints, radius)), dsl)

    @DSL
    fun image(
        image: String,
        constraints: Constraints? = null,
        radius: FloatArray = 0.radii(),
        dsl: ElementScope<ImageElement>.() -> Unit = {}
    ) = create(ElementScope(ImageElement(Image(image), constraints, radius)), dsl)

    fun onCreation(block: () -> Unit) {
        element.registerEvent(Lifetime.Initialized) {
            block()
            false
        }
    }

    fun afterCreation(block: () -> Unit) {
        element.registerEvent(Lifetime.AfterInitialized) {
            block()
            false
        }
    }

    fun onRemove(block: () -> Unit) {
        element.registerEvent(Lifetime.Uninitialized) {
            block()
            false
        }
    }

    fun onClick(button: Int = 0, block: (Mouse.Clicked) -> Boolean) {
        element.registerEvent(Mouse.Clicked(button), block)
    }

    fun onFocusedClick(button: Int = 0, block: (Focused.Clicked) -> Boolean) {
        element.registerEvent(Focused.Clicked(button), block)
    }

    fun onRelease(button: Int = 0, block: (Mouse.Released) -> Unit) {
        element.registerEvent(Mouse.Released(button)) {
            block(this)
            true
        }
    }

    fun onScroll(block: (Mouse.Scrolled) -> Boolean) {
        element.registerEvent(Mouse.Scrolled(0f), block)
    }

    fun onKeyPressed(block: (Key.CodePressed) -> Boolean) {
        element.registerEvent(Key.CodePressed(-1, true), block)
    }

    fun onKeyRelease(block: (Key.CodePressed) -> Boolean) {
        element.registerEvent(Key.CodePressed(-1, false), block)
    }

    fun onMouseEnter(block: (Event) -> Unit) {
        element.registerEvent(Mouse.Entered) {
            block(this)
            false
        }
    }

    fun onMouseExit(block: (Event) -> Unit) {
        element.registerEvent(Mouse.Exited) {
            block(this)
            false
        }
    }

    fun onMouseEnterExit(block: (Event) -> Boolean) {
        element.registerEvent(Mouse.Entered, block)
        element.registerEvent(Mouse.Exited, block)
    }

    fun onMouseMove(block: (Mouse.Moved) -> Boolean) {
        element.registerEvent(Mouse.Moved, block)
    }

    fun onMouseHovered(ms: Long, block: () -> Unit) {
//        if (element.hoverEvents == null) element.hoverEvents = arrayListOf()
//        element.hoverEvents!!.add(Mouse.Hovered(ms))
    }

    fun onFocusGain(block: (Event) -> Unit) {
        element.registerEvent(Focused.Gained) {
            block(this)
            false
        }
    }

    fun onFocusLost(block: (Event) -> Unit) {
        element.registerEvent(Focused.Lost) {
            block(this)
            false
        }
    }

    fun redraw() {
        element.redraw = true
    }

    fun scissors() {
        element.scissors = true
    }

    fun focusThis() {
        ui.eventManager.focus(element)
    }

    fun <E : Element, S : ElementScope<E>> create(scope: S, dsl: S.() -> Unit = {}) : S {
        this.element.addElement(scope.element)
        scope.dsl()
        return scope
    }

    fun Element.add() {
        this@ElementScope.element.addElement(this)
    }

    fun UIOperation.add() {
        element.addOperation(this)
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
