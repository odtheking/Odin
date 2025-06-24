package me.odinmain.features.impl.render

import com.github.stivais.aurora.animations.Animation
import com.github.stivais.aurora.color.Color
import com.github.stivais.aurora.components.Component
import com.github.stivais.aurora.components.impl.Layout
import com.github.stivais.aurora.components.impl.dropShadow
import com.github.stivais.aurora.components.impl.scroll
import com.github.stivais.aurora.components.scope.ContainerScope
import com.github.stivais.aurora.dsl.*
import com.github.stivais.aurora.effects.AuroraEffect
import com.github.stivais.aurora.input.Keys
import com.github.stivais.aurora.measurements.impl.Sum
import com.github.stivais.aurora.renderer.Renderer
import com.github.stivais.aurora.renderer.data.Radius.Companion.radius
import com.github.stivais.aurora.utils.Timing.Companion.seconds
import com.github.stivais.aurora.utils.color
import com.github.stivais.aurora.utils.multiply
import com.github.stivais.aurora.utils.withAlpha
import me.odinmain.OdinMain
import me.odinmain.OdinMain.aurora
import me.odinmain.aurora.components.draggable
import me.odinmain.aurora.screens.AuroraOverlay
import me.odinmain.config.Config
import me.odinmain.features.AlwaysActive
import me.odinmain.features.Category
import me.odinmain.features.Module
import me.odinmain.features.ModuleManager.modules
import me.odinmain.features.settings.RepresentableSetting
import me.odinmain.features.settings.Setting.Companion.withDependency
import me.odinmain.features.settings.impl.*
import me.odinmain.utils.lateinit
import me.odinmain.utils.skyblock.LocationUtils
import me.odinmain.utils.skyblock.createClickStyle
import me.odinmain.utils.skyblock.getChatBreak
import me.odinmain.utils.skyblock.modMessage
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
    val updateMessage by SelectorSetting("Update Message", arrayListOf("Full", "Beta", "None"), description = "")//.hide()

    // needs own module
    val devMessages by BooleanSetting("Dev Messages", true, description = "Enables dev messages in chat.").withDependency { DevPlayers.isDev } // make dev-specific modules and put this there
    val devSize by BooleanSetting("Dev Size", true, description = "Toggles client side dev size.").withDependency { DevPlayers.isDev }
    private val devWings by BooleanSetting("Dev Wings", false, description = "Toggles client side dev wings.").withDependency { DevPlayers.isDev }
    private val devWingsColor by ColorSetting("Dev Wings Color", Color.RGB(255, 255, 255), description = "Color of the dev wings.").withDependency { DevPlayers.isDev }
    private val devSizeX by NumberSetting("Dev Size X", 1f, -1f, 3f, 0.1, description = "X scale of the dev size.").withDependency { DevPlayers.isDev && devSize }
    private val devSizeY by NumberSetting("Dev Size Y", 1f, -1f, 3f, 0.1, description = "Y scale of the dev size.").withDependency { DevPlayers.isDev && devSize }
    private val devSizeZ by NumberSetting("Dev Size Z", 1f, -1f, 3f, 0.1, description = "Z scale of the dev size.").withDependency { DevPlayers.isDev && devSize }
    private val passcode by StringSetting("Passcode", "odin", description = "Passcode for dev features.").withDependency { DevPlayers.isDev }.censors()

    // TODO READD
//    val reset by ActionSetting("Send Dev Data") {
//        scope.launch {
//            modMessage(
//                sendDataToServer(
//                    body = "${mc.thePlayer.name}, [${devWingsColor.red},${devWingsColor.green},${devWingsColor.blue}], [$devSizeX,$devSizeY,$devSizeZ], $devWings, $passcode",
//                    "https://tj4yzotqjuanubvfcrfo7h5qlq0opcyk.lambda-url.eu-north-1.on.aws/"
//                )
//            )
//            DevPlayers.updateDevs()
//        }
//    }.withDependency { DevPlayers.isDev }

    // todo readd
//    val action by ActionSetting(
//        "Open HUD Editor",
//        description = "Opens the HUD Editor, allowing you to reposition HUDs"
//    ) {
//        open(HUDManager.makeHUDEditor())
//    }

    val panelSettings by MapSetting("panel.data", mutableMapOf<Category, PanelData>()).also { setting ->
        Category.entries.forEach {
            setting.value[it] = PanelData(x = 10f + 260f * it.ordinal, y = 10f, extended = true)
        }
    }

    private var joined by BooleanSetting("first.join", false, description = "").hide()
    var lastSeenVersion by StringSetting("last.seen.version", "1.0.0", description = "")//.hide()

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
        this.toggle()
    }

    override fun onEnable() {
        super.onEnable()
        toggle()
    }

    @JvmField
    val gray26: Color = Color.RGB(26, 26, 26)

    @JvmField
    val gray38: Color = Color.RGB(38, 38, 38)

    @JvmField
    val colorDarker: Color = color { color.rgba.multiply(0.9f) }

    /**
     * Creates the Click GUI using aurora.
     */
    fun clickGUI() = aurora {
        for (category in Category.entries) {
            val data = panelSettings[category] ?: throw NullPointerException()
            block(
                position = at(data.x.px, data.y.px),
                size = sum(),
                color = gray38.withAlpha(0.7f),
                radius = 15.radius()
            ) background@ {
                dropShadow(color = Color.BLACK.withAlpha(0.25f), blur = 10f, spread = 5f)
                column {
                    clipContents()

                    val animatable = animatable(from = Sum, to = 0.px)

                    block(
                        size = size(270.px, 50.px),
                        color = gray26,
                        radius = radius(tl = 15, tr = 15)
                    ) {
                        text(
                            string = category.displayName,
                            size = 50.percent
                        )
                        onClick(button = 1) {
                            animatable.animate(0.5.seconds, Animation.Style.EaseInOutQuint)
                            component.redraw()
                            true
                        }
                        draggable(moves = this@background.component)
                    }

                    val scrollable = scrollable(size = size(100.percent, Sum)) {
                        clipContents()
                        column(size = size(100.percent, animatable)) {
                            for (module in modules) {
                                if (module.category != category) continue
                                module(module)
                            }
                        }
                    }
                    onScroll { (amount) ->
                        scrollable.scroll(amount * 75f, 0.3.seconds, Animation.Style.EaseOutQuint)
                        true
                    }
                }
            }
        }

        // open and close animation
        var animation: Animation? = Animation(0.5.seconds, Animation.Style.EaseInOutQuint)
        var reverse = false

        effect(object : AuroraEffect {
            override fun preRender(component: Component, renderer: Renderer) {
                animation?.let { anim ->
                    val progress = if (reverse) 1f - anim.get() else anim.get()
                    renderer.globalAlpha(progress)
                    val x = component.x + component.width / 2f
                    val y = component.y + component.height / 2f
                    renderer.translate(x, y)
                    renderer.scale(progress, progress)
                    renderer.translate(-x, -y)
                    if (anim.finished) animation = null
                }
            }
        })

        onBind(Keys.ESCAPE) {
            reverse = true
            val overlay = AuroraOverlay(aurora)
            overlay.open()
            animation = Animation(0.5.seconds, Animation.Style.EaseInBack).onFinish {
                overlay.close()
            }
            false
        }

    }

    private fun ContainerScope<*>.module(module: Module) = column(size = size(100.percent)) {
        clipContents()
        // used to lazily load the components,
        // as most of the time you aren't seeing any a lot of them
        var loaded = false
        var column: ContainerScope<Layout> by lateinit()

        val color = Color.Animated(from = gray26, to = color, swapIf = module.enabled)
        val animatable = animatable(from = 0.px, to = Sum)

        block(
            size = size(100.percent, 40.px),
            color = color
        ) {
            text(
                string = module.name,
                size = 50.percent
            )
            onClick {
                color.animate(0.5.seconds, Animation.Style.EaseInOutQuint)
                module.toggle()
                true
            }
            onClick(button = 1) {
                if (!loaded) {
                    column.apply {
                        for (setting in module.settings) {
                            if (setting is RepresentableSetting) setting.visualize(this)
                        }
                    }
                    loaded = true
                }
                animatable.animate(0.5.seconds, Animation.Style.EaseInOutQuint)
                component.redraw()
                true
            }
        }

        column = column(size = size(100.percent, animatable), gap = Layout.Gap.Static(16.px)) {
            padding(horizontal = 12f, vertical = 10f)
        }
    }

//    fun ComponentScope<*>.hoverInformation(description: String) {
//        if (description.isEmpty()) return
//
//        val lines = arrayListOf<String>()
//        var lastIndex = 0
//        var spaces = 0
//        for (index in description.indices) {
//            if (description[index] == ' ') {
//                spaces++
//            }
//            if (spaces == 5 || index == description.lastIndex) {
//                lines.add(description.substring(lastIndex, index + 1))
//                lastIndex = index + 1
//                spaces = 0
//            }
//        }
//
//        var popup: Popup? = null
//
//        onHover(duration = 1.seconds) {
//            if (popup != null) return@onHover
//
//            val it = component
//            val x = if (it.x >= ui.main.width / 2f) (it.x - 10).px.alignRight else (it.x + it.width + 10).px
//            val y = (it.y + 5).px
//
//            popup = popup(constrain(x, y, Bounding, Bounding), smooth = true) {
//                block(
//                    constraints = bounds(padding = 7.5.px),
//                    color = `gray 38`,
//                    radius = 5.radius()
//                ) {
//                    outline(this@ClickGUI.color, 2.px)
//                    column {
//                        lines.loop {
//                            text(
//                                string = it,
//                                size = 15.px
//                            )
//                        }
//                    }
//                }
//            }
//            onMouseExit {
//                popup?.let {
//                    it.closePopup()
//                    popup = nullV
//                }
//            }
//        }
//    }

    data class PanelData(var x: Float, var y: Float, var extended: Boolean) {
        val defaultX = x
        val defaultY = y
    }
}
