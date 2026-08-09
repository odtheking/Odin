package com.odtheking.odin.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.odtheking.odin.OdinMod.logger
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

object JsonResourceLoader {
    val defaultGson: Gson = GsonBuilder().setPrettyPrinting().create()

    inline fun <reified T> loadJson(resourcePath: String, defaultValue: T, ): T {
        return try {
            val stream = JsonResourceLoader::class.java.getResourceAsStream(resourcePath) ?: throw IllegalStateException("Resource not found: $resourcePath")
            InputStreamReader(stream, StandardCharsets.UTF_8).use { reader -> defaultGson.fromJson(reader, object : TypeToken<T>() {}.type) }
        } catch (e: Exception) {
            logger.error("Error loading $resourcePath", e)
            defaultValue
        }
    }
}


