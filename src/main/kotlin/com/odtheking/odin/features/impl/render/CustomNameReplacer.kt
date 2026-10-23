package com.odtheking.odin.features.impl.render

import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import net.minecraft.locale.Language
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.network.chat.FormattedText
import net.minecraft.network.chat.Style
import net.minecraft.util.FormattedCharSequence
import net.minecraft.util.FormattedCharSink
import java.util.*

object CustomNameReplacer {
    private class Replacement(val plainText: String, val component: Component) {
        fun sequence(outer: Style) = FormattedCharSequence { sink ->
            component.visualOrderText.accept { index, style, codePoint -> sink.accept(index, style.applyTo(outer), codePoint) }
        }
    }

    private class Node {
        private var chars = CharArray(0)
        private var children = emptyArray<Node>()
        var replacement: Replacement? = null

        fun child(c: Char): Node? {
            for (i in chars.indices) if (chars[i] == c) return children[i]
            return null
        }

        fun getOrAdd(c: Char): Node = child(c) ?: Node().also {
            chars += c
            children += it
        }
    }

    @Volatile private var root: Node? = null

    @JvmStatic
    fun rebuild(players: Collection<PlayerSize.RandomPlayer>) {
        val newRoot = Node()
        var hasNames = false
        for ((customName, name) in players) {
            if (name.isEmpty() || !name.all { isWordChar(it.code) }) continue
            val rawJson = customName?.trim()?.takeIf { it.isNotEmpty() } ?: continue
            val element = runCatching { JsonParser.parseString(rawJson) }.getOrNull() ?: continue
            val parsed = ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, element).result().orElse(null) ?: continue
            name.fold(newRoot) { node, c -> node.getOrAdd(c) }.replacement = Replacement(parsed.string, parsed)
            hasNames = true
        }
        root = if (hasNames) newRoot else null
    }

    @JvmStatic
    fun clear() {
        root = null
    }

    @JvmStatic
    fun replaceStringIfNeeded(text: String): String {
        val root = root ?: return text
        var out: StringBuilder? = null
        var last = 0
        forEachName(text, root) { start, end, replacement ->
            val builder = out ?: StringBuilder(text.length + 16).also { out = it }
            builder.append(text, last, start).append(replacement.plainText)
            last = end
            true
        }
        return out?.append(text, last, text.length)?.toString() ?: text
    }

    @JvmStatic
    fun replaceSequenceIfNeeded(text: FormattedCharSequence): FormattedCharSequence {
        val root = root ?: return text
        val finder = NameFinder(root)
        text.accept(finder)
        if (!finder.endWord()) return text

        val parts = ArrayList<FormattedCharSequence>()
        text.forEachRun { style, run ->
            var last = 0
            forEachName(run, root) { start, end, replacement ->
                if (start > last) parts.add(FormattedCharSequence.forward(run.substring(last, start), style))
                parts.add(replacement.sequence(style))
                last = end
                true
            }
            if (last < run.length) parts.add(FormattedCharSequence.forward(run.substring(last), style))
        }
        return FormattedCharSequence.composite(parts)
    }

    @JvmStatic
    fun replaceTextIfNeeded(text: FormattedText): FormattedText {
        val root = root ?: return text
        val hasName = text.visit { if (containsName(it, root)) FormattedText.STOP_ITERATION else Optional.empty() }.isPresent
        if (!hasName) return text

        val parts = ArrayList<FormattedText>()
        replaceSequenceIfNeeded(Language.getInstance().getVisualOrder(text)).forEachRun { style, run -> parts.add(FormattedText.of(run, style)) }
        return FormattedText.composite(parts)
    }

    private class NameFinder(private val root: Node) : FormattedCharSink {
        private var node: Node? = null
        private var inWord = false
        private var style: Style? = null
        private var found = false

        override fun accept(index: Int, style: Style, codePoint: Int): Boolean {
            if (style != this.style) {
                this.style = style
                if (endWord()) return false
            }
            if (isWordChar(codePoint)) {
                node = (if (inWord) node else root)?.child(codePoint.toChar())
                inWord = true
            } else if (endWord()) return false
            return true
        }

        fun endWord(): Boolean {
            if (inWord && node?.replacement != null) found = true
            inWord = false
            return found
        }
    }

    private inline fun forEachName(text: String, root: Node, action: (start: Int, end: Int, replacement: Replacement) -> Boolean) {
        var i = 0
        while (i < text.length) {
            val c = text[i]
            when {
                c == '§' -> i += 2
                !isWordChar(c.code) -> i++
                else -> {
                    val start = i
                    var node: Node? = root
                    while (i < text.length && isWordChar(text[i].code)) {
                        node = node?.child(text[i])
                        i++
                    }
                    val replacement = node?.replacement
                    if (replacement != null && !action(start, i, replacement)) return
                }
            }
        }
    }

    private fun containsName(text: String, root: Node): Boolean {
        forEachName(text, root) { _, _, _ -> return true }
        return false
    }

    private fun FormattedCharSequence.forEachRun(action: (Style, String) -> Unit) {
        val builder = StringBuilder()
        var current: Style? = null
        accept { _, style, codePoint ->
            if (style != current) {
                current?.let { action(it, builder.toString()) }
                builder.setLength(0)
                current = style
            }
            builder.appendCodePoint(codePoint)
            true
        }
        current?.let { action(it, builder.toString()) }
    }

    private fun isWordChar(c: Int) = c in 'a'.code..'z'.code || c in 'A'.code..'Z'.code || c in '0'.code..'9'.code || c == '_'.code
}