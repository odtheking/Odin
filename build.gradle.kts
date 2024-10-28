plugins {
    idea
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
    kotlin("jvm") version "2.0.0-Beta1"
    id("net.kyori.blossom") version "1.3.1"
    id("gg.essential.loom") version "0.10.0.+"
    id("dev.architectury.architectury-pack200") version "0.1.3"
}

version = project.findProperty("version") as String

blossom {
    replaceToken("@VER@", version)
}

allprojects {
    repositories {
        mavenCentral()
        maven("https://repo.spongepowered.org/maven/")
        maven("https://repo.essential.gg/repository/maven-public/")
    }

    apply(plugin = "java")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "net.kyori.blossom")
    apply(plugin = "gg.essential.loom")
    apply(plugin = "dev.architectury.architectury-pack200")

    dependencies {
        minecraft("com.mojang:minecraft:1.8.9")
        mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
        forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

        implementation(kotlin("stdlib-jdk8"))

        compileOnly("com.github.NotEnoughUpdates:NotEnoughUpdates:2.4.0:all")
        implementation("com.github.Stivais:Commodore:3f4a14b1cf")

        annotationProcessor("org.spongepowered:mixin:0.8.5-SNAPSHOT")
        implementation("org.spongepowered:mixin:0.7.11-SNAPSHOT") { isTransitive = false }

        implementation("gg.essential:loader-launchwrapper:1.1.3")
        compileOnly("gg.essential:essential-1.8.9-forge:12132+g6e2bf4dc5")

        sourceSets.main {
            java.srcDir(file("$projectDir/src/main/kotlin"))
            output.setResourcesDir(sourceSets.main.flatMap { it.java.classesDirectory })
        }

        java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))
        kotlin.jvmToolchain(8)
    }
}