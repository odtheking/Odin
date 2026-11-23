@file:Suppress("unused")

package com.odtheking.odin.config

import com.google.gson.*
import com.odtheking.odin.OdinMod
import com.odtheking.odin.OdinMod.ioDispatcher
import com.odtheking.odin.OdinMod.logger
import com.odtheking.odin.OdinMod.scope
import com.odtheking.odin.clickgui.settings.Saving
import com.odtheking.odin.features.Module
import kotlinx.coroutines.launch
import java.io.File
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * # ModuleConfig
 *
 * This class handles saving Modules, and their settings, into a JSON format.
 */
class ModuleConfig internal constructor(file: File) {

    /**
     * Main constructor for Addons. (config/odin/addons/{fileName})
     */
    constructor(fileName: String) : this(File(OdinMod.configFile, "addons/$fileName"))

    // key is module name in lowercase
    internal val modules: HashMap<String, Module> = hashMapOf()

    private val file: File = file

    fun load() {
        scope.launch(ioDispatcher) {
            try {
                val text = if (file.isFile) file.readText() else ""
                if (text.isBlank()) null else JsonParser.parseString(text).asJsonArray
            } catch (e: Exception) {
                logger.error("Error loading module config $file", e)
                null
            }?.let { jsonArray ->
                for (modules in jsonArray) {
                    try {
                        val moduleObj = modules?.asJsonObject ?: continue
                        val module = this@ModuleConfig.modules[moduleObj.get("name").asString.lowercase()] ?: continue
                        if (moduleObj.get("enabled").asBoolean != module.enabled) module.toggle()
                        val settingObj = moduleObj.get("settings")?.takeIf { it.isJsonObject }?.asJsonObject?.entrySet() ?: continue
                        for ((key, value) in settingObj) {
                            (module.settings[key] as? Saving)?.apply { read(value ?: continue, gson) }
                        }
                    } catch (e: Exception) {
                        logger.error("Error loading module from config $file", e)
                    }
                }
            }
        }
    }

    fun save() {
        val json = try {
            // reason doing this is better is that
            // using like a custom serializer leaves 'null' in settings that don't save
            // code looks hideous tho, but it fully works
            gson.toJson(JsonArray().apply {
                for ((_, module) in modules) {
                    add(JsonObject().apply {
                        add("name", JsonPrimitive(module.name))
                        add("enabled", JsonPrimitive(module.enabled))
                        add("settings", JsonObject().apply {
                            for ((name, setting) in module.settings) {
                                if (setting is Saving) add(name, setting.write(gson))
                            }
                        })
                    })
                }
            })
        } catch (e: Exception) {
            return logger.error("Error saving module config $file", e)
        }

        scope.launch(ioDispatcher) {
            try {
                file.parentFile?.mkdirs()
                val temp = File(file.parentFile, "${file.name}.tmp")
                temp.writeText(json)
                try {
                    Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
                } catch (_: AtomicMoveNotSupportedException) {
                    Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING)
                }
            } catch (e: Exception) {
                logger.error("Error saving module config $file", e)
            }
        }
    }

    private companion object {
        private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    }

    override fun toString(): String {
        return "ModuleConfig(file=$file)"
    }
}