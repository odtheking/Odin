package me.odinmain.aurora.screens

import com.github.stivais.aurora.Aurora
import com.github.stivais.aurora.input.Keys
import com.github.stivais.aurora.input.Modifier
import me.odinmain.OdinMain
import net.minecraft.client.gui.GuiScreen
import net.minecraft.util.ChatAllowedCharacters
import org.lwjgl.input.Keyboard.*
import org.lwjgl.input.Mouse
import kotlin.math.sign

/**
 * [GuiScreen] implementation, which renders and handles input for an [aurora instance][Aurora.Instance].
 */
class AuroraMCScreen(val instance: Aurora.Instance) : GuiScreen() {

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        val mx = Mouse.getX().toFloat()
        val my = instance.window.height - Mouse.getY() - 1f
        instance.inputManager.onMouseMove(mx, my)
        instance.render()
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int) {
        instance.inputManager.onMouseClick(mouseButton)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        instance.inputManager.onMouseRelease(state)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (instance.inputManager.onKeycodePressed(keyCode)) {
            return
        }

        val char = when {
            instance.inputManager.modifiers.hasControl -> {
                if (instance.inputManager.modifiers.hasShift) shiftedCharsMap[keyCode] else nonShiftedCharMap[keyCode]
            }
            else -> if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) typedChar else null
        }
        if (char != null) {
            println(typedChar)
            if (instance.inputManager.onKeyTyped(char)) {
                return
            }
        } else {
            val key = keyMap[keyCode]
            if (key != null && instance.inputManager.onKeyTyped(key)) {
                return
            }
            val mods = modifierMap[keyCode]
            if (mods != null) {
                instance.inputManager.addModifier(mods.value)
                return
            }
        }
        super.keyTyped(typedChar, keyCode)
    }

    override fun handleMouseInput() {
        super.handleMouseInput()
        val scroll = Mouse.getEventDWheel()
        if (scroll != 0) instance.inputManager.onMouseScroll(scroll.sign * -1f)
    }

    override fun handleKeyboardInput() {
        super.handleKeyboardInput()
        handleModifierReleases()
    }

    private fun handleModifierReleases() {
        val mod = instance.inputManager.modifiers
        if (mod.hasLeftShift && !isKeyDown(KEY_LSHIFT)) instance.inputManager.removeModifier(Modifier.LSHIFT.value)
        if (mod.hasRightShift && !isKeyDown(KEY_RSHIFT)) instance.inputManager.removeModifier(Modifier.RSHIFT.value)
        if (mod.hasLeftControl && !isKeyDown(KEY_LCONTROL)) instance.inputManager.removeModifier(Modifier.LCTRL.value)
        if (mod.hasRightControl && !isKeyDown(KEY_RCONTROL)) instance.inputManager.removeModifier(Modifier.RCTRL.value)
        if (mod.hasLeftAlt && !isKeyDown(KEY_LMENU)) instance.inputManager.removeModifier(Modifier.LALT.value)
        if (mod.hasRightAlt && !isKeyDown(KEY_RMENU)) instance.inputManager.removeModifier(Modifier.RALT.value)
    }

    override fun doesGuiPauseGame() = false

    companion object {

        /**
         * Opens an [AuroraMCScreen] for the provided aurora instance.
         */
        fun open(instance: Aurora.Instance): AuroraMCScreen {
            val screen = AuroraMCScreen(instance)
            OdinMain.display = screen
            return screen
        }


        private val keyMap = hashMapOf(
            KEY_ESCAPE to Keys.ESCAPE,

            KEY_F1 to Keys.F1,
            KEY_F2 to Keys.F2,
            KEY_F3 to Keys.F3,
            KEY_F4 to Keys.F4,
            KEY_F5 to Keys.F5,
            KEY_F6 to Keys.F6,
            KEY_F7 to Keys.F7,
            KEY_F8 to Keys.F8,
            KEY_F9 to Keys.F9,
            KEY_F10 to Keys.F10,
            KEY_F11 to Keys.F11,
            KEY_F12 to Keys.F12,

            KEY_RETURN to Keys.ENTER,
            KEY_BACK to Keys.BACKSPACE,
            KEY_TAB to Keys.TAB,

            KEY_INSERT to Keys.INSERT,
            KEY_DELETE to Keys.DELETE,
            KEY_HOME to Keys.HOME,
            KEY_END to Keys.END,
            KEY_PRIOR to Keys.PAGE_UP,
            KEY_NEXT to Keys.PAGE_DOWN,

            KEY_UP to Keys.UP,
            KEY_DOWN to Keys.DOWN,
            KEY_LEFT to Keys.LEFT,
            KEY_RIGHT to Keys.RIGHT,
        )

        private val modifierMap = hashMapOf(
            KEY_LSHIFT to Modifier.LSHIFT,
            KEY_RSHIFT to Modifier.RSHIFT,
            KEY_LCONTROL to Modifier.LCTRL,
            KEY_RCONTROL to Modifier.RCTRL,
            KEY_LMENU to Modifier.LALT,
            KEY_RMENU to Modifier.RALT,
        )


        // fuck you lwjgl
        private val nonShiftedCharMap = hashMapOf(
            KEY_1 to '1',
            KEY_2 to '2',
            KEY_3 to '3',
            KEY_4 to '4',
            KEY_5 to '5',
            KEY_6 to '6',
            KEY_7 to '7',
            KEY_8 to '8',
            KEY_9 to '9',
            KEY_0 to '0',
            KEY_MINUS to '-',
            KEY_EQUALS to '=',
            KEY_COMMA to ',',
            KEY_PERIOD to '.',
            KEY_SLASH to '/',
            KEY_SEMICOLON to ';',
            KEY_APOSTROPHE to '\'',
            KEY_LBRACKET to '[',
            KEY_RBRACKET to ']',
            KEY_BACKSLASH to '\\',

            KEY_A to 'a',
            KEY_B to 'b',
            KEY_C to 'c',
            KEY_D to 'd',
            KEY_E to 'e',
            KEY_F to 'f',
            KEY_G to 'g',
            KEY_H to 'h',
            KEY_I to 'i',
            KEY_J to 'j',
            KEY_K to 'k',
            KEY_L to 'l',
            KEY_M to 'm',
            KEY_N to 'n',
            KEY_O to 'o',
            KEY_P to 'p',
            KEY_Q to 'q',
            KEY_R to 'r',
            KEY_S to 's',
            KEY_T to 't',
            KEY_U to 'u',
            KEY_V to 'v',
            KEY_W to 'w',
            KEY_X to 'x',
            KEY_Y to 'y',
            KEY_Z to 'z',
        )

        private val shiftedCharsMap = hashMapOf(
            KEY_1 to '!',
            KEY_2 to '@',
            KEY_3 to '#',
            KEY_4 to '$',
            KEY_5 to '%',
            KEY_6 to '^',
            KEY_7 to '&',
            KEY_8 to '*',
            KEY_9 to '(',
            KEY_0 to ')',
            KEY_MINUS to '_',
            KEY_EQUALS to '+',
            KEY_COMMA to '<',
            KEY_PERIOD to '>',
            KEY_SLASH to '?',
            KEY_SEMICOLON to ':',
            KEY_APOSTROPHE to '"',
            KEY_LBRACKET to '{',
            KEY_RBRACKET to '}',
            KEY_BACKSLASH to '|',

            KEY_A to 'A',
            KEY_B to 'B',
            KEY_C to 'C',
            KEY_D to 'D',
            KEY_E to 'E',
            KEY_F to 'F',
            KEY_G to 'G',
            KEY_H to 'H',
            KEY_I to 'I',
            KEY_J to 'J',
            KEY_K to 'K',
            KEY_L to 'L',
            KEY_M to 'M',
            KEY_N to 'N',
            KEY_O to 'O',
            KEY_P to 'P',
            KEY_Q to 'Q',
            KEY_R to 'R',
            KEY_S to 'S',
            KEY_T to 'T',
            KEY_U to 'U',
            KEY_V to 'V',
            KEY_W to 'W',
            KEY_X to 'X',
            KEY_Y to 'Y',
            KEY_Z to 'Z',
        )
    }
}