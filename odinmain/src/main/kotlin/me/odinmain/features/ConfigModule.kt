package me.odinmain.features

/**
 * Placeholder class for config.
 * @author Aton
 */
class ConfigModule(
    name: String,
    keyCode: Int = 0,
    category: Category = Category.RENDER,
    description: String = ""
) : Module(name, keyCode, category, description)