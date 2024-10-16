import dev.architectury.pack200.java.Pack200Adapter
import org.apache.commons.lang3.SystemUtils
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "me.odin"

val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    implementation(project(":"))
    shadowImpl(project(":"))
}

loom {
    log4jConfigs.from(rootProject.file("log4j2.xml"))
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
        pack200Provider.set(Pack200Adapter())
        mixinConfig("mixins.odin.json")
    }
    @Suppress("UnstableApiUsage")
    mixin.defaultRefmapName.set("mixins.odin.refmap.json")
}

tasks {
    processResources {
        inputs.property("version", version)

        filesMatching("mcmod.info") {
            expand(mapOf("version" to version))
        }
        dependsOn(compileJava)
    }

    jar {
        manifest.attributes(
            "FMLCorePlugin" to "me.odinmain.lwjgl.plugin.LWJGLLoadingPlugin",
            "FMLCorePluginContainsFMLMod" to true,
            "ForceLoadAsMod" to true,
            "MixinConfigs" to "mixins.odin.json",
            "TweakClass" to "org.spongepowered.asm.launch.MixinTweaker",
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
        configurations = listOf(
            project.configurations.getByName("shadowImpl"),
            project.configurations.getByName("lwjglNative")
        )
        exclude("META-INF/versions/**")
        mergeServiceFiles()
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        mustRunAfter(":processResources")
    }

    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}