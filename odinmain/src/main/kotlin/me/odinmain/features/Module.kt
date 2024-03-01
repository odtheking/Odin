package me.odinmain.features

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

import kotlin.reflect.full.hasAnnotation

/**
 * Class that represents a module. And handles all the settings.
 * @author Aton
 */
abstract class Module(
    val name: String,
    val key: Int = Keyboard.KEY_NONE,
    @Transient val category: Category = Category.RENDER,
    @Transient var description: String = "",
    @Transient val tag: Int = TagType.NONE,
    toggled: Boolean = false,
) {

    var enabled: Boolean = toggled
        private set

    /**
     * Settings for the module
     */
    val settings: ArrayList<Setting<*>> = ArrayList()

    protected inline val mc get() = OdinMain.mc

    /**
     * Indicates if the module has the annotation [AlwaysActive],
     * which keeps the module registered to the eventbus, even if disabled
     */
    @Transient
    val alwaysActive = this::class.hasAnnotation<AlwaysActive>()

    init {
        if (alwaysActive) {
            MinecraftForge.EVENT_BUS.register(this)
        }
    }

    /**
     * Gets toggled when module is enabled
     */
    open fun onEnable() {
        if (!alwaysActive) {
            MinecraftForge.EVENT_BUS.register(this)
        }
    }

    /**
     * Gets toggled when module is disabled
     */
    open fun onDisable() {
        if (!alwaysActive) {
            MinecraftForge.EVENT_BUS.unregister(this)
        }
    }

    open fun onKeybind() {
        toggle()
        if (ClickGUIModule.enableNotification) {
            modMessage("$name ${if (enabled) "§aenabled" else "§cdisabled"}.")
        }
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
        for (i in setting) {
            register(i)
        }
    }

    operator fun <K : Setting<*>> K.unaryPlus(): K = register(this)

    fun getSettingByName(name: String): Setting<*>? {
        for (setting in settings) {
            if (setting.name.equals(name, ignoreCase = true)) {
                return setting
            }
        }
        return null
    }

    /**
     * Helper function to make cleaner code, and more performance, since we don't need multiple registers for packet received events.
     *
     * @param type The packet type to listen for.
     * @param shouldRun Get whether the function should run (Will in most cases be used with the "enabled" value)
     * @param func The function to run when the packet is received.
     */
    fun <T : Packet<*>> onPacket(type: Class<T>, shouldRun: () -> Boolean = { this::class.hasAnnotation<AlwaysActive>() || enabled }, func: (T) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        ModuleManager.packetFunctions.add(
            ModuleManager.PacketFunction(type, func, shouldRun) as ModuleManager.PacketFunction<Packet<*>>
        )
    }

    fun onMessage(filter: Regex, shouldRun: () -> Boolean = { this::class.hasAnnotation<AlwaysActive>() || enabled }, func: (String) -> Unit) {
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

    // todo change this to enum
    object TagType {
        const val NONE = 0
        const val NEW = 1
        const val RISKY = 2
        const val FPSTAX = 3
    }
}