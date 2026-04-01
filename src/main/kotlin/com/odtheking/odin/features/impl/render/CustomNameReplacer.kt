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
    private data class CachedReplacement(val plainText: String, val component: Component)

    @Volatile private var nameRegex: Regex? = null
    @Volatile private var cache: Map<String, CachedReplacement> = emptyMap()
    @Volatile private var startChars: Set<Char> = emptySet()

    @JvmStatic
    fun isEnabled() = nameRegex != null

    @JvmStatic
    fun rebuild(players: Collection<PlayerSize.RandomPlayer>) {
        val entries = players.mapNotNull { player ->
            val rawJson = player.customName?.trim()?.takeIf { it.isNotEmpty() } ?: return@mapNotNull null
            val element = runCatching { JsonParser.parseString(rawJson) }.getOrNull() ?: return@mapNotNull null
            val parsed = ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, element).result().orElse(null) ?: return@mapNotNull null
            player.name to CachedReplacement(parsed.string, parsed)
        }.sortedByDescending { it.first.length }

        if (entries.isEmpty()) { clear(); return }

        nameRegex = Regex("(?<![A-Za-z0-9_])(${entries.joinToString("|") { Regex.escape(it.first) }})(?![A-Za-z0-9_])")
        cache = entries.toMap()
        startChars = entries.mapNotNull { it.first.firstOrNull() }.toSet()
    }

    @JvmStatic
    fun clear() {
        nameRegex = null
        cache = emptyMap()
        startChars = emptySet()
    }

    @JvmStatic
    fun replaceStringIfNeeded(text: String): String {
        val regex = nameRegex ?: return text
        if (text.isBlank() || !regex.containsMatchIn(text)) return text
        return regex.replace(text) { match ->
            cache[match.groupValues[1]]?.plainText ?: match.value
        }
    }

    @JvmStatic
    fun replaceComponentIfNeeded(component: Component): Component? {
        val regex = nameRegex ?: return null
        if (!regex.containsMatchIn(component.string)) return null
        return transformComponent(component)
    }

    @JvmStatic
    fun replaceSequenceIfNeeded(text: FormattedCharSequence): FormattedCharSequence {
        if (!hasStartChar(text)) return text
        val regex = nameRegex ?: return text
        val segments = mutableListOf<Pair<Style, String>>()
        var curStyle: Style? = null
        val curText = StringBuilder()
        text.accept { _, style, cp ->
            if (curStyle != null && style != curStyle) { segments.add(curStyle!! to curText.toString()); curText.clear() }
            curStyle = style; curText.appendCodePoint(cp); true
        }
        if (curText.isNotEmpty()) segments.add((curStyle ?: Style.EMPTY) to curText.toString())
        if (segments.none { regex.containsMatchIn(it.second) }) return text
        val root = Component.empty()
        segments.forEach { (style, seg) -> appendReplaced(root, seg, style, regex) }
        return root.visualOrderText
    }

    private fun transformComponent(component: Component): MutableComponent {
        val out = if (component.contents is PlainTextContents && nameRegex?.containsMatchIn(component.contents.let { (it as PlainTextContents).text() }) == true)
            Component.empty().also { appendReplaced(it, (component.contents as PlainTextContents).text(), component.style, nameRegex!!) }
        else component.plainCopy()
        component.siblings.forEach { out.append(transformComponent(it)) }
        return out
    }

    private fun appendReplaced(out: MutableComponent, text: String, style: Style, regex: Regex) {
        var start = 0
        for (match in regex.findAll(text)) {
            if (match.range.first > start) out.append(Component.literal(text.substring(start, match.range.first)).withStyle(style))
            out.append(cache[match.groupValues[1]]?.component?.copy() ?: Component.literal(match.value).withStyle(style))
            start = match.range.last + 1
        }
        if (start < text.length) out.append(Component.literal(text.substring(start)).withStyle(style))
    }

    private fun hasStartChar(text: FormattedCharSequence): Boolean {
        if (startChars.isEmpty()) return false
        var found = false
        text.accept { _, _, cp -> if (cp <= Char.MAX_VALUE.code && cp.toChar() in startChars) { found = true; false } else true }
        return found
    }
}
