package me.odinmain.features.settings

/**
 * Annotation to ensure a module is always registered to the Forge Eventbus, even while disabled
 *
 * @see me.odinmain.features.Module
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class AlwaysActive
