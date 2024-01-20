package me.odinmain.features

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import me.odinmain.OdinMain
import me.odinmain.features.ModuleManager.executors
import me.odinmain.features.impl.render.ClickGUIModule
import me.odinmain.features.settings.AlwaysActive
import me.odinmain.features.settings.Setting
import me.odinmain.features.settings.impl.HudSetting
import me.odinmain.utils.clock.Executable
import me.odinmain.utils.clock.Executor
import me.odinmain.utils.skyblock.modMessage
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

    var description: String

    protected inline val mc get() = OdinMain.mc

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
        if (ClickGUIModule.enableNotification) modMessage("$name ${if (enabled) "§aenabled" else "§cdisabled"}.")
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
        return null
    }

    fun isKeybindDown(): Boolean {
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
        ModuleManager.messageFunctions.add(ModuleManager.MessageFunction(filter, shouldRun, func))
    }

    fun onWorldLoad(func: () -> Unit) {
        ModuleManager.worldLoadFunctions.add(func)
    }

    fun execute(delay: Long, profileName: String = this.name, func: Executable) {
        executors.add(this to Executor(delay, profileName, func))
    }

    fun execute(delay: Long, repeats: Int, profileName: String = this.name, func:Executable) {
        executors.add(this to Executor.LimitedExecutor(delay, repeats, profileName, func))
    }

    fun execute(delay: () -> Long, profileName: String = this.name, func: Executable) {
        executors.add(this to Executor(delay, profileName, func))
    }
}