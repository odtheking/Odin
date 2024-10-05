package me.odinmain.features.impl.render

import me.odinmain.features.*
import me.odinmain.features.settings.impl.StringSetting

/**
 * @see me.odinclient.mixin.mixins.MixinFontRenderer
 */
object NameChanger : Module(
    name = "Name Changer",
    category = Category.RENDER,
    description = "Replaces your name with the given nick, color codes work (&)."
) {
    private val nick by StringSetting("Nick", "Odin", 32, description = "The nick to replace your name with.")

    @JvmStatic
    fun modifyString(string: String?): String? {
        if (!enabled || string == null) return string
        return string.replace(mc.session.username, nick.replace("&", "§").replace("$", ""))
    }
}
