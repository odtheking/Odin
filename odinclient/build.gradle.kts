import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "me.odinclient"

val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    shadowImpl(project(":"))
}

loom {
    log4jConfigs.from(file("log4j2.xml"))
    runConfigs {
        getByName("client") {
            programArgs("--tweakClass", "gg.essential.loader.stage0.EssentialSetupTweaker")
            programArgs("--mixin", "mixins.odinclient.json")
            isIdeConfigGenerated = true
        }
    }
    forge {
        mixinConfig("mixins.odinclient.json")
    }
    @Suppress("UnstableApiUsage")
    mixin.defaultRefmapName.set("mixins.odinclient.refmap.json")
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
            "FMLCorePluginContainsFMLMod" to true,
            "ForceLoadAsMod" to true,
            "MixinConfigs" to "mixins.odinclient.json",
            "ModSide" to "CLIENT",
            "TweakClass" to "gg.essential.loader.stage0.EssentialSetupTweaker",
            "TweakOrder" to "0"
        )
        dependsOn(shadowJar)
        enabled = false
    }

    remapJar {
        archiveBaseName = "OdinClient"
        input = shadowJar.get().archiveFile
    }

    shadowJar {
        destinationDirectory.set(layout.buildDirectory.dir("archiveJars"))
        archiveBaseName = "OdinClient"
        archiveClassifier = "dev"
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        configurations = listOf(shadowImpl)
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