package me.odinmain.features.impl.render

import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.settings.impl.StringSetting

/**
 * @see me.odinclient.mixin.mixins.MixinFontRenderer
 */
object NickHider : Module(
    name = "Nick Hider",
    category = Category.RENDER,
    description = "Replace your name, color codes work."
) {
    private val nick: String by StringSetting("Nick")

    @JvmStatic
    fun modifyString(string: String?): String? {
        if (!enabled || string == null) return string
        val name = mc.session.username
        val nick = nick.replace("&".toRegex(), "§").replace("\\$".toRegex(), "")
        return string.replace(name.toRegex(), nick)
    }
}
