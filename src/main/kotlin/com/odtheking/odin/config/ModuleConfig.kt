@file:Suppress("unused")

package com.odtheking.odin.config

import com.google.gson.*
import com.odtheking.odin.OdinMod
import com.odtheking.odin.OdinMod.logger
import com.odtheking.odin.clickgui.settings.Saving
import com.odtheking.odin.features.Module
import java.io.File
import java.util.Locale

private const val ODIN_NAME = "Odin"
private const val ODIN_NAMESPACE = "odin"

private fun validateAddonNamespace(namespace: String): String {
    require(!namespace.equals(ODIN_NAMESPACE, ignoreCase = true)) {
        "Namespace from an addon can't be ${ODIN_NAMESPACE}"
    }
    return namespace.lowercase()
}

/**
 * # ModuleConfig
 *
 * This class handles saving Modules, and their settings, into a JSON format.
 */
class ModuleConfig internal constructor(file: File, val addonName: String = ODIN_NAME, val namespace: String = ODIN_NAMESPACE) {

    /**
     * Legacy constructor for Addons. (config/odin/addons/{fileName})
     *
     * The module namespace is used to distinguish between modules with the same name
     * from Odin itself and various addons. The namespace will be automatically converted to lowercase.
     *
     * The addon name is used to prefix name of the entries in the vanilla Controls menu.
     *
     * The module namespace will be the same name as the [fileName] in lowercase.
     * The addon name will be the same as the file name, with first character turned to uppercase.
     *
     * @deprecated Use the constructor taking three [String]s to supply a custom namespace and an addon name instead.
     */
    @Deprecated("Use the constructor taking fileName, addonName, and namespace instead.")
    constructor(fileName: String) : this(fileName, File(fileName).nameWithoutExtension.replaceFirstChar { it.titlecase(Locale.ROOT) }, File(fileName).nameWithoutExtension)

    /**
     * Main constructor for Addons. (config/odin/addons/{fileName})
     *
     * The module namespace is used to distinguish between modules with the same name
     * from Odin itself and various addons. The namespace will be automatically converted to lowercase.
     *
     * The addon name is used to prefix name of the entries in the vanilla Controls menu.
     */
    constructor(fileName: String, addonName: String, namespace: String) :
        this(
            File(OdinMod.configFile, "addons/$fileName"),
            addonName,
            validateAddonNamespace(namespace)
        )

    // key is module name in lowercase
    internal val modules: LinkedHashMap<String, Module> = linkedMapOf()

    private val file: File = file.apply {
        try {
            parentFile.mkdirs()
            createNewFile()
        } catch (e: Exception) {
            logger.error("Error initializing module config", e)
        }
    }

    /**
     * Loads configuration from file, into [modules].
     */
    fun load() {
        try {
            with(file.bufferedReader().use { it.readText() }) {
                if (isEmpty()) return

                val jsonArray = JsonParser.parseString(this).asJsonArray ?: return
                for (modules in jsonArray) {
                    val moduleObj = modules?.asJsonObject ?: continue
                    val module = this@ModuleConfig.modules[moduleObj.get("name").asString.lowercase()] ?: continue
                    if (moduleObj.get("enabled").asBoolean != module.enabled) module.toggle()
                    val settingObj = moduleObj.get("settings")?.takeIf { it.isJsonObject }?.asJsonObject?.entrySet() ?: continue
                    for ((key, value) in settingObj) {
                        (module.settings[key] as? Saving)?.apply { read(value ?: continue, gson) }
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Error initializing module config", e)
        }
    }

    /**
     * Saves configuration to files, from [modules].
     */
    fun save() {
        try {
            // reason doing this is better is that
            // using like a custom serializer leaves 'null' in settings that don't save
            // code looks hideous tho, but it fully works
            val jsonArray = JsonArray().apply {
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
            }
            file.bufferedWriter().use { it.write(gson.toJson(jsonArray)) }
        } catch (e: Exception) {
            logger.error("Error saving module config.", e)
        }
    }

    private companion object {
        private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    }

    override fun toString(): String {
        return "ModuleConfig(file=$file)"
    }
}
