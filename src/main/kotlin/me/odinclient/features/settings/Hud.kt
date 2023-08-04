package me.odinclient.features.settings

/**
 * Creates a setting for the hud.
 *
 * @param name Name for the Setting
 * @param toggle If you should be able to toggle it on and off.
 * @param hidden If the setting should be hidden.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Hud(
    val name: String,
    val toggle: Boolean,
    val hidden: Boolean = true
)
