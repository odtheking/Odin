package me.odinmain.features.impl.render

import com.github.stivais.ui.UI
import com.github.stivais.ui.UIScreen
import com.github.stivais.ui.UIScreen.Companion.open
import com.github.stivais.ui.animation.Animation
import com.github.stivais.ui.animation.Animations
import com.github.stivais.ui.color.*
import com.github.stivais.ui.constraints.*
import com.github.stivais.ui.constraints.measurements.Animatable
import com.github.stivais.ui.constraints.sizes.Bounding
import com.github.stivais.ui.elements.impl.Popup
import com.github.stivais.ui.elements.impl.popup
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
import me.odinmain.utils.capitalizeFirst
import me.odinmain.utils.sendDataToServer
import me.odinmain.utils.skyblock.*
import net.minecraft.event.ClickEvent
import net.minecraft.util.ChatComponentText
import org.lwjgl.input.Keyboard

@AlwaysActive
object ClickGUI: Module(
    name = "Click GUI",
    key = Keyboard.KEY_RSHIFT,
    description = "Allows you to customize the UI"
) {
    val color by ColorSetting("Color", Color.RGB(50, 150, 220), allowAlpha = false, description = "Color mainly used within the UI")
    // todo: look into a blur screen?
//    val blur: Boolean by BooleanSetting("Blur", false, description = "Toggles the background blur for the gui.")
    val enableNotification: Boolean by BooleanSetting("Enable chat notifications", true, description = "Sends a message when you toggle a module with a keybind")

    // by default on?
    val hudChat by BooleanSetting("Shows HUDs in GUIs", true, description = "Shows HUDs in GUIs")

    val forceHypixel by BooleanSetting("Force Hypixel", false, description = "Forces the hypixel check to be on (Mainly used for development. Only use if you know what you're doing)")

    // make useful someday
    val updateMessage by SelectorSetting("Update Message", "Beta", arrayListOf("Beta", "Full", "None")).hide()

    val devMessages by BooleanSetting("Dev Messages", true, description = "Enables dev messages in chat.").withDependency { DevPlayers.isDev }
    val devSize by BooleanSetting("Dev Size", true, description = "Toggles client side dev size.").withDependency { DevPlayers.isDev }
    private val devWings by BooleanSetting("Dev Wings", false, description = "Toggles client side dev wings.").withDependency { DevPlayers.isDev }
    private val devWingsColor by ColorSetting("Dev Wings Color", Color.RGB(255, 255, 255), description = "Color of the dev wings.").withDependency { DevPlayers.isDev }
    private val devSizeX by NumberSetting("Dev Size X", 1f, -1f, 3f, 0.1, description = "X scale of the dev size.").withDependency { DevPlayers.isDev && devSize }
    private val devSizeY by NumberSetting("Dev Size Y", 1f, -1f, 3f, 0.1, description = "Y scale of the dev size.").withDependency { DevPlayers.isDev && devSize }
    private val devSizeZ by NumberSetting("Dev Size Z", 1f, -1f, 3f, 0.1, description = "Z scale of the dev size.").withDependency { DevPlayers.isDev && devSize }
    private var showHidden by DropdownSetting("Show Hidden", false).withDependency { DevPlayers.isDev }

    // todo: censored option for textinput, max length, etc idk man its so much work
    private val passcode: String by StringSetting("Passcode", "odin", description = "Passcode for dev features.").withDependency { DevPlayers.isDev && showHidden }

    val reset by ActionSetting("Send Dev Data") {
        showHidden = false
        scope.launch {
            modMessage(sendDataToServer(body = "${mc.thePlayer.name}, [${devWingsColor.red},${devWingsColor.green},${devWingsColor.blue}], [$devSizeX,$devSizeY,$devSizeZ], $devWings, $passcode", "https://tj4yzotqjuanubvfcrfo7h5qlq0opcyk.lambda-url.eu-north-1.on.aws/"))
            DevPlayers.updateDevs()
        }
    }.withDependency { DevPlayers.isDev }

    val action by ActionSetting("Open HUD Editor", description = "Opens the HUD Editor, allowing you to reposition HUDs") { /*OdinMain.display = EditHUDGui*/ }

    private var joined by BooleanSetting("First join", false).hide()
    private var warned by BooleanSetting("Warned", false).hide()
    var lastSeenVersion by StringSetting("Last seen version", "1.0.0").hide()

    var firstTimeOnVersion = false

    val panelSettings by MapSetting("Panel Data", mutableMapOf<Category, PanelData>()).also { setting ->
        Category.entries.forEach { setting.value[it] = PanelData(x = 10f + 260f * it.ordinal, y = 10f, extended = true) }
    }

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
                column(size(h = Animatable(from = Bounding, to = 0.px, swapIf = !data.extended))) {
                    background(color = Color.RGB(38, 38, 38, 0.7f))
                    scissors()
                    for (module in ModuleManager.modules.sortedBy { ui.renderer.textWidth(it.name, 16f) }) {
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

        // search bar
        block(constrain(y = 80.percent, w = 25.percent, h = 5.percent), color = `gray 38`, radius = 10.radii()) {
            textInput(placeholder = "Search") { str ->
                moduleElements.loop { (module, element) ->
                    element.enabled = module.name.contains(str, true)
                }
                this@UI.redraw()
            }
            onClick {
                child(0)!!.focusThis(); true
            }
            outline(this@ClickGUI.color, thickness = 2.px)
            draggable(button = 1)
        }

        openAnim(0.5.seconds, Animations.EaseOutQuint)
        closeAnim(0.5.seconds, Animations.EaseInBack)

        // for fun icl
        if (!warned) {
            uiBranchWarning()
        }
    }

    private fun ElementDSL.module(module: Module) = column(size(h = Animatable(from = 32.px, to = Bounding))) {
        // used to lazily load setting elements, as they're not visible until clicked and most of them go unseen
        var loaded = false
        val color = Color.Animated(from = `gray 26`, to = this@ClickGUI.color, swapIf = module.enabled)
        block(
            size(240.px, 32.px),
            color = color
        ) {
            hoverInfo(description = module.description)
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
            AnimationOperation(Animation(duration, animation).onFinish {
                UIScreen.closeAnimHandler = null
            }) {
                element.alpha = 1f - it
                element.scale = 1f - it
            }.add()
        }
    }

    fun ElementDSL.hoverInfo(description: String) {
        if (description.isEmpty()) return

        var popup: Popup? = null
        onHover(1.seconds) {
            if (popup != null) return@onHover
            val x = if (element.x >= ui.main.width / 2f) (element.x - 5).px.alignRight else (element.x + element.width + 5).px
            val y = (element.y + 5).px
            popup = popup(at(x, y)) {
                block(
                    constraints = constrain(0.px, 0.px, Bounding + 10.px, 30.px),
                    color = `gray 38`,
                    radius = 5.radii()
                ) {
                    outline(this@ClickGUI.color, 2.px)
                    text(text = description)
                }
                element.alphaAnim = Animatable(0.px, 1.px).apply { animate(0.25.seconds) }
            }
        }
        onMouseExit {
            popup?.let {
                it.element.alphaAnim?.animate(0.25.seconds, Animations.Linear)?.onFinish {
                    it.closePopup()
                    popup = null
                }
            }
        }
    }

    fun ElementDSL.onHover(duration: Float, block: () -> Unit) {
        onMouseEnter {
            val start = System.nanoTime()
            operation {
                if (System.nanoTime() - start >= duration) {
                    block()
                    return@operation true
                }
                !element.isInside(ui.mx, ui.my) || !element.renders
            }
        }
    }

    // for ui branch warning

    private fun ElementDSL.uiBranchWarning() {
        popup(copies()) {
            column(size(Bounding + 50.px, Bounding + 10.px), padding = 5.px) {
                // background
                block(copies(), color = `gray 38`, radius = 10.radii()).outline(this@ClickGUI.color, thickness = 3.px)

                divider(15.px)
                text("WARNING", size = 30.px)

                text("You are using an extremely unstable branch that shouldn't be unintentionally used.", size = 20.px)
                text("If you downloaded this from GitHub Actions, ensure you don't download from the ui branch accidentally.", size = 20.px)

                divider(10.px)
                block(size(Bounding + 30.px, Bounding + 10.px), this@ClickGUI.color, radius = 5.radii()) {
                    text("I understand", size = 25.px)
                    hoverEffect(0.1.seconds)
                    onClick {
                        closePopup(smooth = true)
                        warned = true; true
                    }
                }
            }
            onMouseEnter { /* silly way to artificially stop inputs */ }
        }
    }

    data class PanelData(var x: Float, var y: Float, var extended: Boolean) {
        val defaultX = x
        val defaultY = y
        val defaultExtended = extended
    }
}