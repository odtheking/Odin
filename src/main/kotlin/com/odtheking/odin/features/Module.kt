package com.odtheking.odin.features

import com.odtheking.odin.OdinMod
import com.odtheking.odin.clickgui.settings.AlwaysActive
import com.odtheking.odin.clickgui.settings.DevModule
import com.odtheking.odin.clickgui.settings.Setting
import com.odtheking.odin.clickgui.settings.impl.HUDSetting
import com.odtheking.odin.events.core.EventBus
import com.odtheking.odin.features.impl.render.ClickGUIModule
import com.odtheking.odin.utils.modMessage
import net.minecraft.client.gui.GuiGraphics
import org.lwjgl.glfw.GLFW
import kotlin.reflect.full.hasAnnotation

/**
 * Class that represents a module. And handles all the settings.
 * @author Aton
 */
abstract class Module(
    val name: String,
    val key: Int? = GLFW.GLFW_KEY_UNKNOWN,
    category: Category? = null,
    @Transient var description: String,
    toggled: Boolean = false,
) {

    /**
     * Map containing all settings for the module,
     * where the key is the name of the setting.
     *
     * Since the map is a [LinkedHashMap], order is preserved.
     */
    val settings: LinkedHashMap<String, Setting<*>> = linkedMapOf()

    /**
     * Category for this module.
     */
    @Transient
    val category: Category = category ?: getCategoryFromPackage(this::class.java)

    /**
     * Flag for if the module is enabled/disabled.
     *
     * When true, it is registered to the [EventBus].
     * When false, it is unregistered, unless the module has the [AlwaysActive] annotation.
     */
    var enabled: Boolean = toggled
        private set

    protected inline val mc get() = OdinMod.mc

    /**
     * Indicates if the module has the annotation [AlwaysActive],
     * which keeps the module registered to the eventbus, even if disabled
     */
    @Transient
    val alwaysActive = this::class.hasAnnotation<AlwaysActive>()

    @Transient
    val isDevModule = this::class.hasAnnotation<DevModule>()

    init {
        if (alwaysActive) {
            @Suppress("LeakingThis")
            EventBus.subscribe(this)
        }
    }

    /**
     * Invoked when module is enabled.
     *
     * It is recommended to call super so it can properly subscribe to the eventbus
     */
    open fun onEnable() {
        if (!alwaysActive) EventBus.subscribe(this)
    }

    /**
     * Invoked when module is disabled.
     *
     * It is recommended to call super so it can properly subscribe to the eventbus
     */
    open fun onDisable() {
        if (!alwaysActive) EventBus.unsubscribe(this)
    }

    /**
     * Invoked when the main keybind is pressed.
     *
     * By default, it toggles the module.
     */
    open fun onKeybind() {
        toggle()
        if (ClickGUIModule.enableNotification) modMessage("$name ${if (enabled) "§aenabled" else "§cdisabled"}.")
    }

    /**
     * Toggles the module and invokes [onEnable]/[onDisable].
     */
    fun toggle() {
        enabled = !enabled
        if (enabled) onEnable()
        else onDisable()
    }

    /**
     * Registers a [Setting] to this module and returns itself.
     */
    fun <K : Setting<*>> registerSetting(setting: K): K {
        settings[setting.name] = setting
        return setting
    }

    operator fun <K : Setting<*>> K.unaryPlus(): K = registerSetting(this)

    @Suppress("FunctionName")
    fun HUD(
        name: String,
        desc: String,
        toggleable: Boolean = true,
        x: Int = 10,
        y: Int = 10,
        scale: Float = 2f,
        block: GuiGraphics.(example: Boolean) -> Pair<Int, Int>
    ): HUDSetting = HUDSetting(name, x, y, scale, toggleable, desc, this, block)

    private companion object {
        private fun getCategoryFromPackage(clazz: Class<out Module>): Category {
            val packageName = clazz.packageName
            return when {
                packageName.contains("dungeon") -> Category.DUNGEON
                packageName.contains("boss") -> Category.BOSS
                packageName.contains("nether") -> Category.NETHER
                packageName.contains("render") -> Category.RENDER
                packageName.contains("skyblock") -> Category.SKYBLOCK
                else -> throw IllegalStateException(
                    "Module ${clazz.name} failed to get category from the package it is in." +
                            "Either manually assign a category," +
                            " or put it under any valid package (dungeon, floor7, nether, render, skyblock))"
                )
            }
        }
    }
}