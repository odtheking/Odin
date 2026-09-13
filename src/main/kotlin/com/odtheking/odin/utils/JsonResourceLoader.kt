package com.odtheking.odin.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.odtheking.odin.OdinMod.logger
import com.odtheking.odin.OdinMod.mc
import net.minecraft.resources.Identifier

object JsonResourceLoader {
    val defaultGson: Gson = GsonBuilder().setPrettyPrinting().create()

    inline fun <reified T> loadJson(identifier: Identifier): T? {
        return try {
            mc.resourceManager.getResource(identifier).get().openAsReader().use { reader -> defaultGson.fromJson(reader, object : TypeToken<T>() {}.type) }
        } catch (e: Exception) {
            logger.error("Error loading $identifier", e)
            null
        }
    }
}


