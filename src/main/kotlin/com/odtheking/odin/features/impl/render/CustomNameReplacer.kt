package com.odtheking.odin.features.impl.render

import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.contents.PlainTextContents
import net.minecraft.util.FormattedCharSequence

object CustomNameReplacer {
    private class Replacement(val plainText: String, val component: Component)

    private class State(val regex: Regex, val replacements: Map<String, Replacement>, val startChars: BooleanArray)

    @Volatile private var state: State? = null

    @JvmStatic
    fun isEnabled() = state != null

    @JvmStatic
    fun rebuild(players: Collection<PlayerSize.RandomPlayer>) {
        val entries = players.mapNotNull { player ->
            val rawJson = player.customName?.trim()?.takeIf { it.isNotEmpty() } ?: return@mapNotNull null
            val element = runCatching { JsonParser.parseString(rawJson) }.getOrNull() ?: return@mapNotNull null
            val parsed = ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, element).result().orElse(null) ?: return@mapNotNull null
            player.name to Replacement(parsed.string, parsed)
        }.sortedByDescending { it.first.length }

        if (entries.isEmpty()) { clear(); return }

        val startChars = BooleanArray(128)
        entries.forEach { (name, _) -> name.firstOrNull()?.takeIf { it.code < 128 }?.let { startChars[it.code] = true } }

        state = State(
            Regex("(?<![A-Za-z0-9_])(${entries.joinToString("|") { Regex.escape(it.first) }})(?![A-Za-z0-9_])"),
            entries.toMap(),
            startChars
        )
    }

    @JvmStatic
    fun clear() {
        state = null
    }

    @JvmStatic
    fun replaceStringIfNeeded(text: String): String {
        val s = state ?: return text
        if (!hasStartChar(text, s) || !s.regex.containsMatchIn(text)) return text
        return s.regex.replace(text) { match -> s.replacements[match.groupValues[1]]?.plainText ?: match.value }
    }

    @JvmStatic
    fun replaceComponentIfNeeded(component: Component): Component? {
        val s = state ?: return null
        val string = component.string
        if (!hasStartChar(string, s) || !s.regex.containsMatchIn(string)) return null
        return transform(component, s)
    }

    @JvmStatic
    fun replaceSequenceIfNeeded(text: FormattedCharSequence): FormattedCharSequence {
        val s = state ?: return text

        var found = false
        text.accept { _, _, cp -> if (cp < 128 && s.startChars[cp]) { found = true; false } else true }
        if (!found) return text

        val styles = ArrayList<Style>()
        val texts = ArrayList<String>()
        var curStyle: Style? = null
        val cur = StringBuilder()
        text.accept { _, style, cp ->
            if (curStyle != null && style != curStyle) { styles.add(curStyle!!); texts.add(cur.toString()); cur.setLength(0) }
            curStyle = style
            cur.appendCodePoint(cp)
            true
        }
        if (cur.isNotEmpty()) { styles.add(curStyle ?: Style.EMPTY); texts.add(cur.toString()) }
        if (texts.none { s.regex.containsMatchIn(it) }) return text

        val root = Component.empty()
        for (i in texts.indices) {
            val holder = Component.empty().setStyle(styles[i])
            appendReplaced(holder, texts[i], s)
            root.append(holder)
        }
        return root.visualOrderText
    }

    private fun transform(component: Component, s: State): MutableComponent {
        val text = (component.contents as? PlainTextContents)?.text()
        val out = if (text != null && s.regex.containsMatchIn(text)) Component.empty().also { appendReplaced(it, text, s) }
        else component.plainCopy()
        out.setStyle(component.style)
        component.siblings.forEach { out.append(transform(it, s)) }
        return out
    }

    private fun appendReplaced(out: MutableComponent, text: String, s: State) {
        var start = 0
        for (match in s.regex.findAll(text)) {
            if (match.range.first > start) out.append(Component.literal(text.substring(start, match.range.first)))
            out.append(s.replacements[match.groupValues[1]]?.component ?: Component.literal(match.value))
            start = match.range.last + 1
        }
        if (start < text.length) out.append(Component.literal(text.substring(start)))
    }

    private fun hasStartChar(text: String, s: State): Boolean {
        for (c in text) if (c.code < 128 && s.startChars[c.code]) return true
        return false
    }
}