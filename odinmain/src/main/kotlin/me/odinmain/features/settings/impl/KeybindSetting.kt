package me.odinmain.features.settings.impl

import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.constraints.measurements.Animatable
import com.github.stivais.ui.constraints.sizes.Bounding
import com.github.stivais.ui.elements.scope.*
import com.github.stivais.ui.utils.*
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.odinmain.features.impl.render.ClickGUI
import me.odinmain.features.impl.render.ClickGUI.`gray 38`
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

//    private fun isConflicting(): Boolean {
//        return mc.gameSettings.keyBindings.any { it.keyCode == value.key } && value.key != 0 && ClickGUI.showBindConfliction
//    }

    override fun ElementScope<*>.createElement() {
        setting(40.px) {
            text(
                text = name,
                pos = at(x = 6.px),
                size = 40.percent,
            )
            block(
                constraints = constrain(x = -6.px, w = Bounding + 6.px, h = 70.percent),
                color = `gray 38`,
                radius = radius(5)
            ) {
                val display = text(
                    text = keyName,
                    color = /*if (isConflicting()) conflictingColor else */Color.WHITE
                )
                onFocusedClick { (button) ->
                    value.key = -100 + button
                    ui.unfocus()
                    true
                }
                onKeyPressed { (code) ->
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
                    display.string = keyName
//                    val target = if (isConflicting()) conflictingColor else Color.WHITE
//                    display.animateColor(to = target, duration = 0.1.seconds)
                    outlineColor!!.animate(0.25.seconds)
                    outline!!.animate(0.25.seconds)
                }
                hoverEffect()
                focuses()
                outline(color = ClickGUI.color, Animatable(from = 1.px, to = 2.5.px))
            }
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

//    private companion object {
//        @JvmField
//        val conflictingColor: Color = Color.RGB(240, 70, 70)
//    }
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