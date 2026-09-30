package com.odtheking.odin.utils.ui.compose

import androidx.compose.runtime.*
import com.mojang.blaze3d.platform.InputConstants
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.utils.Colors
import net.minecraft.client.gui.components.EditBox
import net.minecraft.network.chat.Component

@Composable
internal fun Layout(policy: MeasurePolicy, content: Draw? = null, children: @Composable () -> Unit = {}): UiNode {
    val node = remember { UiNode() }
    node.reset()
    node.policy = policy
    node.content = content
    ComposeNode<UiNode, Applier<UiNode>>(factory = { node }, update = {}, content = children)
    return node
}

@Composable
fun Box(content: @Composable () -> Unit = {}): UiNode = Layout(BoxPolicy, children = content)

@Composable
fun Column(spacing: Int = 0, content: @Composable () -> Unit): UiNode =
    Layout(remember(spacing) { ColumnPolicy(spacing) }, children = content)

@Composable
fun Row(spacing: Int = 0, content: @Composable () -> Unit): UiNode =
    Layout(remember(spacing) { RowPolicy(spacing) }, children = content)

@Composable
fun Canvas(draw: Draw): UiNode = Layout(BoxPolicy, draw)

@Composable
fun Canvas(size: () -> Pair<Int, Int>, draw: Draw): UiNode =
    Canvas(draw).width { size().first }.height { size().second }

val LocalTextShadow = staticCompositionLocalOf { false }

@Composable
fun Text(text: () -> String, color: () -> Int = { Colors.WHITE.rgba }, shadow: Boolean = LocalTextShadow.current): UiNode =
    Canvas(size = { mc.font.width(text()) to TEXT_HEIGHT }) { graphics -> graphics.text(mc.font, text(), x, y, color(), shadow) }

@Composable
fun TextInput(
    value: String,
    onValueChange: (String) -> Unit,
    maxLength: Int = 32,
    centered: Boolean = false,
    filter: (String) -> String = { it },
    focusRequester: FocusRequester? = null,
    onFocusChanged: (Boolean) -> Unit = {},
): UiNode {
    val onChange = rememberUpdatedState(onValueChange)
    val onFocus = rememberUpdatedState(onFocusChanged)
    val inserted = rememberUpdatedState(filter)
    val box = remember {
        object : EditBox(mc.font, 0, 0, 200, TEXT_HEIGHT, Component.empty()) {
            override fun insertText(text: String) = super.insertText(inserted.value(text))
        }.apply {
            isBordered = false
            setTextShadow(false)
            setTextColor(Colors.WHITE.rgba)
            this.value = value
            setResponder { onChange.value(it) }
        }
    }
    box.setMaxLength(maxLength)
    box.setCentered(centered)

    SideEffect {
        if (!box.isFocused && box.value != value) box.value = value
        focusRequester?.onRequest = {
            box.moveCursorToStart(false)
            box.moveCursorToEnd(true)
        }
    }

    val node = Canvas { graphics ->
        box.x = x + TEXT_INSET
        box.y = y + (height - TEXT_HEIGHT) / 2
        box.width = width - TEXT_INSET * 2
        box.extractRenderState(graphics, Pointer.x, Pointer.y, 0f)
    }
        .height(TEXT_HEIGHT)
        .pointerInput(onDrag = { event -> box.mouseDragged(event, 0.0, 0.0) }) { event ->
            if (event.button() != InputConstants.MOUSE_BUTTON_LEFT) return@pointerInput false
            box.onClick(event, Pointer.doubleClick)
            true
        }
        .onKey { box.keyPressed(it) }
        .onChar { box.charTyped(it) }
        .onFocusChanged {
            box.isFocused = it
            onFocus.value(it)
        }
    if (focusRequester != null) node.focusRequester(focusRequester)
    return node
}

internal object BoxPolicy : MeasurePolicy {
    override fun measure(node: UiNode) {
        var width = 0
        var height = 0
        node.forEachChild { child ->
            child.measure()
            width = maxOf(width, child.width)
            height = maxOf(height, child.height)
        }
        node.width = width
        node.height = height
    }

    override fun place(node: UiNode) {
        node.forEachChild { child -> child.place(node.x, node.y) }
    }
}

internal class ColumnPolicy(private val spacing: Int = 0) : MeasurePolicy {
    override fun measure(node: UiNode) {
        var width = 0
        var height = 0
        node.forEachChild { child ->
            child.measure()
            if (child.height > 0) {
                if (height > 0) height += spacing
                width = maxOf(width, child.width)
                height += child.height
            }
        }
        node.width = width
        node.height = height
    }

    override fun place(node: UiNode) {
        var y = node.y
        node.forEachChild { child ->
            child.place(node.x, y)
            if (child.height > 0) y += child.height + spacing
        }
    }
}

internal class RowPolicy(private val spacing: Int = 0) : MeasurePolicy {
    override fun measure(node: UiNode) {
        var width = 0
        var height = 0
        node.forEachChild { child ->
            child.measure()
            if (child.width > 0) {
                if (width > 0) width += spacing
                width += child.width
                height = maxOf(height, child.height)
            }
        }
        node.width = width
        node.height = height
    }

    override fun place(node: UiNode) {
        var x = node.x
        node.forEachChild { child ->
            child.place(x, node.y)
            if (child.width > 0) x += child.width + spacing
        }
    }
}

private const val TEXT_HEIGHT = 8
private const val TEXT_INSET = 4