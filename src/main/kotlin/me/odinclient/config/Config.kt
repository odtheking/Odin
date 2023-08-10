package me.odinclient.config

import com.google.gson.GsonBuilder
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import me.odinclient.OdinClient.Companion.mc
import me.odinclient.config.jsonutils.SettingDeserializer
import me.odinclient.config.jsonutils.SettingSerializer
import me.odinclient.features.ConfigModule
import me.odinclient.features.ModuleManager
import me.odinclient.features.settings.Setting
import me.odinclient.features.settings.impl.*
import me.odinclient.utils.render.Color
import java.io.File
import java.io.IOException

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