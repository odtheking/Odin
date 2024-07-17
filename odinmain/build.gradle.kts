import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("gg.essential.loom") version "0.10.0.+"
    id("dev.architectury.architectury-pack200") version "0.1.3"
}

blossom {
    replaceToken("@VER@", version)
}

group = "me.odinmain"

val lwjgl: Configuration by configurations.creating
val lwjglNative: Configuration by configurations.creating {
    isTransitive =true
}

val lwjglJar = tasks.create<ShadowJar>("lwjglJar") {
    group = "shadow"
    destinationDirectory.set(layout.buildDirectory.dir("archiveJars"))
    archiveClassifier.set("lwjgl")
    configurations = listOf(lwjgl)
    exclude("META-INF/versions/**")
    exclude("**/module-info.class")
    exclude("**/package-info.class")
    relocate("org.lwjgl", "org.lwjgl3") {
        include("org.lwjgl.PointerBuffer")
        include("org.lwjgl.BufferUtils")
    }
}

val lwjglVersion = "3.3.3"

val lwjglNatives: String = run {
    val arch = System.getProperty("os.arch")!!
    if (arch.contains("64")) {
        "natives-windows${if (arch.startsWith("aarch64")) "-arm64" else ""}"
    } else {
        "natives-windows-x86"
    }
}

sourceSets.main {
    java.srcDir(file("$projectDir/src/main/kotlin"))
    output.setResourcesDir(sourceSets.main.flatMap { it.java.classesDirectory })
    runtimeClasspath += configurations.getByName("lwjglNative")
}

val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

    implementation(kotlin("stdlib-jdk8"))
    shadowImpl("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")
    compileOnly("com.github.notenoughupdates:notenoughupdates:v2.1.0:all")

    shadowImpl("com.github.Stivais:Commodore:3f4a14b1cf") {
        exclude(module = "kotlin-stdlib-jdk8")
    }

    lwjgl("org.lwjgl:lwjgl:${lwjglVersion}")
    lwjgl("org.lwjgl:lwjgl-nanovg:${lwjglVersion}")
    lwjgl("org.lwjgl:lwjgl-stb:${lwjglVersion}")
    lwjglNative("org.lwjgl:lwjgl:${lwjglVersion}:${lwjglNatives}")
    lwjglNative("org.lwjgl:lwjgl-nanovg:${lwjglVersion}:${lwjglNatives}")
    lwjglNative("org.lwjgl:lwjgl-stb:${lwjglVersion}:${lwjglNatives}")
    shadowImpl(lwjglJar.outputs.files)
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))
kotlin.jvmToolchain(8)
