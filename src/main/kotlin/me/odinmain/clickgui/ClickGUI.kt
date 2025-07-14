package me.odinmain.clickgui

import me.odinmain.clickgui.settings.impl.ColorSetting
import me.odinmain.config.Config
import me.odinmain.features.Category
import me.odinmain.features.impl.render.ClickGUIModule
import me.odinmain.utils.render.Color
import me.odinmain.utils.render.Colors
import me.odinmain.utils.ui.HoverHandler
import me.odinmain.utils.ui.Screen
import me.odinmain.utils.ui.animations.LinearAnimation
import me.odinmain.utils.ui.mouseX
import me.odinmain.utils.ui.mouseY
import me.odinmain.utils.ui.rendering.NVGRenderer
import net.minecraftforge.common.MinecraftForge
import org.lwjgl.opengl.Display
import kotlin.math.sign

/**
 * Renders all the modules.
 *
 * Backend made by Aton, with some changes
 * Design mostly made by Stivais
 *
 * @author Stivais, Aton
 * @see [Panel]
 */
object ClickGUI : Screen() {

    private val panels: ArrayList<Panel> = arrayListOf<Panel>().apply {
        if (Category.entries.any { ClickGUIModule.panelSetting[it] == null }) ClickGUIModule.resetPositions()
        for (category in Category.entries) add(Panel(category))
    }

    private var openAnim = LinearAnimation<Float>(400)
    val gray38 = Color(38, 38, 38)
    val gray26 = Color(26, 26, 26)

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    override fun draw() {
        NVGRenderer.beginFrame(Display.getWidth().toFloat(), Display.getHeight().toFloat())
        if (openAnim.isAnimating()) {
            NVGRenderer.translate(0f, openAnim.get(-10f, 0f))
            NVGRenderer.globalAlpha(openAnim.get(0f, 1f))
        }

        for (i in 0 until panels.size) { panels[i].draw(mouseX, mouseY) }
        SearchBar.draw(Display.getWidth() / 2f - 175f, Display.getHeight() - 110f, mouseX, mouseY)
        desc.render()

        NVGRenderer.endFrame()
    }

    override fun onScroll(amount: Int) {
        val actualAmount = (amount.sign * 16)
        for (i in panels.size - 1 downTo 0) {
            if (panels[i].handleScroll(actualAmount)) return
        }
        super.onScroll(amount)
    }

    override fun mouseClicked(x: Int, y: Int, mouseButton: Int) {
        SearchBar.mouseClicked(mouseX, mouseY, mouseButton)
        for (i in panels.size - 1 downTo 0) {
            if (panels[i].mouseClicked(mouseX, mouseY, mouseButton)) return
        }
        super.mouseClicked(x, y, mouseButton)
    }

    override fun mouseReleased(mouseX: Int, mouseY: Int, state: Int) {
        SearchBar.mouseReleased()
        for (i in panels.size - 1 downTo 0) {
            panels[i].mouseReleased(state)
        }
        super.mouseReleased(mouseX, mouseY, state)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        val searchTyped = SearchBar.keyTyped(typedChar)
        val searchPressed = SearchBar.keyPressed(keyCode)
        if (searchTyped || searchPressed) return

        for (i in panels.size - 1 downTo 0) {
            val panelTyped = panels[i].keyTyped(typedChar)
            val panelPressed = panels[i].keyPressed(keyCode)
            if (panelTyped || panelPressed) return
        }
        super.keyTyped(typedChar, keyCode)
    }

    override fun initGui() {
        openAnim.start()
        super.initGui()
    }

    override fun onGuiClosed() {
        for (panel in panels.filter { it.panelSetting.extended }.reversed()) {
            for (moduleButton in panel.moduleButtons.filter { it.extended }) {
                for (setting in moduleButton.representableSettings) {
                    if (setting is ColorSetting) setting.section = null
                    setting.listening = false
                }
            }
        }
        Config.save()
        super.onGuiClosed()
    }

    private var desc = Description("", 0f, 0f, HoverHandler(100))

    /** Sets the description without creating a new data class which isn't optimal */
    fun setDescription(text: String, x: Float, y: Float, hoverHandler: HoverHandler) {
        desc.text = text
        desc.x = x
        desc.y = y
        desc.hoverHandler = hoverHandler
    }

    data class Description(var text: String, var x: Float, var y: Float, var hoverHandler: HoverHandler) {

        fun render() {
            if (text.isEmpty() || hoverHandler.percent() <= 0) return
            val area = NVGRenderer.wrappedTextBounds(text, 300f, 16f, NVGRenderer.defaultFont)
            NVGRenderer.rect(x, y, area[2] - area[0] + 16f, area[3] - area[1] + 16f, gray38.rgba, 5f)
            NVGRenderer.hollowRect(x, y, area[2] - area[0] + 16f, area[3] - area[1] + 16f, 1.5f, ClickGUIModule.clickGUIColor.rgba, 5f)
            NVGRenderer.drawWrappedString(text, x + 8f, y + 8f, 300f, 16f, Colors.WHITE.rgba, NVGRenderer.defaultFont)
        }
    }

    val movementImage = NVGRenderer.createImage("/assets/odinmain/clickgui/MovementIcon.svg")
    val hueImage = NVGRenderer.createImage("/assets/odinmain/clickgui/HueGradient.png")
    val chevronImage = NVGRenderer.createImage("/assets/odinmain/clickgui/chevron.svg")
}