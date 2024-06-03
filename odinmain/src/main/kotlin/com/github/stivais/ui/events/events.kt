package com.github.stivais.ui.events

interface Event {
    // todo: maybe better way?
    fun isFocused() = false
}

interface Mouse : Event {

    data class Clicked(val button: Int?) : Mouse {

        override fun isFocused(): Boolean = button == null

        override fun equals(other: Any?): Boolean { // needs to be overridden, so it is recognized in the events Map
            if (this === other) return true
            if (other !is Clicked) return false
            return button == other.button
        }

        override fun hashCode(): Int = 31 * ((button ?: -1) + 500)

        override fun toString(): String = "MouseClicked(button=$button)"
    }

    data class Released(val button: Int) : Mouse {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Released) return false
            return button == other.button
        }

        override fun hashCode(): Int = 31 * (button + 250)

        override fun toString(): String = "MouseReleased(button=$button)"
    }

    data class Scrolled(val amount: Float) : Mouse {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            return other is Scrolled
        }

        override fun hashCode(): Int = 28629151 // 31^5
    }

    data object Entered : Mouse

    data object Exited : Mouse

    data object Moved: Mouse
}

// todo: implement cleaner way?
// todo: implement key mods (i.e indicator for if ctrl and or shift is down)
interface Key : Event {

    data class Typed(val char: Char) : Key {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            return other is Typed
            //if (other !is Typed) return false
            //return char == other.char
        }

        override fun hashCode(): Int {
            return 31
        }
    }


    data class CodePressed(val code: Int, private val down: Boolean) : Key {

        override fun isFocused(): Boolean = true

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is CodePressed) return false
            return down == other.down

        }

        override fun hashCode(): Int {
            return 29791 + down.hashCode()// 31^3
        }
    }

    class CodeReleased(val code: Int?) : Key {

        override fun isFocused(): Boolean = true

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is CodeReleased) return false
            return other.code == null || code == other.code
        }

        override fun hashCode(): Int {
            return 923521 // 31^4
        }
    }
}

interface Focused : Event {

    data object Gained : Focused {
        override fun isFocused(): Boolean = true
    }

    data object Lost : Focused {
        override fun isFocused(): Boolean = true
    }

}