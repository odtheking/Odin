package me.odinmain.config

import com.google.gson.GsonBuilder
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import me.odinmain.OdinMain.mc
import me.odinmain.config.utils.SettingDeserializer
import me.odinmain.config.utils.SettingSerializer
import me.odinmain.features.ConfigModule
import me.odinmain.features.ModuleManager
import me.odinmain.features.settings.Setting
import java.io.File
import java.io.IOException

/**
 * @author Stivais, Aton
 */
object Config {

    private val gson = GsonBuilder()
        .registerTypeAdapter(object : TypeToken<Setting<*>>(){}.type, SettingSerializer())
        .registerTypeAdapter(object : TypeToken<Setting<*>>(){}.type, SettingDeserializer())
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
                configModules = gson.fromJson(this, object : TypeToken<ArrayList<ConfigModule>>() {}.type)
            }
            for (configModule in configModules) {
                val module = ModuleManager.getModuleByName(configModule.name) ?: continue
                if (module.enabled != configModule.enabled) module.toggle()

                for (configSetting in configModule.settings) {
                    @Suppress("SENSELESS_COMPARISON")
                    if (configSetting == null) continue
                    val setting = module.getSettingByName(configSetting.name) ?: continue
                    setting.update(configSetting)
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