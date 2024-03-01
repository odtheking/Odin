package me.odinmain.features.impl.render

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.StringSetting

/**
 * @see me.odinclient.mixin.mixins.MixinFontRenderer
 */
object NameChanger : Module(
    name = "Name Changer",
    category = Category.RENDER,
    description = "Replaces your name with the given nick, color codes work (&)."
) {
    private val nick: String by StringSetting("Nick")

    @JvmStatic
    fun modifyString(string: String?): String? {
        if (!enabled || string == null) return string
        return string.replace(mc.session.username, nick.replace("&", "§").replace("$", ""))
    }
}
