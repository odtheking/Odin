package com.odtheking.odin.features.impl.render

import com.google.gson.JsonParser
import com.mojang.serialization.JsonOps
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentSerialization
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
        val plain = component.string
        if (!regex.containsMatchIn(plain)) return null
        return buildComponent(plain).copy().setStyle(component.style)
    }

    @JvmStatic
    fun replaceSequenceIfNeeded(text: FormattedCharSequence): FormattedCharSequence {
        if (!hasStartChar(text)) return text
        val plain = toPlainString(text)
        val regex = nameRegex ?: return text
        if (!regex.containsMatchIn(plain)) return text
        return buildComponent(plain).visualOrderText
    }

    private fun buildComponent(text: String): Component {
        val regex = nameRegex ?: return Component.literal(text)
        val out = Component.empty()
        var start = 0
        for (match in regex.findAll(text)) {
            if (match.range.first > start) out.append(Component.literal(text.substring(start, match.range.first)))
            val rep = cache[match.groupValues[1]]
            out.append(rep?.component?.copy() ?: Component.literal(match.value))
            start = match.range.last + 1
        }
        if (start < text.length) out.append(Component.literal(text.substring(start)))
        return out
    }

    private fun hasStartChar(text: FormattedCharSequence): Boolean {
        if (startChars.isEmpty()) return false
        var found = false
        text.accept { _, _, cp ->
            if (cp <= Char.MAX_VALUE.code && cp.toChar() in startChars) { found = true; false } else true
        }
        return found
    }

    private fun toPlainString(text: FormattedCharSequence): String {
        val sb = StringBuilder()
        text.accept { _, _, cp -> sb.appendCodePoint(cp); true }
        return sb.toString()
    }
}
