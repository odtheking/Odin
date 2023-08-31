package me.odinclient.features.settings

/**
 * Creates a setting for the hud.
 *
 * @param name Name for the Setting
 * @param toggleable If you should be able to toggle it on and off. (Use this when you want the hud to be tied to a module.)
 * @param hidden If the setting should be hidden.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Hud(
    val name: String,
    val toggleable: Boolean,
    val hidden: Boolean = true
)
