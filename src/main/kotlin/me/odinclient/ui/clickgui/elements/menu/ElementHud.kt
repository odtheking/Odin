package me.odinclient.ui.clickgui.elements.menu

import cc.polyfrost.oneconfig.renderer.font.Fonts
import cc.polyfrost.oneconfig.utils.dsl.*
import me.odinclient.OdinClient
import me.odinclient.features.settings.impl.ActionSetting
import me.odinclient.features.settings.impl.BooleanSetting
import me.odinclient.features.settings.impl.HudSetting
import me.odinclient.ui.clickgui.elements.Element
import me.odinclient.ui.clickgui.elements.ElementType
import me.odinclient.ui.clickgui.elements.ModuleButton
import me.odinclient.ui.clickgui.util.ColorUtil
import me.odinclient.ui.clickgui.util.ColorUtil.darker
import me.odinclient.ui.hud.ExampleHudGui
import me.odinclient.utils.render.gui.GuiUtils.drawOutlineRoundedRect
import me.odinclient.utils.render.gui.GuiUtils.nanoVG
import me.odinclient.utils.render.gui.GuiUtils.resetScissor
import me.odinclient.utils.render.gui.GuiUtils.scissor
import me.odinclient.utils.render.gui.MouseUtils
import me.odinclient.utils.render.gui.animations.impl.EaseInOut
import me.odinclient.utils.skyblock.ChatUtils.modMessage
import org.lwjgl.input.Mouse
import kotlin.math.floor

class ElementHud(parent: ModuleButton, setting: HudSetting) : Element<HudSetting>(
    parent, setting, ElementType.DUAL
) {
    override val isHovered: Boolean
        get() = MouseUtils.isAreaHovered(x + width - 41, y + 5, 31.5f, 19f)

    private val anim = EaseInOut(200)

    private val isEnabledSetting = BooleanSetting("Enabled", setting.isEnabled)
    private val isEnabledElement = ElementCheckBox(this.parent, isEnabledSetting)

    private val openExampleHudSetting = ActionSetting("Open Example Hud") {
        OdinClient.display = ExampleHudGui
    }
    private val openExampleHudElement = ElementAction(this.parent, openExampleHudSetting)

    override fun draw(vg: VG) {
        vg.nanoVG {
            drawRect(x, y, width, 36f, ColorUtil.elementBackground)
            height = floor(anim.get(36f, 100f, !extended))

            drawText(displayName, x + 6f, y + 18f, -1, 16f, Fonts.REGULAR)
            drawDropShadow(x + width - 40f, y + 5f, 31f, 19f, 10f, 0.75f, 5f)
            drawRoundedRect(x + width - 40f, y + 5f, 31f, 19f, 5f, ColorUtil.clickGUIColor.rgba)
            drawOutlineRoundedRect(x + width - 40f, y + 5f, 31f, 19f, 5f, ColorUtil.clickGUIColor.darker().rgba, 1.5f)
            if (isHovered) drawOutlineRoundedRect(x + width - 40f, y + 5f, 31f, 19f, 5f, ColorUtil.boxHoverColor, 1.5f)

            if (!extended && !anim.isAnimating()) return@nanoVG
            val sc = scissor(x, y + 36f, width, anim.get(0f, 100f, !extended))

            isEnabledElement.y = parent.menuElements.sumOf { it.height.toInt() } - 64f
            isEnabledElement.render(vg)

            openExampleHudElement.y =  parent.menuElements.sumOf { it.height.toInt() } - 32f
            openExampleHudElement.render(vg)

            resetScissor(sc)
        }

    }

    override fun mouseClicked(mouseButton: Int): Boolean {
        if (mouseButton == 0 && isHovered) {
            if (anim.start()) extended = !extended
            return true
        } else if (isEnabledElement.mouseClicked(mouseButton)) {
            setting.isEnabled = isEnabledSetting.value
            return true
        } else if (openExampleHudElement.mouseClicked(mouseButton)) {
            return true
        }
        return false
    }
}