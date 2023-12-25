import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.RemapJarTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "me.odin"

loom {
    log4jConfigs.from(file("log4j2.xml"))
    launchConfigs {
        "client" {
            arg("--tweakClass", "cc.polyfrost.oneconfig.loader.stage0.LaunchWrapperTweaker")
            arg("--mixin", "mixins.odin.json")
        }
    }
    forge {
        pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
        mixinConfig("mixins.odin.json")
    }
    mixin {
        defaultRefmapName.set("mixins.odin.refmap.json")
    }
}

val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    shadowImpl(project(":odinmain")) {
        exclude(module = "kotlin-stdlib-jdk8")
    }

    compileOnly("cc.polyfrost:oneconfig-1.8.9-forge:0.2.0-alpha+")
    shadowImpl("cc.polyfrost:oneconfig-wrapper-launchwrapper:1.0.0-beta+")

    api("com.mojang:brigadier:1.0.18")
    shadowImpl("com.github.Stivais:Commodore:9342db41b1") {
        exclude(module = "kotlin-stdlib-jdk8")
        exclude(module = "kotlin-reflect")
    }
}

tasks {
    processResources {
        inputs.property("version", version)
        filesMatching("mcmod.info") {
            expand(
                mapOf(
                    "version" to version
                )
            )
        }
    }

    named<Jar>("jar") {
        archiveBaseName.set("odinclient")
        manifest.attributes.run {
            this["FMLCorePluginContainsFMLMod"] = "true"
            this["ForceLoadAsMod"] = "true"

            // If you don't want mixins, remove these lines
            this["TweakClass"] = "cc.polyfrost.oneconfig.loader.stage0.LaunchWrapperTweaker"
            this["MixinConfigs"] = "mixins.odin.json"
        }
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        dependsOn(shadowJar)
        enabled = false
    }

    named<RemapJarTask>("remapJar") {
        archiveBaseName.set(project.name)
        input.set(shadowJar.get().archiveFile)
    }

    named<ShadowJar>("shadowJar") {
        archiveBaseName.set(project.name)
        archiveClassifier.set("dev")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        configurations = listOf(shadowImpl)
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        mustRunAfter(":odinmain:processResources")
    }

    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}
