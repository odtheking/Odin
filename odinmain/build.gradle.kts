plugins {
    id("gg.essential.loom") version "0.10.0.+"
    id("dev.architectury.architectury-pack200") version "0.1.3"
}

blossom {
    replaceToken("@VER@", version)
}

group = "me.odinmain"

sourceSets.main {
    java.srcDir(file("$projectDir/src/main/kotlin"))
    output.setResourcesDir(sourceSets.main.flatMap { it.java.classesDirectory })
}

val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

    implementation(kotlin("stdlib-jdk8"))
    //shadowImpl("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")
    compileOnly("com.github.NotEnoughUpdates:NotEnoughUpdates:2.4.0:all")

    shadowImpl("gg.essential:loader-launchwrapper:1.1.3")
    compileOnly("gg.essential:essential-1.8.9-forge:12132+g6e2bf4dc5")

    shadowImpl("com.github.Stivais:Commodore:3f4a14b1cf") {
        exclude(module = "kotlin-stdlib-jdk8")
    }
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))
kotlin.jvmToolchain(8)