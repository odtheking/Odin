package com.odtheking.odin.clickgui.settings.impl

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.odtheking.odin.clickgui.ClickGUI.gray38
import com.odtheking.odin.clickgui.Panel
import com.odtheking.odin.clickgui.settings.RenderableSetting
import com.odtheking.odin.clickgui.settings.Saving
import com.odtheking.odin.features.impl.render.ClickGUIModule
import com.odtheking.odin.utils.Colors
import com.odtheking.odin.utils.ui.animations.EaseInOutAnimation
import com.odtheking.odin.utils.ui.isAreaHovered
import com.odtheking.odin.utils.ui.rendering.NVGRenderer
import net.minecraft.client.input.MouseButtonEvent

class ItemEnchantSetting(
    name: String,
    private val items: Map<String, Pair<String, String>>,
    private val pools: Map<String, Map<String, Pair<String, Int>>>,
    desc: String
) : RenderableSetting<MutableList<Triple<String, String, Int>>>(name, desc), Saving {

    override val default: MutableList<Triple<String, String, Int>> = mutableListOf()
    override var value: MutableList<Triple<String, String, Int>> = mutableListOf()

    private val rowHeight = 32f
    private val headerHeight = Panel.HEIGHT
    private val settingAnim = EaseInOutAnimation(200)
    private var extended = false
    private var addPhase = 0
    private var pendingItem: String? = null
    private var animFrom = headerHeight
    private var animTo = headerHeight

    private fun enchantsFor(itemId: String?): Map<String, Pair<String, Int>> =
        pools[items[itemId]?.second] ?: emptyMap()

    private fun phaseList(): List<String> = when (addPhase) {
        1 -> items.keys.toList()
        2 -> enchantsFor(pendingItem).keys.filter { ench -> value.none { it.first == pendingItem && it.second == ench } }
        else -> emptyList()
    }

    private fun bodyRows(): Int = value.size + 1 + phaseList().size

    private fun targetHeight(): Float = if (extended) 44f + bodyRows() * rowHeight else headerHeight

    private fun animateFrom(previous: Float) {
        animFrom = previous
        animTo = targetHeight()
        settingAnim.start()
    }

    override fun getHeight(): Float = settingAnim.get(animFrom, animTo, false)

    override val isHovered: Boolean get() = isAreaHovered(lastX, lastY, width, headerHeight, true)

    private fun rowHovered(rowIndex: Int): Boolean =
        isAreaHovered(lastX, lastY + 38f + rowHeight * rowIndex, width, rowHeight, true)

    private fun fitSize(text: String, maxWidth: Float): Float {
        val full = NVGRenderer.textWidth(text, 16f, NVGRenderer.defaultFont)
        return if (full <= maxWidth) 16f else (16f * maxWidth / full).coerceAtLeast(7f)
    }

    override fun render(x: Float, y: Float, mouseX: Float, mouseY: Float): Float {
        super.render(x, y, mouseX, mouseY)

        NVGRenderer.text(name, x + 6f, y + headerHeight / 2f - 8f, 16f, Colors.WHITE.rgba, NVGRenderer.defaultFont)
        val summary = if (value.isEmpty()) "None" else "${value.size} set"
        val summaryWidth = NVGRenderer.textWidth(summary, 16f, NVGRenderer.defaultFont)
        NVGRenderer.text(summary, x + width - 14f - summaryWidth, y + headerHeight / 2f - 8f, 16f, Colors.WHITE.rgba, NVGRenderer.defaultFont)

        if (!extended && !settingAnim.isAnimating()) return headerHeight

        val displayHeight = getHeight()
        if (settingAnim.isAnimating()) NVGRenderer.pushScissor(x, y, width, displayHeight)

        NVGRenderer.rect(x + 6f, y + 37f, width - 12f, bodyRows() * rowHeight, gray38.rgba, 5f)

        var row = 0
        for ((item, ench, level) in value) {
            val rowY = y + 38f + rowHeight * row
            val label = "${items[item]?.first ?: item} · ${enchantsFor(item)[ench]?.first ?: ench}"
            val labelSize = fitSize(label, width - 112f)
            NVGRenderer.text(label, x + 14f, rowY + (rowHeight - labelSize) / 2f, labelSize, Colors.WHITE.rgba, NVGRenderer.defaultFont)
            NVGRenderer.text("-", x + width - 92f, rowY + 8f, 16f, Colors.WHITE.rgba, NVGRenderer.defaultFont)
            NVGRenderer.text(level.toString(), x + width - 66f, rowY + 8f, 16f, Colors.WHITE.rgba, NVGRenderer.defaultFont)
            NVGRenderer.text("+", x + width - 50f, rowY + 8f, 16f, Colors.WHITE.rgba, NVGRenderer.defaultFont)
            NVGRenderer.text("x", x + width - 20f, rowY + 8f, 16f, Colors.MINECRAFT_RED.rgba, NVGRenderer.defaultFont)
            if (rowHovered(row)) NVGRenderer.hollowRect(x + 6f, rowY, width - 12f, rowHeight, 1.5f, ClickGUIModule.clickGUIColor.rgba, 4f)
            row++
        }

        val addRowY = y + 38f + rowHeight * row
        val addLabel = when (addPhase) {
            1 -> "Pick item..."
            2 -> "Pick enchant for ${items[pendingItem]?.first ?: pendingItem}..."
            else -> "+ Add requirement"
        }
        NVGRenderer.text(addLabel, x + 14f, addRowY + 8f, 16f, ClickGUIModule.clickGUIColor.rgba, NVGRenderer.defaultFont)
        if (rowHovered(row)) NVGRenderer.hollowRect(x + 6f, addRowY, width - 12f, rowHeight, 1.5f, ClickGUIModule.clickGUIColor.rgba, 4f)
        row++

        val list = phaseList()
        for (key in list) {
            val rowY = y + 38f + rowHeight * row
            val text = if (addPhase == 1) items[key]?.first ?: key else enchantsFor(pendingItem)[key]?.first ?: key
            NVGRenderer.text(text, x + 24f, rowY + 8f, 16f, Colors.WHITE.rgba, NVGRenderer.defaultFont)
            if (rowHovered(row)) NVGRenderer.hollowRect(x + 6f, rowY, width - 12f, rowHeight, 1.5f, ClickGUIModule.clickGUIColor.rgba, 4f)
            row++
        }

        if (settingAnim.isAnimating()) NVGRenderer.popScissor()
        return displayHeight
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, click: MouseButtonEvent): Boolean {
        if (click.button() != 0) return false

        if (isHovered) {
            val from = getHeight()
            extended = !extended
            if (!extended) { addPhase = 0; pendingItem = null }
            animateFrom(from)
            return true
        }
        if (!extended) return false

        for (i in value.indices) {
            if (rowHovered(i)) {
                val (item, ench, level) = value[i]
                val max = enchantsFor(item)[ench]?.second ?: 10
                val relX = mouseX - lastX
                when {
                    relX in (width - 26f)..(width - 4f) -> { val from = getHeight(); value.removeAt(i); animateFrom(from) }
                    relX in (width - 54f)..(width - 30f) -> value[i] = Triple(item, ench, (level + 1).coerceAtMost(max))
                    relX in (width - 96f)..(width - 72f) -> value[i] = Triple(item, ench, (level - 1).coerceAtLeast(1))
                }
                return true
            }
        }

        if (rowHovered(value.size)) {
            val from = getHeight()
            if (addPhase == 0) addPhase = 1 else { addPhase = 0; pendingItem = null }
            animateFrom(from)
            return true
        }

        val list = phaseList()
        for (j in list.indices) {
            if (rowHovered(value.size + 1 + j)) {
                val from = getHeight()
                if (addPhase == 1) {
                    pendingItem = list[j]
                    addPhase = 2
                } else {
                    pendingItem?.let { value.add(Triple(it, list[j], enchantsFor(it)[list[j]]?.second ?: 1)) }
                    addPhase = 0
                    pendingItem = null
                }
                animateFrom(from)
                return true
            }
        }
        return false
    }

    override fun reset() {
        value = mutableListOf()
    }

    override fun write(gson: Gson): JsonElement {
        val array = JsonArray()
        for ((item, ench, level) in value) {
            val obj = JsonObject()
            obj.addProperty("item", item)
            obj.addProperty("enchant", ench)
            obj.addProperty("level", level)
            array.add(obj)
        }
        return array
    }

    override fun read(element: JsonElement, gson: Gson) {
        if (!element.isJsonArray) return
        val loaded = mutableListOf<Triple<String, String, Int>>()
        for (entry in element.asJsonArray) {
            val obj = entry.asJsonObject
            val item = obj.get("item")?.asString ?: continue
            val ench = obj.get("enchant")?.asString ?: continue
            if (item in items && ench in enchantsFor(item)) loaded.add(Triple(item, ench, obj.get("level")?.asInt ?: 1))
        }
        value = loaded
    }
}
