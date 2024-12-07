package me.odinmain.features.impl.render

import com.github.stivais.aurora.animations.Animation
import com.github.stivais.aurora.color.Color
import com.github.stivais.aurora.constraints.impl.measurements.Animatable
import com.github.stivais.aurora.constraints.impl.size.Bounding
import com.github.stivais.aurora.constraints.impl.size.Copying
import com.github.stivais.aurora.dsl.*
import com.github.stivais.aurora.elements.ElementScope
import com.github.stivais.aurora.elements.impl.Block.Companion.outline
import com.github.stivais.aurora.elements.impl.Popup
import com.github.stivais.aurora.elements.impl.Scrollable.Companion.scroll
import com.github.stivais.aurora.elements.impl.TextInput.Companion.onTextChanged
import com.github.stivais.aurora.elements.impl.popup
import com.github.stivais.aurora.utils.*
import kotlinx.coroutines.launch
import me.odinmain.OdinMain
import me.odinmain.OdinMain.scope
import me.odinmain.config.Config
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.ModuleManager
import me.odinmain.features.huds.HUDManager
import me.odinmain.features.settings.AlwaysActive
import me.odinmain.features.settings.Setting
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.capitalizeFirst
import me.odinmain.utils.sendDataToServer
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.createClickStyle
import me.odinmain.utils.skyblock.getChatBreak
import me.odinmain.utils.skyblock.modMessage
import me.odinmain.utils.ui.*
import me.odinmain.utils.ui.renderer.NVGRenderer
import me.odinmain.utils.ui.renderer.NVGRenderer.textWidth
import me.odinmain.utils.ui.screens.UIScreen.Companion.open
import net.minecraft.event.ClickEvent
import net.minecraft.util.ChatComponentText
import org.lwjgl.input.Keyboard

@AlwaysActive
object ClickGUI : Module(
    name = "Click GUI",
    key = Keyboard.KEY_RSHIFT,
    description = "Allows you to customize the UI."
) {
    /**
     * Main color used within the mod.
     */
    val color by ColorSetting("Color", Color.RGB(50, 150, 220), allowAlpha = false, description = "Main color theme for Odin.")

    val enableNotification by BooleanSetting("Enable chat notifications", true, description = "Sends a message when you toggle a module with a keybind")

    val forceHypixel by BooleanSetting("Force Hypixel", false, description = "Forces the Hypixel check to be on (Mainly used for development. Only use if you know what you're doing)")

    // make useful someday
    val updateMessage by SelectorSetting("Update Message", arrayListOf("Full", "Beta", "None"), description = "").hide()

    // needs own module
    val devMessages by BooleanSetting("Dev Messages", true, description = "Enables dev messages in chat.").withDependency { DevPlayers.isDev } // make dev-specific modules and put this there
    val devSize by BooleanSetting("Dev Size", true, description = "Toggles client side dev size.").withDependency { DevPlayers.isDev }
    private val devWings by BooleanSetting("Dev Wings", false, description = "Toggles client side dev wings.").withDependency { DevPlayers.isDev }
    private val devWingsColor by ColorSetting("Dev Wings Color", Color.RGB(255, 255, 255), description = "Color of the dev wings.").withDependency { DevPlayers.isDev }
    private val devSizeX by NumberSetting("Dev Size X", 1f, -1f, 3f, 0.1, description = "X scale of the dev size.").withDependency { DevPlayers.isDev && devSize }
    private val devSizeY by NumberSetting("Dev Size Y", 1f, -1f, 3f, 0.1, description = "Y scale of the dev size.").withDependency { DevPlayers.isDev && devSize }
    private val devSizeZ by NumberSetting("Dev Size Z", 1f, -1f, 3f, 0.1, description = "Z scale of the dev size.").withDependency { DevPlayers.isDev && devSize }
    private val passcode by StringSetting("Passcode", "odin", description = "Passcode for dev features.").withDependency { DevPlayers.isDev }.censors()

    val reset by ActionSetting("Send Dev Data") {
        scope.launch {
            modMessage(
                sendDataToServer(
                    body = "${mc.thePlayer.name}, [${devWingsColor.red},${devWingsColor.green},${devWingsColor.blue}], [$devSizeX,$devSizeY,$devSizeZ], $devWings, $passcode",
                    "https://tj4yzotqjuanubvfcrfo7h5qlq0opcyk.lambda-url.eu-north-1.on.aws/"
                )
            )
            DevPlayers.updateDevs()
        }
    }.withDependency { DevPlayers.isDev }

    val action by ActionSetting(
        "Open HUD Editor",
        description = "Opens the HUD Editor, allowing you to reposition HUDs"
    ) {
        open(HUDManager.makeHUDEditor())
    }

    val panelSettings by MapSetting("panel.data", mutableMapOf<Category, PanelData>()).also { setting ->
        Category.entries.forEach {
            setting.value[it] = PanelData(x = 10f + 260f * it.ordinal, y = 10f, extended = true)
        }
    }

    private var joined by BooleanSetting("first.join", false, description = "").hide()
    var lastSeenVersion by StringSetting("last.seen.version", "1.0.0", description = "").hide()

    /**
     * Used in [ColorSetting].
     */
    val favoriteColors by ListSetting("favorite.colors", mutableListOf<Color.HSB>())

    var firstTimeOnVersion = false

    init {
        execute(250) {
            if (joined) destroyExecutor()
            if (!LocationUtils.isInSkyblock) return@execute
            joined = true
            Config.save()

            modMessage(
                """
            ${getChatBreak()}
            §d§kOdinOnTopWeLoveOdinLiterallyTheBestModAAAAAAAAAAAAAAAA
            
            §7Thanks for installing §3Odin ${OdinMain.VERSION}§7!

            §7Use §d§l/od §r§7to access GUI settings.
            §7Use §d§l/od help §r§7for all of of the commands.
             
            §7Join the discord for support and suggestions.
            """.trimIndent(), ""
            )
            mc.thePlayer.addChatMessage(
                ChatComponentText(" §9https://discord.gg/2nCbC9hkxT")
                    .setChatStyle(createClickStyle(ClickEvent.Action.OPEN_URL, "https://discord.gg/2nCbC9hkxT"))
            )

            modMessage(
                """
            
            §d§kOdinOnTopWeLoveOdinLiterallyTheBestModAAAAAAAAAAAAAAAA
            ${getChatBreak()}
            
            """.trimIndent(), ""
            )
        }
    }

    override fun onKeybind() {
        open(clickGUI())
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

    fun clickGUI() = Aurora(renderer = NVGRenderer) {

        // used for search bar to not require iterating over all elements
        val moduleElements = arrayListOf<Pair<Module, ElementScope<*>>>()

        onRemove {
            Config.save()
            HUDManager.UI?.close()
            HUDManager.setupHUDs()
        }

        for (panel in Category.entries) {
            val data = panelSettings[panel] ?: throw NullPointerException("This should never happen")
            column(at(x = data.x.px, y = data.y.px)) {
                onRemove {
                    data.x = element.x
                    data.y = element.y
                }

                val height = Animatable(from = Bounding, to = 0.px, swapIf = !data.extended)

                // panel header
                block(
                    size(240.px, 40.px),
                    color = `gray 26`,
                    radius = radius(tl = 5, tr = 5)
                ) {
                    text(
                        string = panel.displayName,
                        size = 20.px
                    )
                    onClick(1) {
                        height.animate(0.5.seconds, Animation.Style.EaseInOutQuint)
                        redraw()
                        data.extended = !data.extended; true
                    }
                    draggable(moves = element.parent!!, coerce = false)
                }

                //---------//
                // modules //
                //---------//
                val scrollable = scrollable(size(Bounding, Bounding)) {
                    column(size(h = height)) {
                        // bg
                        block(
                            copies(),
                            color = `gray 38`.withAlpha(0.7f)
                        )
                        for (module in ModuleManager.modules.sortedByDescending { textWidth(it.name, 18f, font = regularFont) }) {
                            if (module.category != panel) continue
                            moduleElements.add(module to module(module))
                        }
                    }
                }
                // tail
                block(
                    size(240.px, 10.px),
                    color = `gray 26`,
                    radius = radius(br = 5, bl = 5)
                )

                // used for smoother transition between header and modules when they're scrolled
//                block(
//                    constrain(y = Linked(header.element), w = 240.px, h = 3.px),
//                    colors = `gray 26` to Color.TRANSPARENT,
//                    gradient = Gradient.TopToBottom
//                )

                onScroll { (amount) ->
                    scrollable.scroll(amount * -75f, style = Animation.Style.EaseOutQuint)
                    true
                }
            }
        }

        //------------//
        // search bar //
        //------------//
        block(
            constrain(x = 40.percent, y = 90.percent, w = 20.percent, 4.percent),
            color = `gray 26`,
            radius = 10.radius()
        ) {
            outline(
                this@ClickGUI.color,
                thickness = 2.px,
            )
            val input = textInput(placeholder = "Search") {
                onTextChanged { (string) ->
                    moduleElements.loop { (module, element) ->
                        element.enabled = module.name.contains(string, true) || module.description.contains(string, true)
                    }
                    this@Aurora.redraw()
                }
            }
            onClick {
                ui.focus(input.element)
            }
            draggable(button = 1)
        }

        lifetimeAnimations(duration = 0.5.seconds, Animation.Style.EaseOutQuint, Animation.Style.EaseInBack)
    }

    private fun ElementScope<*>.module(module: Module): ElementScope<*> {
        var loaded = false
        val height = Animatable(from = 32.px, to = Bounding)
        val color = Color.Animated(from = `gray 26`, to = this@ClickGUI.color, swapIf = module.enabled)
        return column(size(w = 240.px, h = height)) {
            block(
                size(Copying, 32.px),
                color
            ) {
                hoverEffect(factor = 1.2f, style = Animation.Style.Linear)
                text(
                    string = module.name,
                    size = 50.percent
                )
                hoverInformation(
                    description = module.description
                )
                onClick {
                    color.animate(0.15.seconds, Animation.Style.Linear)
                    module.toggle()
                }
                onClick(button = 1) { _ ->
                    if (!loaded) {
                        loaded = true
                        module.settings.loop { setting ->
                            if (!setting.hidden && setting is Setting.Renders) {
                                this@column.scope(setting.Drawable()) {
                                    hoverInformation(
                                        description = setting.description
                                    )
                                    setting.apply { create() }
                                }
                            }
                        }
                    }
                    height.animate(0.25.seconds, Animation.Style.EaseInOutQuint)
                    redraw()
                    true
                }
            }
        }
    }

    fun ElementScope<*>.hoverInformation(description: String) {
        if (description.isEmpty()) return

        val lines = arrayListOf<String>()
        var lastIndex = 0
        var spaces = 0
        for (index in description.indices) {
            if (description[index] == ' ') {
                spaces++
            }
            if (spaces == 5 || index == description.lastIndex) {
                lines.add(description.substring(lastIndex, index + 1))
                lastIndex = index + 1
                spaces = 0
            }
        }

        var popup: Popup? = null

        onHover(duration = 1.seconds) {
            if (popup != null) return@onHover

            val it = element
            val x = if (it.x >= ui.main.width / 2f) (it.x - 10).px.alignRight else (it.x + it.width + 10).px
            val y = (it.y + 5).px

            popup = popup(constrain(x, y, Bounding, Bounding), smooth = true) {
                block(
                    constraints = bounds(padding = 7.5.px),
                    color = `gray 38`,
                    radius = 5.radius()
                ) {
                    outline(this@ClickGUI.color, 2.px)
                    column {
                        lines.loop {
                            text(
                                string = it,
                                size = 15.px
                            )
                        }
                    }
                }
            }
            onMouseExit {
                popup?.let {
                    it.closePopup()
                    popup = null
                }
            }
        }
    }

    data class PanelData(var x: Float, var y: Float, var extended: Boolean) {
        val defaultX = x
        val defaultY = y
    }
}
