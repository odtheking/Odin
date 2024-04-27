package com.github.stivais.ui.elements.impl

// Unfinished
//class TextInput(var text: String, constraints: Constraints?) : Element(constraints) {
//
//    private var caret: Int = text.length
//        set(value) {
//            field = value.coerceIn(0, text.length)
//        }
//
//    //var dragg
//
//    var selected = 0f
//
//    var cx: Float = 0f
//
//    var cy: Float = 0f
//
//    override fun draw() {
//        if (focused()) {
//            renderer.rect(cx, cy, 1f, 16f, Color.WHITE.rgb)
//        }
//        renderer.text(text, x, y, Color.WHITE.rgb, 16f)
//    }
//
//
//    init {
//        registerEvent(Key.Typed(' ')) {
//            insert((this as Key.Typed).char)
//            positionCaret()
//            true
//        }
//        onKeycodePressed {
//            when (code) {
//                GLFW.GLFW_KEY_RIGHT -> right(1)
//
//                GLFW.GLFW_KEY_LEFT -> left(1)
//
//                GLFW.GLFW_KEY_ENTER -> insert('\n')
//
//                GLFW.GLFW_KEY_BACKSPACE -> remove(1)
//
//
//                GLFW.GLFW_KEY_ESCAPE -> {
//                    ui.unfocus()
//                }
//            }
//            positionCaret()
//            true
//        }
//        focuses()
//    }
//
//    fun left(amount: Int) {
//        caret -= amount
//    }
//
//    fun right(amount: Int) {
//        caret += amount
//    }
//
//    private fun insert(string: Char) {
//        val before = caret
//        text = text.substring(0, caret) + string + text.substring(caret)
//        if (text.length != before) caret++
//    }
//
//    private fun insert(string: String) {
//        text = text.substring(0, caret) + string + text.substring(caret)
//    }
//
//    private fun remove(amount: Int) {
//        if (caret - amount < 0) return
//        text = text.substring(0, caret - amount) + text.substring(caret)
//        caret -= amount
//    }
//
//    fun positionCaret() {
//        val currLine = getCurrentLine()
//        cx = renderer.textWidth(currLine.first, fontSize = 16f) + x
//        cy = y + currLine.second * 16f
//    }
//
//    fun getCurrentLine(): Pair<String, Int> {
//        var i = 0
//        var ls = 0
//        var line = 0
//
//        for (chr in text) {
//            i++
//            if (chr == '\n') {
//                ls = i
//                line++
//            }
//            if (i == caret) {
//                return text.substring(ls, caret).substringBefore('\n') to line
//            }
//        }
//        return "" to 0
//    }
//}