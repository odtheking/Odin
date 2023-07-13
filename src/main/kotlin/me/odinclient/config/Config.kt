package me.odinclient.config

import cc.polyfrost.oneconfig.config.core.OneColor
import com.google.gson.GsonBuilder
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import me.odinclient.config.jsonutils.SettingDeserializer
import me.odinclient.config.jsonutils.SettingSerializer
import me.odinclient.features.ConfigModule
import me.odinclient.features.ModuleManager
import me.odinclient.features.settings.Setting
import me.odinclient.clickgui.ClickGUI
import me.odinclient.features.settings.impl.*
import java.io.File
import java.io.IOException

class Config(path: File) {

    private val gson = GsonBuilder()
        .registerTypeAdapter(object : TypeToken<Setting<*>>(){}.type, SettingSerializer())
        .registerTypeAdapter(object : TypeToken<Setting<*>>(){}.type, SettingDeserializer())
        .excludeFieldsWithoutExposeAnnotation()
        .setPrettyPrinting().create()


    private val configFile = File(path, "forknifeConfig.json")

    init {
        try {
            if (!path.exists()) {
                path.mkdirs()
            }
            configFile.createNewFile()
        } catch (e: Exception) {
            println("Error initializing module config")
        }
    }

    fun loadConfig() {
        try {
            val configModules: ArrayList<ConfigModule>
            with(configFile.bufferedReader().use { it.readText() }) {
                if (this == "") {
                    return
                }
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

                        /** This is so spells in spell macro can properly save */
                        /*if (module === SpellMacro) {
                            if (configSetting.name.startsWith("Spell") && !module.getNameFromSettings(configSetting.name))
                                module.addSettings(SelectorSetting(configSetting.name, "R-R-R", arrayListOf("R-R-R", "R-L-R", "R-L-L", "R-R-L", "L-L-L", "L-R-L", "L-R-R", "L-L-R")))
                        }*/

                        val setting = module.getSettingByName(configSetting.name) ?: continue
                        when (setting) {
                            is BooleanSetting -> setting.enabled = (configSetting as BooleanSetting).enabled
                            is NumberSetting -> setting.value = (configSetting as NumberSetting).value
                            is ColorSetting -> setting.value = OneColor((configSetting as NumberSetting).value.toInt())
                            is SelectorSetting -> setting.selected = (configSetting as StringSetting).text
                            is StringSetting -> setting.text = (configSetting as StringSetting).text
                        }
                    }
                }
            }

            ClickGUI.panels.forEach { panel ->
                panel.moduleButtons.forEach {
                    it.updateElements()
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