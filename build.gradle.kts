plugins {
    idea
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
    kotlin("jvm") version "2.0.0-Beta1"
    id("net.kyori.blossom") version "1.3.1"
}

version = project.findProperty("version") as String

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
}
