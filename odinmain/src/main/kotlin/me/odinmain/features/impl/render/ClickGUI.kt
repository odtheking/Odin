package me.odinmain.features.impl.render

import com.github.stivais.ui.UI
import com.github.stivais.ui.UIScreen
import com.github.stivais.ui.UIScreen.Companion.open
import com.github.stivais.ui.animation.Animation
import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.color.Color
import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.constraints.measurements.Animatable
import com.github.stivais.ui.constraints.sizes.Bounding
import com.github.stivais.ui.elements.scope.*
import com.github.stivais.ui.operation.AnimationOperation
import com.github.stivais.ui.utils.*
import kotlinx.coroutines.launch
import me.odinmain.OdinMain
import me.odinmain.OdinMain.scope
import me.odinmain.config.Config
import me.odinmain.features.*
import me.odinmain.features.settings.AlwaysActive
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.ui.hud.EditHUDGui
import me.odinmain.utils.capitalizeFirst
import me.odinmain.utils.sendDataToServer
import me.odinmain.utils.skyblock.*
import net.minecraft.event.ClickEvent
import net.minecraft.util.ChatComponentText
import org.lwjgl.input.Keyboard
import me.odinmain.utils.render.Color as _Color

@AlwaysActive
object ClickGUI: Module(
    name = "Click GUI",
    key = Keyboard.KEY_RSHIFT,
    category = Category.RENDER,
    description = "Allows you to customize the UI"
) {
    val color by ColorSetting("Color", Color.RGB(50, 150, 220), allowAlpha = false, description = "Color mainly used within the UI")
    // todo: look into a blur screen?
//    val blur: Boolean by BooleanSetting("Blur", false, description = "Toggles the background blur for the gui.")
    val enableNotification: Boolean by BooleanSetting("Enable chat notifications", true, description = "Sends a message when you toggle a module with a keybind")

    // remove
    val oldColor by OldColorSetting("Gui Color", _Color(50, 150, 220), allowAlpha = false, description = "Color theme in the gui.")
    // remove
    val switchType: Boolean by DualSetting("Switch Type", "Checkbox", "Switch", default = true, description = "Switches the type of the settings in the gui.").hide()

    // by default on?
    val hudChat by BooleanSetting("Shows HUDs in GUIs", true, description = "Shows HUDs in GUIs")

    val forceHypixel by BooleanSetting("Force Hypixel", false, description = "Forces the hypixel check to be on (Mainly used for development. Only use if you know what you're doing)")

    // make useful someday
    val updateMessage by SelectorSetting("Update Message", "Beta", arrayListOf("Beta", "Full", "None")).hide()

    val devMessages: Boolean by BooleanSetting("Dev Messages", true, description = "Enables dev messages in chat.").withDependency { DevPlayers.isDev }
    val devSize: Boolean by BooleanSetting("Dev Size", true, description = "Toggles client side dev size.").withDependency { DevPlayers.isDev }
    private val devWings: Boolean by BooleanSetting("Dev Wings", false, description = "Toggles client side dev wings.").withDependency { DevPlayers.isDev }
    private val devWingsColor by OldColorSetting("Dev Wings Color", _Color(255, 255, 255), description = "Color of the dev wings.").withDependency { DevPlayers.isDev }
    private val devSizeX: Float by NumberSetting("Dev Size X", 1f, -1f, 3f, 0.1, description = "X scale of the dev size.").withDependency { DevPlayers.isDev && devSize }
    private val devSizeY: Float by NumberSetting("Dev Size Y", 1f, -1f, 3f, 0.1, description = "Y scale of the dev size.").withDependency { DevPlayers.isDev && devSize }
    private val devSizeZ: Float by NumberSetting("Dev Size Z", 1f, -1f, 3f, 0.1, description = "Z scale of the dev size.").withDependency { DevPlayers.isDev && devSize }
    private var showHidden: Boolean by DropdownSetting("Show Hidden", false).withDependency { DevPlayers.isDev }

    // todo: censored option for textinput
    private val passcode: String by StringSetting("Passcode", "odin", description = "Passcode for dev features.").withDependency { DevPlayers.isDev && showHidden }

    val reset by ActionSetting("Send Dev Data") {
        showHidden = false
        scope.launch {
            modMessage(sendDataToServer(body = "${mc.thePlayer.name}, [${devWingsColor.r},${devWingsColor.g},${devWingsColor.b}], [$devSizeX,$devSizeY,$devSizeZ], $devWings, $passcode", "https://tj4yzotqjuanubvfcrfo7h5qlq0opcyk.lambda-url.eu-north-1.on.aws/"))
            DevPlayers.updateDevs()
        }
    }.withDependency { DevPlayers.isDev }

    val action by ActionSetting("Open HUD Editor", description = "Opens the HUD Editor, allowing you to reposition HUDs") { OdinMain.display = EditHUDGui }

    private var joined: Boolean by BooleanSetting("First join", false).hide()
    var lastSeenVersion: String by StringSetting("Last seen version", "1.0.0").hide()

    var firstTimeOnVersion = false

    val panelSettings by MapSetting("Panel Data", mutableMapOf<Category, PanelData>()).also { setting ->
        Category.entries.forEach { setting.value[it] = PanelData(x = 10f + 260f * it.ordinal, y = 10f, extended = true) }
    }

    // remove
    val panelX = mutableMapOf<Category, NumberSetting<Float>>()
    val panelY = mutableMapOf<Category, NumberSetting<Float>>()
    val panelExtended = mutableMapOf<Category, BooleanSetting>()

    init {
        // todo: cleanup
        execute(250) {
            if (joined) destroyExecutor()
            if (!LocationUtils.inSkyblock) return@execute
            joined = true
            Config.save()

            modMessage("""
            ${getChatBreak()}
            §d§kOdinOnTopWeLoveOdinLiterallyTheBestModAAAAAAAAAAAAAAAA
            
            §7Thanks for installing §3Odin ${OdinMain.VERSION}§7!

            §7Use §d§l/od §r§7to access GUI settings.
            §7Use §d§l/od help §r§7for all of of the commands.
             
            §7Join the discord for support and suggestions.
            """.trimIndent(), false)
            mc.thePlayer.addChatMessage(
                ChatComponentText(" §9https://discord.gg/2nCbC9hkxT")
                    .setChatStyle(createClickStyle(ClickEvent.Action.OPEN_URL, "https://discord.gg/2nCbC9hkxT"))
            )

            modMessage("""
            
            §d§kOdinOnTopWeLoveOdinLiterallyTheBestModAAAAAAAAAAAAAAAA
            ${getChatBreak()}
            
            """.trimIndent(), false)
        }
        resetPositions()
    }

    // todo: remove
    fun resetPositions() {
        Category.entries.forEach {
            val incr = 10f + 260f * it.ordinal
            panelX.getOrPut(it) { +NumberSetting(it.name + ",x", default = incr, hidden = true) }.value = incr
            panelY.getOrPut(it) { +NumberSetting(it.name + ",y", default = 10f, hidden = true) }.value = 10f
            panelExtended.getOrPut(it) { +BooleanSetting(it.name + ",extended", default = true, hidden = true) }.enabled = true
        }
    }

    override fun onKeybind() {
        this.toggle()
    }

    override fun onEnable() {
        open(clickGUI())
        super.onEnable()
        toggle()
    }

    @JvmField
    val `gray 26`: Color = Color.RGB(26, 26, 26)

    @JvmField
    val `gray 38`: Color = Color.RGB(38, 38, 38)

    fun clickGUI() = UI {
        // used for search bar to not require iterating over all elements
        val moduleElements = arrayListOf<Pair<Module, ElementDSL>>()
        onRemove {
            Config.save()
        }
        for (panel in Category.entries) {
            val data = panelSettings[panel] ?: throw NullPointerException("This should never happen")
            column(at(x = data.x.px, y = data.y.px)) {
                onRemove {
                    data.x = element.x
                    data.y = element.y
                }
                onScroll { (amount) ->
                    child(1)!!.scroll(amount, 0.1.seconds, Animations.Linear); true
                }
                // panel header
                block(
                    size(240.px, 40.px),
                    color = `gray 26`,
                    radius = radius(tl = 5, tr = 5)
                ) {
                    text(
                        text = panel.name.capitalizeFirst(),
                        size = 20.px
                    )
                    onClick(1) {
                        sibling()!!.height.animate(0.5.seconds, Animations.EaseInOutQuint)
                        data.extended = !data.extended; true
                    }
                    draggable(moves = parent!!)
                }
                // modules
                column(size(h = Animatable(from = Bounding, to = 0.px, swap = !data.extended))) {
                    background(color = Color.RGB(38, 38, 38, 0.7f))
                    scissors()
                    for (module in ModuleManager.modules) {
                        if (module.category != panel) continue
                        val it = module(module)
                        moduleElements.add(module to it)
                    }
                }
                // tail
                block(
                    size(240.px, 10.px),
                    color = `gray 26`,
                    radius = radius(br = 5, bl = 5)
                )
            }
        }
        openAnim(0.5.seconds, Animations.EaseOutQuint)
        closeAnim(0.5.seconds, Animations.EaseInBack)
    }

    private fun ElementDSL.module(module: Module) = column(size(h = Animatable(from = 32.px, to = Bounding))) {
        // used to lazily load setting elements, as they're not visible until clicked and most of them go unseen
        var loaded = false
        val color = Color.Animated(from = `gray 26`, to = this@ClickGUI.color)
        block(
            size(240.px, 32.px),
            color = color
        ) {
            hoverEffect(0.1.seconds)
            text(
                text = module.name,
                size = 16.px
            )
            onClick {
                color.animate(0.15.seconds)
                module.toggle(); true
            }
            onClick(button = 1) {
                // load settings if haven't yet
                if (!loaded) {
                    loaded = true
                    module.settings.loop { if (!it.hidden) it.apply { this@column.createElement() } }
                    redraw()
                }
                parent()!!.height.animate(0.25.seconds, Animations.EaseOutQuint); true
            }
        }
    }

    // todo: move out and cleanup
    fun ElementDSL.openAnim(
        duration: Float,
        animation: Animations,
    ) {
        onCreation {
            // test
            AnimationOperation(Animation(duration, animation)) {
                element.alpha = it
                element.scale = it
            }.add()
        }
    }

    // todo: move out and cleanup
    fun ElementDSL.closeAnim(duration: Float, animation: Animations) {
        onRemove {
            UIScreen.closeAnimHandler = ui.window as UIScreen
            // test
            AnimationOperation(Animation(duration, animation).onFinish { UIScreen.closeAnimHandler = null }) {
                element.alpha = 1f - it
                element.scale = 1f - it
            }.add()
        }
    }

    data class PanelData(var x: Float, var y: Float, var extended: Boolean) {
        val defaultX = x
        val defaultY = y
        val defaultExtended = extended
    }
}