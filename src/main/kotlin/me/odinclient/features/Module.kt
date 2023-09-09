package me.odinclient.features

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import me.odinclient.OdinClient
import me.odinclient.features.ModuleManager.executors
import me.odinclient.features.impl.render.ClickGUIModule
import me.odinclient.features.settings.AlwaysActive
import me.odinclient.features.settings.Setting
import me.odinclient.features.settings.impl.HudSetting
import me.odinclient.utils.clock.Executable
import me.odinclient.utils.clock.Executor
import me.odinclient.utils.skyblock.ChatUtils
import net.minecraft.network.Packet
import net.minecraftforge.common.MinecraftForge
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import kotlin.reflect.full.hasAnnotation

/**
 * Class that represents a module. And handles all the settings.
 * @author Aton
 */
abstract class Module(
    name: String,
    keyCode: Int = Keyboard.KEY_NONE,
    category: Category = Category.RENDER,
    toggled: Boolean = false,
    settings: ArrayList<Setting<*>> = ArrayList(),
    description: String = "",
    val tag: Int = TagType.NONE
) {
    object TagType {
        const val NONE = 0
        const val NEW = 1
        const val RISKY = 2
        const val FPSTAX = 3
    }

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
        settings.find { it.name.equals(name, ignoreCase = true) }
        System.err.println("[" + OdinClient.NAME + "] Error Setting NOT found: '" + name + "'!")
        return null
    }

    internal fun isKeybindDown(): Boolean {
        return keyCode != 0 && (Keyboard.isKeyDown(keyCode) || Mouse.isButtonDown(keyCode + 100))
    }

    /**
     * Helper function to make cleaner code, and more performance, since we don't need multiple registers for packet received events.
     *
     * @param type The packet type to listen for.
     * @param shouldRun Get whether the function should run (Will in most cases be used with the "enabled" value)
     * @param func The function to run when the packet is received.
     */
    fun <T : Packet<*>> onPacket(type: Class<T>, shouldRun: () -> Boolean = { enabled }, func: (T) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        ModuleManager.packetFunctions.add(
            ModuleManager.PacketFunction(type, func, shouldRun) as ModuleManager.PacketFunction<Packet<*>>
        )
    }

    fun onMessage(filter: Regex, shouldRun: () -> Boolean = { enabled }, func: (String) -> Unit) {
        ModuleManager.messageFunctions.add(ModuleManager.MessageFunction(filter, func))
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
}