package me.odinmain.lwjgl.plugin

import net.minecraft.launchwrapper.Launch
import net.minecraft.launchwrapper.LaunchClassLoader
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin

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

    override fun getModContainerClass(): String? {
        return null
    }

    override fun getSetupClass(): String? {
        return null
    }

    override fun injectData(data: Map<String, Any>) {}

    override fun getAccessTransformerClass(): String? {
        return null
    }
}