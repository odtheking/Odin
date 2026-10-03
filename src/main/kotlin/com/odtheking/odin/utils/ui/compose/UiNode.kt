package com.odtheking.odin.utils.ui.compose

import com.mojang.blaze3d.platform.InputConstants
import com.odtheking.odin.utils.render.scissored
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.client.input.MouseButtonEvent

typealias Draw = UiNode.(graphics: GuiGraphicsExtractor) -> Unit

interface MeasurePolicy {
    fun measure(node: UiNode)
    fun place(node: UiNode)
}

class UiNode {
    var parent: UiNode? = null
        internal set
    val children = ArrayList<UiNode>(0)

    var x = 0
        internal set
    var y = 0
        internal set
    var width = 0
    var height = 0

    val right: Int get() = x + width
    val bottom: Int get() = y + height

    var policy: MeasurePolicy = BoxPolicy
    var content: Draw? = null

    var widthOf: (UiNode.() -> Int)? = null
    var heightOf: (UiNode.() -> Int)? = null
    var clipWhen: (UiNode.() -> Boolean)? = null
    var inertWhen: (() -> Boolean)? = null
    var offsetX: (() -> Int)? = null
    var offsetY: (() -> Int)? = null
    var scale: (() -> Float)? = null
    val layers = ArrayList<Draw>(0)
    val hoverSources = ArrayList<InteractionSource>(0)
    var onPress: (() -> Unit)? = null
    var pointer: PointerInput? = null
    var onScroll: (UiNode.(amount: Double) -> Boolean)? = null
    var onKey: (UiNode.(event: KeyEvent) -> Boolean)? = null
    var onChar: (UiNode.(event: CharacterEvent) -> Boolean)? = null
    var onFocusChanged: ((focused: Boolean) -> Unit)? = null

    var tag: Any? = null
    fun reset() {
        widthOf = null
        heightOf = null
        clipWhen = null
        inertWhen = null
        offsetX = null
        offsetY = null
        scale = null
        layers.clear()
        hoverSources.clear()
        onPress = null
        pointer = null
        onScroll = null
        onKey = null
        onChar = null
        onFocusChanged = null
        tag = null
    }

    fun measure() {
        policy.measure(this)
        widthOf?.let { width = it(this) }
        heightOf?.let { height = it(this) }
    }

    fun place(x: Int, y: Int) {
        this.x = x + (offsetX?.invoke() ?: 0)
        this.y = y + (offsetY?.invoke() ?: 0)
        policy.place(this)
    }

    fun render(graphics: GuiGraphicsExtractor, parentPointerInside: Boolean) {
        if (width <= 0 || height <= 0) return
        val pointerInside = parentPointerInside && inertWhen?.invoke() != true
        val over = pointerInside && Pointer.isOver(x, y, width, height)
        for (i in hoverSources.indices) hoverSources[i].hovered = over

        val factor = scale?.invoke() ?: 1f
        if (factor != 1f) {
            val pivotX = x + width / 2f
            val pivotY = y + height / 2f
            graphics.pose().pushMatrix()
            graphics.pose().translate(pivotX, pivotY)
            graphics.pose().scale(factor, factor)
            graphics.pose().translate(-pivotX, -pivotY)
        }

        if (clips) graphics.scissored(x, y, right, bottom) { draw(graphics, over) }
        else draw(graphics, pointerInside)

        if (factor != 1f) graphics.pose().popMatrix()
    }

    private val clips: Boolean get() = clipWhen?.invoke(this) == true

    private fun draw(graphics: GuiGraphicsExtractor, pointerInside: Boolean) {
        for (i in layers.indices) layers[i](graphics)
        content?.invoke(this, graphics)
        val clipped = clips
        forEachChild { child ->
            if (!clipped || (child.y < bottom && child.bottom > y)) child.render(graphics, pointerInside)
        }
    }

    fun pick(pointX: Double, pointY: Double): UiNode? {
        if (width <= 0 || height <= 0) return null
        val inside = pointX >= x && pointX < right && pointY >= y && pointY < bottom
        if (clips && !inside) return null
        for (i in children.indices.reversed()) children[i].pick(pointX, pointY)?.let { return it }
        return if (inside) this else null
    }

    internal inline fun forEachChild(action: (UiNode) -> Unit) {
        for (i in children.indices) action(children[i])
    }
}

class PointerInput(
    val onPress: UiNode.(event: MouseButtonEvent) -> Boolean,
    val onDrag: (UiNode.(event: MouseButtonEvent) -> Unit)?,
    val onRelease: (UiNode.(event: MouseButtonEvent) -> Unit)?,
)

object Pointer {
    var x = 0
        internal set
    var y = 0
        internal set

    var doubleClick = false
        internal set

    fun isOver(x: Int, y: Int, width: Int, height: Int): Boolean =
        this.x >= x && this.x < x + width && this.y >= y && this.y < y + height
}

fun UiNode.size(w: Int, h: Int) = width(w).height(h)
fun UiNode.size(size: Int) = size(size, size)
fun UiNode.width(w: Int) = apply { widthOf = { w } }
fun UiNode.height(h: Int) = apply { heightOf = { h } }

fun UiNode.width(compute: UiNode.() -> Int) = apply { widthOf = compute }
fun UiNode.height(compute: UiNode.() -> Int) = apply { heightOf = compute }
fun UiNode.clip(enabled: UiNode.() -> Boolean = { true }) = apply { clipWhen = enabled }
fun UiNode.inert(inert: () -> Boolean) = apply { inertWhen = inert }
fun UiNode.offset(x: () -> Int = { 0 }, y: () -> Int = { 0 }) = apply { offsetX = x; offsetY = y }
fun UiNode.at(x: Int, y: Int) = offset({ x }, { y })
fun UiNode.scale(factor: () -> Float) = apply { scale = factor }
fun UiNode.drawBehind(draw: Draw) = apply { layers += draw }
fun UiNode.hoverable(source: InteractionSource) = apply { hoverSources += source }
fun UiNode.tag(value: Any) = apply { tag = value }
fun UiNode.onPress(action: () -> Unit) = apply { onPress = action }

fun UiNode.pointerInput(
    onDrag: (UiNode.(event: MouseButtonEvent) -> Unit)? = null,
    onRelease: (UiNode.(event: MouseButtonEvent) -> Unit)? = null,
    onPress: UiNode.(event: MouseButtonEvent) -> Boolean,
) = apply { pointer = PointerInput(onPress, onDrag, onRelease) }

fun UiNode.clickable(onRightClick: (() -> Unit)? = null, onClick: (() -> Unit)? = null): UiNode {
    if (onClick == null && onRightClick == null) return this
    return pointerInput { event ->
        when (event.button()) {
            InputConstants.MOUSE_BUTTON_LEFT -> onClick?.invoke() ?: return@pointerInput false
            InputConstants.MOUSE_BUTTON_RIGHT -> onRightClick?.invoke() ?: return@pointerInput false
            else -> return@pointerInput false
        }
        true
    }
}

fun UiNode.onScroll(handler: UiNode.(amount: Double) -> Boolean) = apply { onScroll = handler }
fun UiNode.onKey(handler: UiNode.(event: KeyEvent) -> Boolean) = apply { onKey = handler }
fun UiNode.onChar(handler: UiNode.(event: CharacterEvent) -> Boolean) = apply { onChar = handler }
fun UiNode.onFocusChanged(handler: (focused: Boolean) -> Unit) = apply { onFocusChanged = handler }
fun UiNode.focusRequester(requester: FocusRequester) = apply { requester.node = this }