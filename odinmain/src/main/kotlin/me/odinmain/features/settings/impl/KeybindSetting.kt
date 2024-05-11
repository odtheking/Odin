package me.odinmain.features.settings.impl

import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.constraints.measurements.Animatable
import com.github.stivais.ui.constraints.sizes.Bounding
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.elements.button
import com.github.stivais.ui.elements.text
import com.github.stivais.ui.events.onClick
import com.github.stivais.ui.events.onFocusGain
import com.github.stivais.ui.events.onFocusLost
import com.github.stivais.ui.events.onKeycodePressed
import com.github.stivais.ui.impl.mainColor
import com.github.stivais.ui.utils.animate
import com.github.stivais.ui.utils.focuses
import com.github.stivais.ui.utils.radii
import com.github.stivais.ui.utils.seconds
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.odinmain.features.settings.Saving
import me.odinmain.features.settings.Setting
import org.lwjgl.input.Keyboard.*
import org.lwjgl.input.Mouse

class KeybindSetting(
    name: String,
    override val default: Keybinding,
    description: String,
) : Setting<Keybinding>(name, false, description), Saving {

    constructor(name: String, key: Int, description: String) : this(name, Keybinding(key), description)

    override var value: Keybinding = default

    /**
     * Action to do, when keybinding is pressed
     *
     * Note: Action is always invoked, even if module isn't enabled.
     */
    fun onPress(block: () -> Unit): KeybindSetting {
        value.onPress = block
        return this
    }

    override fun reset() {
        value.key = default.key
    }

    private val keyName: String
        get() {
            val key = value.key
            return when {
                key > 0 -> getKeyName(key) ?: "Error"
                key < 0 -> {
                    when (val button = key + 100) {
                        0 -> "Left Button"
                        1 -> "Right Button"
                        2 -> "Middle Button"
                        else -> "Button $button"
                    }
                }
                else -> "None"
            }
        }

    override fun getElement(parent: Element): SettingElement = parent.setting(40.px) {
        text(
            text = name,
            pos = at(x = 6.px),
            size = 40.percent
        )
        button(
            constraints = constrain(x = -6.px, w = Bounding + 6.px, h = 70.percent),
            offColor = Color.RGB(38, 38, 38),
            onColor = Color.RGB(38, 38, 38),
            radii = radii(all = 5)
        ) {
            val display = text(
                text = keyName
            )
            onClick(null) { (button) ->
                value.key = -100 + button!!
                ui.unfocus()
                true
            }
            onKeycodePressed { (code) ->
                value.key = when (code) {
                    KEY_ESCAPE, KEY_BACK -> 0
                    KEY_NUMPADENTER, KEY_RETURN -> value.key
                    else -> code
                }
                ui.unfocus()
                true
            }
            onFocusGain {
                outlineColor!!.animate(0.25.seconds)
                outline!!.animate(0.25.seconds)
            }
            onFocusLost {
                display.text = keyName
                outlineColor!!.animate(0.25.seconds)
                outline!!.animate(0.25.seconds)
            }
            focuses()
            outline(color = mainColor, Animatable(from = 1.px, to = 2.5.px))
        }
    }

    override fun write(): JsonElement {
        return JsonPrimitive(value.key)
    }

    override fun read(element: JsonElement?) {
        element?.asInt?.let {
            value.key = it
        }
    }

}

class Keybinding(var key: Int) {

    /**
     * Intended to active when keybind is pressed.
     */
    var onPress: (() -> Unit)? = null

    /**
     * @return `true` if [key] is held down.
     */
    fun isDown(): Boolean {
        return if (key == 0) false else (if (key < 0) Mouse.isButtonDown(key + 100) else isKeyDown(key))
    }
}