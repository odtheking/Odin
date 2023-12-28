import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    idea
    java
    id("gg.essential.loom") version "0.10.0.+"
    id("dev.architectury.architectury-pack200") version "0.1.3"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    kotlin("jvm") version "2.0.0-Beta1"
    id("net.kyori.blossom") version "1.3.1"
}

group = "me.odinmain"

blossom {
    replaceToken("@VER@", version)
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

val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

    implementation(kotlin("stdlib-jdk8"))

    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
    compileOnly("org.spongepowered:mixin:0.8.5")

    compileOnly("cc.polyfrost:oneconfig-1.8.9-forge:0.2.2-alpha+")
    shadowImpl("cc.polyfrost:oneconfig-wrapper-launchwrapper:1.0.0-beta+")

    api("com.mojang:brigadier:1.0.18")

    shadowImpl("com.github.Stivais:Commodore:9342db41b1") {
        exclude(module = "kotlin-stdlib-jdk8")
        exclude(module = "kotlin-reflect")
    }
}

tasks {
    jar {
        dependsOn(shadowJar)
        enabled = false
    }

    remapJar {
        archiveBaseName = "odinmain"
        input = shadowJar.get().archiveFile
    }

    shadowJar {
        archiveBaseName = "odinmain"
        archiveClassifier = "dev"
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        configurations = listOf(shadowImpl)
        mergeServiceFiles()
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))

kotlin.jvmToolchain(8)
