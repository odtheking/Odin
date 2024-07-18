package me.odinmain.features

import me.odinmain.OdinMain
import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.features.ModuleManager.executors
import me.odinmain.features.impl.render.ClickGUI
import me.odinmain.features.settings.AlwaysActive
import me.odinmain.features.settings.Setting
import me.odinmain.features.settings.impl.Keybinding
import me.odinmain.utils.clock.Executable
import me.odinmain.utils.clock.Executor
import me.odinmain.utils.registerAndCatch
import me.odinmain.utils.skyblock.modMessage
import net.minecraft.network.Packet
import org.lwjgl.input.Keyboard
import kotlin.reflect.full.hasAnnotation

/**
 * Class that represents a module. And handles all the settings.
 * @author Aton
 */
abstract class Module(
    val name: String,
    key: Int? = Keyboard.KEY_NONE,
    @Transient val category: Category = Category.RENDER,
    @Transient var description: String = "",
    @Transient val tag: TagType = TagType.NONE,
    toggled: Boolean = false,
) {

    var enabled: Boolean = toggled
        private set

    /**
     * Settings for the module
     */
    val settings: ArrayList<Setting<*>> = ArrayList()

    /**
     * Main keybinding of the module
     */
    val keybinding: Keybinding? = key?.let { Keybinding(it).apply { onPress = ::onKeybind } }

    protected inline val mc get() = OdinMain.mc

    /**
     * Indicates if the module has the annotation [AlwaysActive],
     * which keeps the module registered to the eventbus, even if disabled
     */
    @Transient
    val alwaysActive = this::class.hasAnnotation<AlwaysActive>()

    init {
        if (alwaysActive) this.registerAndCatch()
    }

    /**
     * Gets toggled when module is enabled
     */
    open fun onEnable() {
        if (!alwaysActive) this.registerAndCatch()
    }

    /**
     * Gets toggled when module is disabled
     */
    open fun onDisable() {
        if (!alwaysActive) this.registerAndCatch()
    }

    open fun onKeybind() {
        toggle()
        if (ClickGUI.enableNotification) {
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
        /*if (setting is HudSetting) {
            setting.value.init(this) TODO: Fix this when we have a proper HUD handling
        }*/
        return setting
    }

    fun register(vararg setting: Setting<*>) {
        for (i in setting) {
            register(i)
        }
    }

    operator fun <K : Setting<*>> K.unaryPlus(): K = register(this)

    fun getSettingByName(name: String?): Setting<*>? {
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
    fun <T : Packet<*>> onPacket(type: Class<T>, shouldRun: () -> Boolean = { alwaysActive || enabled }, func: (T) -> Unit) {
        @Suppress("UNCHECKED_CAST")
        ModuleManager.packetFunctions.add(
            ModuleManager.PacketFunction(type, func, shouldRun) as ModuleManager.PacketFunction<Packet<*>>
        )
    }

    /**
     * Runs the given function when a Chat Packet is sent with a message that matches the given regex filter.
     *
     * @param filter The regex the message should match
     * @param shouldRun Boolean getter to decide if the function should run at any given time, could check if the option is enabled for instance.
     * @param func The function to run if the message matches the given regex and shouldRun returns true.
     *
     * @author Bonsai
     */
    fun onMessage(filter: Regex, shouldRun: () -> Boolean = { alwaysActive || enabled }, func: (String) -> Unit) {
        ModuleManager.messageFunctions.add(ModuleManager.MessageFunction(filter, shouldRun, func))
    }

    /**
     * Runs the given function when a Chat Packet is sent with the same message as the given text (or contains the given text) (Case Sensitive!)
     *
     * @param text The text to look for.
     * @param contains If the function should run when the message only contains the text but does not necessarily equal it.
     * @param shouldRun Boolean getter to decide if the function should run at any given time, could check if the option is enabled for instance.
     * @param func The function to run if the message matches or contains the given text and shouldRun returns true.
     *
     * @author Bonsai
     */
    fun onMessage(text: String, contains: Boolean, shouldRun: () -> Boolean = { alwaysActive || enabled }, func: (String) -> Unit) {
        val regex =
            if (contains)
                ".*${Regex.escape(text)}.*".toRegex()
            else
                Regex.escape(text).toRegex()

        ModuleManager.messageFunctions.add(ModuleManager.MessageFunction(regex, shouldRun, func))
    }

    fun onMessageCancellable(filter: Regex, shouldRun: () -> Boolean = { alwaysActive || enabled }, func: (ChatPacketEvent) -> Unit) {
        ModuleManager.cancellableMessageFunctions.add(ModuleManager.MessageFunctionCancellable(filter, shouldRun, func))
    }

    fun onWorldLoad(func: () -> Unit) {
        ModuleManager.worldLoadFunctions.add(func)
    }

    fun execute(delay: Long, profileName: String = this.name, func: Executable) {
        executors.add(this to Executor(delay, profileName, func))
    }

    fun execute(delay: Long, repeats: Int, profileName: String = this.name, func: Executable) {
        executors.add(this to Executor.LimitedExecutor(delay, repeats, profileName, func))
    }

    fun execute(delay: () -> Long, profileName: String = this.name, func: Executable) {
        executors.add(this to Executor(delay, profileName, func))
    }

    enum class TagType {
        NONE, NEW, RISKY, FPSTAX
    }
}