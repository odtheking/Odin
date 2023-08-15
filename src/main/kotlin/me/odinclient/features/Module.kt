package me.odinclient.features

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import me.odinclient.OdinClient
import me.odinclient.features.impl.general.ClickGUIModule
import me.odinclient.features.settings.AlwaysActive
import me.odinclient.features.settings.Hud
import me.odinclient.features.settings.Setting
import me.odinclient.features.settings.impl.HudSetting
import me.odinclient.ui.hud.HudElement
import me.odinclient.utils.clock.Executable
import me.odinclient.utils.clock.Executor
import me.odinclient.utils.clock.Executor.Companion.executeAll
import me.odinclient.utils.skyblock.ChatUtils
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation

/**
 * Class that represents a module. And handles all the settings.
 * @author Aton
 */
abstract class Module(
    name: String,
    keyCode: Int = Keyboard.KEY_NONE,
    category: Category = Category.GENERAL,
    toggled: Boolean = false,
    settings: ArrayList<Setting<*>> = ArrayList(),
    description: String = "",
    val risky: Boolean = false,
    val fpsHeavy: Boolean = false
) {

    @Expose
    @SerializedName("name")
    val name: String

    @Expose
    @SerializedName("key")
    var keyCode: Int
    val category: Category

    @Expose
    @SerializedName("enabled")
    var enabled: Boolean = toggled
        private set

    @Expose
    @SerializedName("settings")
    val settings: ArrayList<Setting<*>>

    /**
     * Will be used for a tooltip
     */
    var description: String

    init {
        this.name = name
        this.keyCode = keyCode
        this.category = category
        this.settings = settings
        this.description = description

        if (this::class.hasAnnotation<AlwaysActive>()) {
            MinecraftForge.EVENT_BUS.register(this)
        }

        /**
         * A little bit scuffed but ig it works.
         */
        this::class.nestedClasses
            .mapNotNull { it.objectInstance }
            .filterIsInstance<HudElement>()
            .forEach { hudElement ->
                val hudset = hudElement::class.findAnnotation<Hud>() ?: return@forEach
                register(HudSetting(hudset.name, hudElement, hudset.toggleable, hidden = hudset.hidden))
            }
    }

    open fun onEnable() {
        MinecraftForge.EVENT_BUS.register(this)
    }

    open fun onDisable() {
        if (!this::class.hasAnnotation<AlwaysActive>()) {
            MinecraftForge.EVENT_BUS.unregister(this)
        }
    }

    open fun onKeybind() {
        toggle()
        if (ClickGUIModule.enableNotification) ChatUtils.modMessage("$name ${if (enabled) "§aenabled" else "§cdisabled"}.")
    }

    fun toggle() {
        enabled = !enabled
        if (enabled) onEnable()
        else onDisable()
    }

    fun <K : Setting<*>> register(setting: K): K {
        settings.add(setting)
        if (setting is HudSetting) {
            setting.value.init(this)
        }
        return setting
    }

    fun register(vararg setting: Setting<*>) {
        settings.addAll(setting)
    }

    operator fun <K : Setting<*>> K.unaryPlus(): K = register(this)

    fun getSettingByName(name: String): Setting<*>? {
        for (set in settings) {
            if (set.name.equals(name, ignoreCase = true)) {
                return set
            }
        }
        System.err.println("[" + OdinClient.NAME + "] Error Setting NOT found: '" + name + "'!")
        return null
    }

    internal fun isKeybindDown(): Boolean {
        return keyCode != 0 && (Keyboard.isKeyDown(keyCode) || Mouse.isButtonDown(keyCode + 100))
    }

    fun execute(delay: Long, func: Executable) {
        executors.add(Executor(delay, func))
    }

    fun execute(delay: Long, repeats: Int, func: Executable) {
        executors.add(Executor.LimitedExecutor(delay, repeats, func))
    }

    fun execute(delay: () -> Long, func: Executable) {
        executors.add(Executor.VaryingExecutor(delay, func))
    }

    private val executors = ArrayList<Executor>()

    @SubscribeEvent
    fun onRender(event: RenderWorldLastEvent) {
        executors.executeAll()
    }

    // TODO: Do this and a vararg instead to make it cleaner.
    enum class Tags {
        Bannable, FpsHeavy
    }
}