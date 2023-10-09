package me.odinmain.config

import com.google.gson.GsonBuilder
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import me.odin.Odin.Companion.mc
import me.odin.features.ConfigModule
import me.odin.features.ModuleManager
import me.odin.features.settings.Setting
import me.odin.features.settings.impl.*
import me.odin.utils.render.Color
import me.odinmain.config.jsonutils.SettingDeserializer
import me.odinmain.config.jsonutils.SettingSerializer
import me.odinmain.features.settings.impl.*
import java.io.File
import java.io.IOException

/**
 * @author Stivais, Aton
 */
object Config {

    private val gson = GsonBuilder()
        .registerTypeAdapter(object : TypeToken<Setting<*>>(){}.type, SettingSerializer())
        .registerTypeAdapter(object : TypeToken<Setting<*>>(){}.type, SettingDeserializer())
        .excludeFieldsWithoutExposeAnnotation()
        .setPrettyPrinting().create()

    private val configFile = File(mc.mcDataDir, "config/odin/odin-config.json").apply {
        try {
            createNewFile()
        } catch (e: Exception) {
            println("Error initializing module config")
        }
    }

    fun loadConfig() {
        try {
            val configModules: ArrayList<ConfigModule>
            with(configFile.bufferedReader().use { it.readText() }) {
                if (this == "") return
                configModules = gson.fromJson(
                    this,
                    object : TypeToken<ArrayList<ConfigModule>>() {}.type
                )
            }
            configModules.forEach { configModule ->
                ModuleManager.getModuleByName(configModule.name).run updateModule@{
                    val module = this ?: return@updateModule
                    if (module.enabled != configModule.enabled) module.toggle()
                    module.keyCode = configModule.keyCode

                    for (configSetting in configModule.settings) {
                        @Suppress("SENSELESS_COMPARISON")
                        if (configSetting == null) continue

                        val setting = module.getSettingByName(configSetting.name)
                        if (setting == null) {
                            println("Setting ${configSetting.name} not found in module ${module.name}, if this is an ActionSetting, ignore this message.")
                            continue
                        }
                        when (setting) {
                            is BooleanSetting -> setting.enabled = (configSetting as BooleanSetting).enabled
                            is DualSetting -> setting.enabled = (configSetting as BooleanSetting).enabled
                            is NumberSetting -> setting.valueAsDouble = (configSetting as NumberSetting).valueAsDouble
                            is ColorSetting -> setting.value = Color((configSetting as NumberSetting).valueAsDouble.toInt())
                            is SelectorSetting -> setting.selected = (configSetting as StringSetting).text
                            is StringSetting -> setting.text = (configSetting as StringSetting).text
                        }
                    }
                }
            }
        } catch (e: JsonSyntaxException) {
            println("Error parsing config.")
            println(e.message)
            e.printStackTrace()
        } catch (e: JsonIOException) {
            println("Error reading config.")
        } catch (e: Exception) {
            println("Config Error.")
            println(e.message)
            e.printStackTrace()
        }
    }

    fun saveConfig() {
        try {
            configFile.bufferedWriter().use {
                it.write(gson.toJson(ModuleManager.modules))
            }
        } catch (e: IOException) {
            println("Error saving config.")
        }
    }
}