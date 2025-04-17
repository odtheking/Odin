package me.odinmain.features.impl.render

import me.odinmain.features.Module
import me.odinmain.features.settings.impl.StringSetting

object NameChanger : Module(
    name = "Name Changer",
    desc = "Replaces your name with the given nick, color codes work (&)."
) {
    private val nick by StringSetting("Nick", "Odin", 32, desc = "The nick to replace your name with.")

    @JvmStatic
    fun modifyString(string: String?): String? {
        if (!enabled || string == null) return string
        return string.replace(mc.session.username, nick.replace("&", "§").replace("$", ""))
    }
}
