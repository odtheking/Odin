package me.odinmain.config

import com.google.gson.*
import me.odinmain.OdinMain.logger
import me.odinmain.OdinMain.mc
import me.odinmain.features.ModuleManager
import me.odinmain.features.settings.Saving
import java.io.File

/**
 * This class handles loading and saving Modules and their settings.
 *
 * @author Stivais
 */
object Config {

    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val parser = JsonParser()

    private val configFile = File(mc.mcDataDir, "config/odin/odin-config.json").apply {
        try {
            createNewFile()
        } catch (e: Exception) {
            println("Error initializing module config\n${e.message}")
            logger.error("Error initializing module config", e)
        }
    }

    fun load() {
        try {
            with (configFile.bufferedReader().use { it.readText() }) {
                if (isEmpty()) return

                val jsonArray = parser.parse(this).asJsonArray ?: return
                for (modules in jsonArray) {
                    val moduleObj = modules?.asJsonObject ?: continue
                    val module = ModuleManager.getModuleByName(moduleObj.get("name").asString) ?: continue
                    if (moduleObj.get("enabled").asBoolean != module.enabled) module.toggle()

                    for (j in moduleObj.get("settings").asJsonArray) {
                        val settingObj = j?.asJsonObject?.entrySet() ?: continue
                        val setting = module.getSettingByName(settingObj.firstOrNull()?.key) ?: continue
                        if (setting is Saving) setting.read(settingObj.first().value)
                    }
                }
            }
        } catch (e: Exception) {
            println("Error loading config.\n${e.message}")
            logger.error("Error initializing module config", e)
        }
    }

    fun save() {
        try {
            // reason doing this is better is that
            // using like a custom serializer leaves 'null' in settings that don't save
            // code looks hideous tho, but it fully works
            val jsonArray = JsonArray().apply {
                for (module in ModuleManager.modules) {
                    add(JsonObject().apply {
                        add("name", JsonPrimitive(module.name))
                        add("enabled", JsonPrimitive(module.enabled))
                        add("settings", JsonArray().apply {
                            for (setting in module.settings) {
                                if (setting is Saving) {
                                    add(JsonObject().apply { add(setting.name, setting.write()) })
                                }
                            }
                        })
                    })
                }
            }
            configFile.bufferedWriter().use { it.write(gson.toJson(jsonArray)) }
        } catch (e: Exception) {
            println("Error saving config.\n${e.message}")
            logger.error("Error saving config.", e)
        }
    }
}
