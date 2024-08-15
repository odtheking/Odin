package me.odinmain.features

import com.github.stivais.ui.constraints.Constraints
import com.github.stivais.ui.constraints.measurements.Percent
import com.github.stivais.ui.elements.Element
import com.github.stivais.ui.elements.scope.ElementScope
import me.odinmain.OdinMain
import me.odinmain.events.impl.ChatPacketEvent
import me.odinmain.features.ModuleManager.executors
import me.odinmain.features.ModuleManager.setupHUD
import me.odinmain.features.impl.render.ClickGUI
import me.odinmain.features.settings.AlwaysActive
import me.odinmain.features.settings.Setting
import me.odinmain.features.settings.impl.HUDSetting
import me.odinmain.features.settings.impl.Keybinding
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
    key: Int? = Keyboard.KEY_NONE,
    @Transient var description: String = "",
    @Transient val tag: TagType = TagType.NONE,
    toggled: Boolean = false,
) {
    /**
     * Category for this module.
     *
     * It is defined by the package of the module. (For example: me.odin.features.impl.render == [Category.RENDER]).
     * If it is in an invalid package, it will use [Category.RENDER] as a default
     */
    @Transient
    val category: Category = getCategory(this::class.java) ?: Category.RENDER

    /**
     * Reference for if the module is enabled
     *
     * When it is enabled, it is registered to the Forge Eventbus,
     * otherwise it's unregistered unless it has the annotation [@AlwaysActive][AlwaysActive]
     */
    var enabled: Boolean = toggled
        private set

    /**
     * List of settings for the module
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
        if (alwaysActive) {
            @Suppress("LeakingThis")
            MinecraftForge.EVENT_BUS.register(this)
        }
    }

    /**
     * Gets toggled when module is enabled
     */
    open fun onEnable() {
        if (!alwaysActive) MinecraftForge.EVENT_BUS.register(this)
    }

    /**
     * Gets toggled when module is disabled
     */
    open fun onDisable() {
        if (!alwaysActive) MinecraftForge.EVENT_BUS.unregister(this)
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

    fun HUD.setting(name: String, desciption: String): HUDSetting {
        return HUDSetting(name, this, desciption)
    }


    // this is unused
    @Deprecated("remove")
    enum class TagType {
        NONE, NEW, RISKY, FPSTAX
    }

    private companion object {
        private fun getCategory(clazz: Class<out Module>): Category? {
            val `package` = clazz.`package`.name
            return when {
                `package`.contains("dungeon") -> Category.DUNGEON
                `package`.contains("floor7") -> Category.FLOOR7
                `package`.contains("render") -> Category.RENDER
                `package`.contains("skyblock") -> Category.SKYBLOCK
                `package`.contains("nether") -> Category.NETHER
                else -> null
            }
        }
    }

    inner class HUD(
        val x: Percent,
        val y: Percent,
        var enabled: Boolean = true,
        val builder: HUDScope.() -> Unit
    ) {
        val defaultX: Float = x.percent
        val defaultY: Float = y.percent

        var scale = 1f
            set(value) {
                field = value.coerceAtLeast(0.25f)
            }

        init {
            ModuleManager.HUDs.add(this)
            setupHUD(this)
        }

        inner class Drawable(constraints: Constraints, val preview: Boolean) : Element(constraints) {

            override var enabled: Boolean = true
                get() = field && this@HUD.enabled && this@Module.enabled

            init {
                scaledCentered = false
                if (!preview) {
                    scale = this@HUD.scale
                }
            }
            override fun draw() {
                if (!preview) {
                    scale = this@HUD.scale
                }
            }
            override fun onElementAdded(element: Element) {
            }
        }
    }

    class HUDScope(element: HUD.Drawable) : ElementScope<HUD.Drawable>(element) {
        inline fun needs(crossinline block: () -> Boolean) {
            if (!element.preview) {
                operation {
                    element.enabled = block()
                    false
                }
            }
        }
    }
}