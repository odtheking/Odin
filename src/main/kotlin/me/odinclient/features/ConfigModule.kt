package me.odinclient.features

import me.odinclient.features.settings.Setting

class ConfigModule(name: String,
                   keyCode: Int = 0,
                   category: Category = Category.GENERAL,
                   toggled: Boolean = false,
                   settings: ArrayList<Setting<*>> = ArrayList(),
                   description: String = ""
) : Module(name, keyCode, category, toggled, settings, description)