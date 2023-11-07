package me.odinmain.features.impl.render

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.StringSetting

/**
 * @see me.odinmain.mixin.MixinFontRenderer
 */
object NickHider : Module(
    name = "Nick Hider",
    category = Category.RENDER,
    description = "Replace your name, color codes work."
) {
    val nick: String by StringSetting("Nick")
}