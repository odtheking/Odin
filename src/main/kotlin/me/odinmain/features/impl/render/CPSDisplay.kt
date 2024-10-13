package me.odinmain.features.impl.render

import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.percent
import com.github.stivais.ui.constraints.px
import me.odinmain.features.Module
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.BooleanSetting
import me.odinmain.features.settings.impl.ColorSetting
import me.odinmain.features.settings.impl.DropdownSetting
import me.odinmain.features.settings.impl.SelectorSetting
import me.odinmain.utils.ui.TextHUD
import me.odinmain.utils.ui.and
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

object CPSDisplay : Module(
    name = "CPS Display",
    description = "Customizable CPS Display"
) {
    // make it depend on right clicks to be shown
    private val countPackets by BooleanSetting("Count Packets", false, description = "Counts packets sent outside of the rightclickmouse method, this will be better at detecting other mods' auto clickers, but might show inaccurate values.")
    private val advanced by DropdownSetting("Show Settings", false)
    private val button by SelectorSetting("Button", "Both", arrayListOf("Left", "Right", "Both")).withDependency { advanced }
    private val mouseText by BooleanSetting("Show Button", true).withDependency { advanced }
    private val textColor by ColorSetting("Text Color", Color.RGB(239, 239, 239, 1f), allowAlpha = true).withDependency { advanced }
    private val outline by BooleanSetting("Outline", true).withDependency { advanced }

    private val leftClicks = mutableListOf<Long>()
    private val rightClicks = mutableListOf<Long>()

    // todo: make it look somewhat how it used to

    private val HUD by TextHUD(
        2.5.percent,
        2.5.percent,
    ) { color, font ->
        text(
            "CPS ",
            color = color,
            font = font,
            size = 30.px
        ) and text({ rightClicks.size }, font = font)
    }.setting("CPS")

    init {
        onEvent<ClientTickEvent> {
            if (leftClicks.size != 0 && System.currentTimeMillis() - leftClicks.first() > 1000) {
                leftClicks.removeFirst()
            }
            if (rightClicks.size != 0 && System.currentTimeMillis() - rightClicks.first() > 1000) {
                rightClicks.removeFirst()
            }
        }
        onPacket<C08PacketPlayerBlockPlacement> {
            if (countPackets) {
                if (rightClicks.size == 0 || System.currentTimeMillis() - rightClicks.last() > 5) {
                    onRightClick()
                }
            }
        }
    }

    @JvmStatic
    fun onLeftClick() {
        leftClicks.add(System.currentTimeMillis())
    }

    @JvmStatic
    fun onRightClick() {
        rightClicks.add(System.currentTimeMillis())
    }
}