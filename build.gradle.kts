import dev.architectury.pack200.java.Pack200Adapter
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("dev.architectury.architectury-pack200") version "0.1.3"
    id("gg.essential.loom") version "0.10.0.+"
    id("net.kyori.blossom") version "1.3.1"
    kotlin("jvm") version "2.0.0-Beta1"
}

version = project.findProperty("version") as String

blossom {
    replaceToken("@VER@", version)
}

allprojects {
    repositories {
        mavenCentral()
        maven("https://repo.spongepowered.org/maven/")
        maven("https://jitpack.io")
    }

    apply(plugin = "dev.architectury.architectury-pack200")
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "gg.essential.loom")
    apply(plugin = "net.kyori.blossom")
    apply(plugin = "java")

    dependencies {
        minecraft("com.mojang:minecraft:1.8.9")
        mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
        forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

        implementation(kotlin("stdlib-jdk8"))
        implementation(kotlin("reflect"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")

        compileOnly("com.github.NotEnoughUpdates:NotEnoughUpdates:2.4.0:all")

        annotationProcessor("org.spongepowered:mixin:0.8.5-SNAPSHOT")
        implementation("org.spongepowered:mixin:0.7.11-SNAPSHOT") { isTransitive = false }

        implementation("com.mojang:brigadier:1.2.9")
        implementation("com.github.Stivais:Commodore:bea320fe0a")

        implementation("com.github.odtheking:odin-lwjgl:68de0d3e0b")
    }

    loom {
        log4jConfigs.from(file("log4j2.xml"))
        forge.pack200Provider.set(Pack200Adapter())
    }

    sourceSets.main {
        java.srcDir(file("$projectDir/src/main/kotlin"))
        output.setResourcesDir(sourceSets.main.flatMap { it.java.classesDirectory })
    }

    java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))
    kotlin.jvmToolchain(8)

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs += "-Xlambdas=class"
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        mustRunAfter(":processResources")
    }
}