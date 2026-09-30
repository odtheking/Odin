package com.odtheking.odin.clickgui.settings.impl

import androidx.compose.runtime.Composable
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.odtheking.odin.OdinMod.mc
import com.odtheking.odin.clickgui.HudManager
import com.odtheking.odin.clickgui.settings.RenderableSetting
import com.odtheking.odin.clickgui.settings.Saving
import com.odtheking.odin.clickgui.ui.Icon
import com.odtheking.odin.clickgui.ui.SWITCH_HEIGHT
import com.odtheking.odin.clickgui.ui.SettingRow
import com.odtheking.odin.clickgui.ui.Switch
import com.odtheking.odin.features.Module
import net.minecraft.resources.Identifier

class HUDSetting(
    name: String,
    hud: HudElement,
    private val toggleable: Boolean = false,
    desc: String,
    val module: Module,
    val content: @Composable (example: Boolean) -> Unit,
) : RenderableSetting<HudElement>(name, desc), Saving {

    override val default: HudElement = hud
    override var value: HudElement = default

    val isEnabled: Boolean get() = module.enabled && value.enabled
    val hud get() = value

    @Composable
    override fun Content() {
        SettingRow {
            if (toggleable) Switch(hud.enabled, onClick = { hud.enabled = !hud.enabled })
            Icon(MOVEMENT, SWITCH_HEIGHT, onClick = { mc.setScreenAndShow(HudManager) })
        }
    }

    override fun write(gson: Gson): JsonElement = value.write()

    override fun read(element: JsonElement, gson: Gson) = value.read(element, toggleable)

    private companion object {
        val MOVEMENT: Identifier = Identifier.fromNamespaceAndPath("odin", "textures/movementicon.png")
    }
}