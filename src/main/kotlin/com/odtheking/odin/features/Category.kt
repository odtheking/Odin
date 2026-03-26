package com.odtheking.odin.features

import com.odtheking.odin.features.Category.Companion.categories

@ConsistentCopyVisibility
data class Category private constructor(val name: String) {
    companion object {

        /**
         * Map containing all the categories, with the key being the name.
         */
        val categories: LinkedHashMap<String, Category> = linkedMapOf()

        @JvmField
        val DUNGEON = custom(name = "Dungeon")
        @JvmField
        val BOSS = custom(name = "Boss")
        @JvmField
        val RENDER = custom(name = "Render")
        @JvmField
        val SKYBLOCK = custom(name = "Skyblock")
        @JvmField
        val NETHER = custom(name = "Nether")

        /**
         * Returns a category with name provided.
         *
         * If a category with the same name has already been made, it won't reallocate.
         * Otherwise, it will be added to [categories].
         */
        fun custom(name: String): Category {
            return categories.getOrPut(name) { Category(name) }
        }
    }
}