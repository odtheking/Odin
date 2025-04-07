
group = "me.odin"

val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    shadowImpl(project(":"))
}

loom {
    runConfigs {
        getByName("client") {
            programArgs("--tweakClass", "gg.essential.loader.stage0.EssentialSetupTweaker")
            programArgs("--mixin", "mixins.odin.json")
            isIdeConfigGenerated = true
        }
    }
    forge {
        mixinConfig("mixins.odin.json")
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
        dependsOn(compileJava)
    }

    jar {
        manifest.attributes(
            "FMLCorePluginContainsFMLMod" to true,
            "ForceLoadAsMod" to true,
            "MixinConfigs" to "mixins.odin.json",
            "ModSide" to "CLIENT",
            "TweakClass" to "gg.essential.loader.stage0.EssentialSetupTweaker",
            "TweakOrder" to "0"
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
        configurations = listOf(shadowImpl)
        mergeServiceFiles()
    }
}