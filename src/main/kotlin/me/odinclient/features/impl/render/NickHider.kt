package me.odinclient.features.impl.render

import me.odinclient.features.Category
import me.odinclient.features.Module
import me.odinclient.features.settings.impl.StringSetting

/**
 * @see me.odinclient.mixin.MixinFontRenderer
 */
object NickHider : Module(
        name = "Nick Hider",
        category = Category.RENDER
) {

    val nick: String by StringSetting("Nick")

}