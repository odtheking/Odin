package me.odinmain.features

import me.odinmain.features.settings.Setting

/**
 * Placeholder class for config.
 * @author Aton
 */
class ConfigModule(
    name: String,
    keyCode: Int = 0,
    category: Category = Category.RENDER,
    toggled: Boolean = false,
    settings: ArrayList<Setting<*>> = ArrayList(),
    description: String = ""
) : Module(name, keyCode, category, toggled, settings, description)