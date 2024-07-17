package me.odinmain.lwjgl.plugin

import net.minecraft.launchwrapper.Launch
import net.minecraft.launchwrapper.LaunchClassLoader
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion

@MCVersion("1.8.9")
@IFMLLoadingPlugin.Name("Odin LWJGL Loading Plugin")
@IFMLLoadingPlugin.SortingIndex(5)
class LWJGLLoadingPlugin : IFMLLoadingPlugin {
    init {
        @Suppress("UNCHECKED_CAST")
        try {
            val fExceptions = LaunchClassLoader::class.java.getDeclaredField("classLoaderExceptions")
            fExceptions.isAccessible = true
            val exceptions = fExceptions[Launch.classLoader] as MutableSet<String>
            exceptions.remove("org.lwjgl.")
        } catch (e: Exception) {
            throw RuntimeException("e")
        }
    }

    override fun getASMTransformerClass(): Array<String> {
        return arrayOf("me.odinmain.lwjgl.plugin.LWJGLClassTransformer")
    }

    override fun getModContainerClass(): String? = null

    override fun getSetupClass(): String? = null

    override fun injectData(data: Map<String, Any>) {}

    override fun getAccessTransformerClass(): String? = null
}