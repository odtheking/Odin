package com.odtheking.odin.features.impl.dungeon.dungeonwaypoints

import com.odtheking.odin.OdinMod.mc
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.components.StringWidget
import net.minecraft.client.gui.layouts.FrameLayout
import net.minecraft.client.gui.layouts.LinearLayout
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.input.KeyEvent
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import org.lwjgl.glfw.GLFW

class TextPromptScreen(val promptTitle: String) : Screen(Component.literal(promptTitle)) {
    private lateinit var textField: EditBox
    private var callback: (String) -> Unit = {}
    private lateinit var layout: LinearLayout

    override fun init() {
        super.init()

        layout = LinearLayout.vertical().spacing(8)
        layout.defaultCellSetting().alignHorizontallyCenter()

        layout.addChild(StringWidget(Component.literal("§6§l$promptTitle"), font))

        textField = EditBox(
            font, 0, 0,
            200, 20, Component.literal("Enter text")
        )
        textField.setMaxLength(32)
        layout.addChild(textField)

        val buttonLayout = layout.addChild(LinearLayout.horizontal().spacing(8))
        buttonLayout.defaultCellSetting().paddingTop(8)

        buttonLayout.addChild(Button.builder(CommonComponents.GUI_DONE) { _ ->
            callback.invoke(textField.value)
        }.width(100).build())

        buttonLayout.addChild(Button.builder(CommonComponents.GUI_CANCEL) { _ ->
            mc.setScreen(null)
        }.width(100).build())

        layout.visitWidgets(this::addRenderableWidget)
        repositionElements()

        setInitialFocus(textField)
    }

    override fun repositionElements() {
        layout.arrangeElements()
        FrameLayout.centerInRectangle(layout, rectangle)
    }

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        val dialogWidth = 300
        val dialogHeight = 140
        val dialogX = (width - dialogWidth) / 2
        val dialogY = (height - dialogHeight) / 2

        context.fill(dialogX, dialogY, dialogX + dialogWidth, dialogY + dialogHeight, 0xE0101010.toInt())

        context.fill(dialogX - 1, dialogY - 1, dialogX + dialogWidth + 1, dialogY, 0xFF404040.toInt())
        context.fill(dialogX - 1, dialogY + dialogHeight, dialogX + dialogWidth + 1, dialogY + dialogHeight + 1, 0xFF404040.toInt())
        context.fill(dialogX - 1, dialogY, dialogX, dialogY + dialogHeight, 0xFF404040.toInt())
        context.fill(dialogX + dialogWidth, dialogY, dialogX + dialogWidth + 1, dialogY + dialogHeight, 0xFF404040.toInt())

        super.render(context, mouseX, mouseY, delta)
    }

    override fun keyPressed(input: KeyEvent): Boolean {
        if (input.key == GLFW.GLFW_KEY_ESCAPE) {
            mc.setScreen(null)
            return true
        }

        if (input.key == GLFW.GLFW_KEY_ENTER || input.key == GLFW.GLFW_KEY_KP_ENTER) {
            callback.invoke(textField.value)
            return true
        }

        return super.keyPressed(input)
    }


    override fun isPauseScreen(): Boolean = false

    fun setCallback(callback: (String) -> Unit): TextPromptScreen {
        this.callback = callback
        return this
    }
}