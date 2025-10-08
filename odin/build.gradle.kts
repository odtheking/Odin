import org.apache.commons.lang3.SystemUtils

group = "me.odin"
val transformerFile = file("src/main/resources/accesstransformer.cfg")

val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    shadowImpl(project(":"))
}

loom {
    launchConfigs {
        getByName("client") {
            property("mixin.debug", "true")
            arg("--tweakClass", "org.spongepowered.asm.launch.MixinTweaker")
        }
    }
    runConfigs {
        getByName("client") {
            property("fml.coreMods.load", "me.odinmain.lwjgl.plugin.LWJGLLoadingPlugin")
            if (SystemUtils.IS_OS_MAC_OSX) vmArgs.remove("-XstartOnFirstThread")
        }
        remove(getByName("server"))
    }
    forge {
        mixinConfig("mixins.odin.json")
        if (transformerFile.exists()) {
            println("Installing access transformer")
            accessTransformer(transformerFile)
        }
    }
    @Suppress("UnstableApiUsage")
    mixin.defaultRefmapName.set("mixins.odin.refmap.json")
}

tasks {
    processResources {
        inputs.property("version", version)

        filesMatching("mcmod.info") {
            expand(inputs.properties)
        }

        rename("accesstransformer.cfg", "META-INF/od_at.cfg")

        dependsOn(compileJava)
    }

    jar {
        manifest.attributes(
            "FMLCorePlugin" to "me.odinmain.lwjgl.plugin.LWJGLLoadingPlugin",
            "FMLCorePluginContainsFMLMod" to true,
            "ForceLoadAsMod" to true,
            "MixinConfigs" to "mixins.odin.json",
            "TweakClass" to "org.spongepowered.asm.launch.MixinTweaker",
            "FMLAT" to "od_at.cfg"
        )
        dependsOn(shadowJar)
        enabled = false
    }

    remapJar {
        archiveBaseName.set("Odin")
        input.set(shadowJar.get().archiveFile)
    }

    shadowJar {
        destinationDirectory.set(layout.buildDirectory.dir("archiveJars"))
        archiveBaseName.set("Odin")
        archiveClassifier.set("dev")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        configurations = listOf(project.configurations.getByName("shadowImpl"))
        exclude("META-INF/versions/**")
        mergeServiceFiles()
    }
}