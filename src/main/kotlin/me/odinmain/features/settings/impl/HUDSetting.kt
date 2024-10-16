package me.odinmain.features.settings.impl

import com.github.stivais.ui.elements.scope.ElementDSL
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import me.odinmain.features.Module
import me.odinmain.features.settings.Saving
import me.odinmain.features.settings.Setting

class HUDSetting(
    name: String,
    hud: Module.HUD,
    description: String,
) : Setting<Module.HUD>(name, false, description), Saving {

    override val default: Module.HUD = hud
    override var value: Module.HUD = hud

    override fun write(): JsonElement {
        return JsonObject().apply {
            addProperty("x", value.x.percent)
            addProperty("y", value.y.percent)
            addProperty("scale", value.scale)
            addProperty("enabled", value.enabled)
        }
    }

    override fun read(element: JsonElement?) {
        element?.asJsonObject?.apply {
            value.x.percent = get("x").asFloat
            value.y.percent = get("y").asFloat
            value.scale = get("scale").asFloat
            value.enabled = get("enabled").asBoolean
        }
    }

    override fun reset() {
        super.reset()
    }

    override fun ElementDSL.createElement() {
        // todo: style it like a dropdown, where it drops down settings under the hud
    }
}