package me.odinclient.features

import me.odinclient.features.settings.Setting
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import me.odinclient.features.settings.AlwaysActive
import me.odinclient.OdinClient
import me.odinclient.utils.skyblock.ChatUtils
import net.minecraftforge.common.MinecraftForge
import org.lwjgl.input.Keyboard
import java.util.ArrayList
import kotlin.reflect.full.hasAnnotation

abstract class Module(
    name: String,
    keyCode: Int = Keyboard.KEY_NONE,
    category: Category = Category.GENERAL,
    toggled: Boolean = false,
    settings: ArrayList<Setting<*>> = ArrayList(),
    description: String = ""
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
     * Will be used for an advanced info gui
     */
    var description: String

    init {
        this.name = name
        this.keyCode = keyCode
        this.category = category
        this.settings = settings
        this.description = description
    }

    fun initializeModule() {
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

    /**
     * Call to perform the key bind action for this module.
     * By default, this will toggle the module and send a chat message.
     * It can be overwritten in the module to change that behaviour.
     */
    open fun keyBind() {
        this.toggle()
        ChatUtils.modMessage("$name ${if (enabled) "§aenabled" else "§cdisabled"}.")
    }

    /**
     * Will toggle the module
     */
    fun toggle() {
        enabled = !enabled
        if (enabled)
            onEnable()
        else
            onDisable()
    }

    /**
     * Adds all settings in the input to the settings field of the module.
     * This is required for saving and loading these settings to / from a file.
     * Keep in mind, that these settings are passed by reference, which will get lost if the original setting is reassigned.
     */
    fun addSettings(setArray: ArrayList<Setting<*>>) {
        setArray.forEach {
            settings.add(it)
        }
    }

    /**
     * Adds all settings in the input to the settings field of the module.
     * This is required for saving and loading these settings to / from a file.
     * Keep in mind, that these settings are passed by reference, which will get lost if the original setting is reassigned.
     */
    fun addSettings(vararg setArray: Setting<*>) {
        this.addSettings(ArrayList(setArray.asList()))
    }

    fun <K: Setting<*>> register(setting: K): K {
        addSettings(setting)
        return setting
    }

    /**
     * Overloads the unaryPlus operator for [Setting] classes to register them to the module.
     * The following is an example of how it can be used to define a setting for a module.
     *
     *     private val distance = +NumberSetting("Distance", 4.0, 1.0,10.0,0.1)
     * @see register
     */
    operator fun <K: Setting<*>> K.unaryPlus(): K = register(this)

    fun getSettingByName(name: String): Setting<*>? {
        for (set in settings) {
            if (set.name.equals(name, ignoreCase = true)) {
                return set
            }
        }
        System.err.println("[" + OdinClient.NAME + "] Error Setting NOT found: '" + name + "'!")
        return null
    }

    fun getNameFromSettings(name: String): Boolean {
        for (set in settings) {
            if (set.name == name) return true
        }
        return false
    }
}