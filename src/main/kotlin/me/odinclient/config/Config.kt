package me.odinclient.config

import com.google.gson.GsonBuilder
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.config.jsonutils.SettingDeserializer
import me.odinclient.config.jsonutils.SettingSerializer
import me.odinclient.features.ConfigModule
import me.odinclient.features.Module
import me.odinclient.features.ModuleManager
import me.odinclient.features.ModuleManager.getModuleByName
import me.odinclient.features.settings.Setting
import me.odinclient.features.settings.impl.*
import me.odinclient.utils.render.Color
import java.io.File
import java.io.IOException

/**
 * @author Stivais, Aton
 */
@Suppress("NOTHING_TO_INLINE", "SENSELESS_COMPARISON")
object Config {

    private val gson = GsonBuilder()
        .registerTypeAdapter(object : TypeToken<Setting<*>>(){}.type, SettingSerializer())
        .registerTypeAdapter(object : TypeToken<Setting<*>>(){}.type, SettingDeserializer())
        .excludeFieldsWithoutExposeAnnotation()
        .setPrettyPrinting().create()

    private val configFile = File(mc.mcDataDir, "config/odin/odin-config.json").apply {
        try {
            createNewFile()
        } catch (e: IOException) {
            println("Error creating module config.\n${e.message}")
            e.printStackTrace()
        }
    }

    fun loadConfig() {
        try {
            with(configFile.bufferedReader().use { it.readText() }) {
                if (this == "") return
                handleModules(gson.fromJson(this, object : TypeToken<ArrayList<ConfigModule>>() {}.type))
            }
        } catch (e: JsonSyntaxException) {
            println("Error parsing config.\n${e.message}")
        } catch (e: JsonIOException) {
            println("Error reading config.\n${e.message}")
        } catch (e: Exception) {
            println("Config Error.\n${e.message}")
        }
    }

    // inline because it is used only in load config.
    private inline fun handleModules(modules: ArrayList<ConfigModule>) {
        modules.forEach { cfg ->
            getModuleByName(cfg.name)?.let {
                if (it.enabled != cfg.enabled) it.toggle()
                it.keyCode = cfg.keyCode
                handleSettings(it)
            }
        }
    }

    private inline fun handleSettings(module: Module) {
        module.settings.forEach { cfg ->
            if (cfg == null) return@forEach

            module.getSettingByName(cfg.name)?.let {
                when (it) {
                    is BooleanSetting -> it.enabled = (cfg as BooleanSetting).enabled
                    is DualSetting -> it.enabled = (cfg as BooleanSetting).enabled
                    is NumberSetting -> it.valueAsDouble = (cfg as NumberSetting).valueAsDouble
                    is ColorSetting -> it.value = Color((cfg as NumberSetting).valueAsDouble.toInt())
                    is SelectorSetting -> it.selected = (cfg as StringSetting).text
                    is StringSetting -> it.text = (cfg as StringSetting).text
                }
            } ?: print("Setting ${cfg.name} not found in module ${module.name}")
        }
    }

    fun saveConfig() {
        try {
            configFile.bufferedWriter().use {
                it.write(gson.toJson(ModuleManager.modules))
            }
        } catch (e: IOException) {
            println("Error saving config.\n${e.message}")
        }
    }
}
