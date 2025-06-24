package me.odinmain.features.settings.impl

import com.github.stivais.aurora.animations.Animation
import com.github.stivais.aurora.color.Color
import com.github.stivais.aurora.components.impl.Layout
import com.github.stivais.aurora.components.impl.dropShadow
import com.github.stivais.aurora.components.scope.ContainerScope
import com.github.stivais.aurora.dsl.*
import com.github.stivais.aurora.measurements.Measurement
import com.github.stivais.aurora.renderer.data.Radius.Companion.radius
import com.github.stivais.aurora.utils.Timing.Companion.seconds
import com.github.stivais.aurora.utils.withAlpha
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.odinmain.aurora.components.SwappingText
import me.odinmain.features.impl.render.ClickGUI
import me.odinmain.features.settings.RepresentableSetting
import me.odinmain.features.settings.Saving
import net.minecraftforge.fml.client.registry.ClientRegistry
import org.lwjgl.input.Keyboard.*
import org.lwjgl.input.Mouse
import net.minecraft.client.settings.KeyBinding as MCKeybinding

class KeybindSetting(
    name: String,
    override val default: Keybinding,
    description: String,
) : RepresentableSetting<Keybinding>(name, description), Saving {

    constructor(name: String, key: Int, description: String) : this(name, Keybinding(key), description)

    override var value: Keybinding = default

    override fun ContainerScope<*>.represent() = row(
        size = size(width = 100.percent),
        gap = Layout.Gap.Auto,
        alignment = Layout.Alignment.Center
    ) {
        text(
            string = name,
            size = 18.px
        )

        val strokeWidth = animatable<Measurement.Size>(from = 2.px, to = 3.px)

        block(
            size = sum(),
            color = ClickGUI.gray38,
            strokeColor = ClickGUI.colorDarker,
            strokeWidth = strokeWidth,
            radius = 8.radius()
        ) {
            dropShadow(color = Color.BLACK.withAlpha(0.4f), blur = 5f, spread = 2f, offsetY = 2f)
            padding(horizontal = 8f, vertical = 5f)

            val text = SwappingText(component, string = getKeyName(), size = 18.px)
            text.scope {
                onEvent(event = ValueChanged) {
                    text.animate(getKeyName(), .3.seconds)
                }
            }

            onClick {
                aurora.focus(component)
                false
            }

            onAnyClick { (button) ->
                if (aurora.isFocused(component)) {
                    value.key = -100 + button
                    aurora.focus(null)
                    true
                } else false
            }

            onKeycodeTyped { (code) ->
                value.key = when (code) {
                    KEY_ESCAPE, KEY_BACK -> 0
                    KEY_NUMPADENTER, KEY_RETURN -> value.key
                    else -> code
                }
                // this should have focus,
                // since it is a focused event.
                aurora.focus(null)
                true
            }

            onFocus { strokeWidth.animate(.5.seconds, Animation.Style.EaseInOutQuad) }
            onFocusLost { strokeWidth.animate(.5.seconds, Animation.Style.EaseInOutQuad) }
        }
    }

    override fun reset() {
        value.key = default.key
    }

    override fun write(): JsonElement {
        return JsonPrimitive(value.key)
    }

    override fun read(element: JsonElement?) {
        element?.asInt?.let {
            value.key = it
        }
    }

    /**
     * Action to do, when keybinding is pressed
     *
     * Note: Action is always invoked, even if module isn't enabled.
     */
    fun onPress(block: () -> Unit): KeybindSetting {
        value.onPress = block
        return this
    }

    fun attach(name: String = this.name): KeybindSetting {
        val bind = object : MCKeybinding(name, value.key, "Odin") {
            override fun getKeyCode(): Int {
                return value.key
            }
            override fun setKeyCode(keyCode: Int) {
                value.key = keyCode
            }
        }
        ClientRegistry.registerKeyBinding(bind)
        return this
    }

    private fun getKeyName(): String {
        val key = value.key
        return when {
            key > 0 -> getKeyName(key) ?: "Error"
            key < 0 -> {
                when (val button = key + 100) {
                    0 -> "Left Button"
                    1 -> "Right Button"
                    2 -> "Middle Button"
                    else -> "MB $button"
                }
            }

            else -> "None"
        }
    }
}

data class Keybinding(var key: Int) {

    /**
     * Intended to active when keybinding is pressed.
     */
    var onPress: (() -> Unit)? = null

    /**
     * @return `true` if [key] is held down.
     */
    fun isDown(): Boolean {
        return if (key == 0) false else (if (key < 0) Mouse.isButtonDown(key + 100) else isKeyDown(key))
    }
}