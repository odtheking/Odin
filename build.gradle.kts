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

    configurations {
        all {
            exclude(group = "tv.twitch", module = "twitch")
//            exclude(group = "oshi-project", module = "oshi-core")
//            exclude(group = "org.scala-lang")
//            exclude(group = "org.scala-lang.plugins")
//            exclude(group = "org.scala-lang.modules")
//            exclude(group = "org.slf4j", module = "slf4j-api")
//            exclude(group = "org.luaj")
//            exclude(group = "org.jline")
//            exclude(group = "org.fusesource")
//            exclude(group = "org.apache.httpcomponents") culprit of the error
//            exclude(group = "org.apache.commons")
//            exclude(group = "net.sf.trove4j")
//            exclude(group = "net.minecrell")
//            exclude(group = "org.kodein.di")
//            exclude(group = "org.kodein.type")
//            exclude(group = "com.paulscode")
//            exclude(group = "com.google.code.findbugs")
//            exclude(group = "ch.qos.logback")
//            exclude(group = "com.ibm.icu")
//            exclude(group = "java3d")
//            exclude(group = "info.bliki.wiki")
//            exclude(group = "jline")
//            exclude(group = "lzma")
//            exclude(group = "org.fusesource.jansi")
        }
    }
}

