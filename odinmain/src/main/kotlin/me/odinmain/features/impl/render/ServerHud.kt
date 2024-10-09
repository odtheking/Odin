@file:Suppress("UNUSED")

package me.odinmain.features.impl.render

import com.github.stivais.ui.constraints.percent
import com.github.stivais.ui.constraints.px
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.NumberSetting
import me.odinmain.utils.ServerUtils
import me.odinmain.utils.ui.and
import kotlin.reflect.jvm.isAccessible

object ServerHud : Module(
    name = "Server Hud",
    description = "Displays your current ping, FPS and server's TPS."
) {
    // todo:
    // create something for texts with multiple colors and if the text is varying or not
    private val fps by HUD(2.percent, 2.percent) {
        text(
            text = "FPS ",
            color = ClickGUI.color,
            size = 30.px
        ) and text({ ServerUtils.fps })
    }.setting("FPS HUD", "HUD, which displays your frames per second.")

    private val ping by HUD(8.percent, 2.percent) {
        text(
            text = "Ping ",
            color = ClickGUI.color,
            size = 30.px
        ) and text({ ServerUtils.averagePing.toInt() })
    }.setting("Ping HUD", "HUD, which displays your ping.")

    val test by NumberSetting("Hi", 0f)
    val test1 by BooleanSetting("Hi2", false)

    private val tps by HUD(14.percent, 2.percent) {
        text(
            text = "TPS ",
            color = ClickGUI.color,
            size = 30.px
        ) and text({ ServerUtils.averageTps.toInt() })
    }.apply {
        // test
        val delegate = ::test.apply { isAccessible = true }.getDelegate() as? Setting<*> ?: return@apply
        setting(delegate)
        val delegate2 = ::test1.apply { isAccessible = true }.getDelegate() as? Setting<*> ?: return@apply
        setting(delegate2)
    }.setting("TPS HUD", "HUD, which displays the server's TPS.")
}