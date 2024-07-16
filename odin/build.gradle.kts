import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    java
    id("gg.essential.loom") version "0.10.0.+"
    id("dev.architectury.architectury-pack200") version "0.1.3"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    kotlin("jvm") version "2.0.0-Beta1"

    // allows to automatically update version in mcmod.info
    id("net.kyori.blossom") version "1.3.1"
}

group = "me.odin"

sourceSets.main {
    java.srcDir(file("$projectDir/src/main/kotlin"))
    output.setResourcesDir(sourceSets.main.flatMap { it.java.classesDirectory })
}

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/maven/")
    maven("https://repo.essential.gg/repository/maven-public/")
}

val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    implementation(project(mapOf("path" to ":odinmain")))
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

    implementation(kotlin("stdlib-jdk8"))

    shadowImpl(project(":odinmain")) {
        exclude(module = "kotlin-stdlib-jdk8")
    }

    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
    compileOnly("org.spongepowered:mixin:0.8.5")

    compileOnly("gg.essential:loader-launchwrapper:1.1.3")
    implementation("gg.essential:essential-1.8.9-forge:12132+g6e2bf4dc5")

    shadowImpl("com.github.Stivais:Commodore:3f4a14b1cf") {
        exclude(module = "kotlin-stdlib-jdk8")
        exclude(module = "kotlin-reflect")
    }
}

configurations {
    all {
        exclude(group = "tv.twitch", module = "twitch")
        exclude(group = "oshi-project", module = "oshi-core")
        exclude(group = "org.scala-lang")
        exclude(group = "org.scala-lang.plugins")
        exclude(group = "org.scala-lang.modules")
        exclude(group = "org.slf4j", module = "slf4j-api")
        exclude(group = "org.luaj")
        exclude(group = "org.jline")
        exclude(group = "org.fusesource")
        exclude(group = "org.apache.httpcomponents")
        exclude(group = "org.apache.commons")
        exclude(group = "net.sf.trove4j")
        exclude(group = "net.sf.jopt-simple")
        exclude(group = "net.minecrell")
        exclude(group = "org.kodein.di")
        exclude(group = "org.kodein.type")
        exclude(group = "org.ow2.asm")
        exclude(group = "com.paulscode")
        exclude(group = "com.google.guava")
        exclude(group = "com.google.code.findbugs")
        exclude(group = "ch.qos.logback")
        exclude(group = "com.ibm.icu")
        exclude(group = "java3d")
        exclude(group = "info.bliki.wiki")
        exclude(group = "jline")
        exclude(group = "lzma")
        exclude(group = "org.fusesource.jansi")
    }
}

loom {
    log4jConfigs.from(file("log4j2.xml"))
    runConfigs {
        getByName("client") {
            programArgs("--tweakClass", "gg.essential.loader.stage0.EssentialSetupTweaker")
            programArgs("--mixin", "mixins.odin.json")
            isIdeConfigGenerated = true
        }
    }
    forge {
        pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
        mixinConfig("mixins.odin.json")
    }
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
        archiveBaseName = "Odin"
        input = shadowJar.get().archiveFile
    }

    shadowJar {
        destinationDirectory.set(layout.buildDirectory.dir("archiveJars"))
        archiveBaseName = "Odin"
        archiveClassifier = "dev"
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        configurations = listOf(shadowImpl)
        mergeServiceFiles()
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

java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))

kotlin.jvmToolchain(8)
