import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.RemapJarTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    java
    id("gg.essential.loom") version "0.10.0.+"
    id("dev.architectury.architectury-pack200") version "0.1.3"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("org.jetbrains.kotlin.jvm") version "2.0.0-Beta1"
    id("net.kyori.blossom") version "1.3.1"
}

allprojects {
    apply(plugin = "idea")
    apply(plugin = "java")
    apply(plugin = "gg.essential.loom")
    apply(plugin = "dev.architectury.architectury-pack200")
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "net.kyori.blossom")

    // set version inside gradle.properties
    version = project.findProperty("version") as String

    java {
        toolchain.languageVersion = JavaLanguageVersion.of(8)
    }

    kotlin {
        jvmToolchain {
            languageVersion = JavaLanguageVersion.of(8)
        }
    }

    sourceSets.main {
        java.srcDir(file("$projectDir/src/main/kotlin"))
        output.setResourcesDir(sourceSets.main.flatMap { it.java.classesDirectory })
    }

    repositories {
        mavenCentral()
        maven("https://repo.spongepowered.org/maven/")
        maven("https://repo.essential.gg/repository/maven-public/")
        maven("https://repo.polyfrost.cc/releases")
    }

    dependencies {
        // forge
        minecraft("com.mojang:minecraft:1.8.9")
        mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
        forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

        // mixins
        annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
        compileOnly("org.spongepowered:mixin:0.8.5")

        // kotlin
        implementation(kotlin("stdlib-jdk8"))
    }
}

repositories {
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
}

val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    runtimeOnly("me.djtheredstoner:DevAuth-forge-legacy:1.1.0")
    compileOnly("cc.polyfrost:oneconfig-1.8.9-forge:0.2.0-alpha+")
    shadowImpl("cc.polyfrost:oneconfig-wrapper-launchwrapper:1.0.0-beta+")
}

tasks {
    named<Jar>("jar") {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        enabled = false
    }

    named<RemapJarTask>("remapJar") {
        archiveBaseName.set(project.name)
        input.set(shadowJar.get().archiveFile)
        enabled = false
    }

    named<ShadowJar>("shadowJar") {
        archiveBaseName.set(project.name)
        archiveClassifier.set("dev")
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        configurations = listOf(shadowImpl)
        enabled = false
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
