package me.odinclient.features.settings

/**
 * Creates a setting for the hud.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Hud(
    val name: String,
    val toggle: Boolean,
    val hidden: Boolean
)
