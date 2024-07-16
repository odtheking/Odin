import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.apache.commons.lang3.SystemUtils
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("gg.essential.loom") version "0.10.0.+"
    id("dev.architectury.architectury-pack200") version "0.1.3"
}

group = "me.odin"

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

val lwjglNatives = when {
    arrayOf("Linux", "SunOS", "Unit").any { System.getProperty("os.name")!!.startsWith(it) } -> {
        val arch = System.getProperty("os.arch")!!
        when {
            arrayOf("arm", "aarch64").any { arch.startsWith(it) } ->
                "natives-linux${if (arch.contains("64") || arch.startsWith("armv8")) "-arm64" else "-arm32"}"
            arch.startsWith("ppc") -> "natives-linux-ppc64le"
            arch.startsWith("riscv") -> "natives-linux-riscv64"
            else -> "natives-linux"
        }
    }
    arrayOf("Mac OS X", "Darwin").any { System.getProperty("os.name")!!.startsWith(it) } -> {
        val arch = System.getProperty("os.arch")!!
        "natives-macos${if (arch.startsWith("aarch64")) "-arm64" else ""}"
    }
    arrayOf("Windows").any { System.getProperty("os.name")!!.startsWith(it) } -> {
        val arch = System.getProperty("os.arch")!!
        if (arch.contains("64")) "natives-windows${if (arch.startsWith("aarch64")) "-arm64" else ""}"
        else "natives-windows-x86"
    }
    else -> throw Error("Unrecognized or unsupported platform. Please set \"lwjglNatives\" manually")
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

    implementation(project(mapOf("path" to ":odinmain")))
    shadowImpl(project(":odinmain")) {
        exclude(module = "kotlin-stdlib-jdk8")
    }

    shadowImpl("org.spongepowered:mixin:0.7.11-SNAPSHOT") { isTransitive = false }
    annotationProcessor("org.spongepowered:mixin:0.8.5-SNAPSHOT")

    shadowImpl("com.github.Stivais:Commodore:3f4a14b1cf") {
        exclude(module = "kotlin-stdlib-jdk8")
        exclude(module = "kotlin-reflect")
    }

    lwjgl("org.lwjgl:lwjgl:${lwjglVersion}")
    lwjgl("org.lwjgl:lwjgl-nanovg:${lwjglVersion}")
    lwjgl("org.lwjgl:lwjgl-stb:${lwjglVersion}")
    lwjglNative("org.lwjgl:lwjgl:${lwjglVersion}:${lwjglNatives}")
    lwjglNative("org.lwjgl:lwjgl-nanovg:${lwjglVersion}:${lwjglNatives}")
    lwjglNative("org.lwjgl:lwjgl-stb:${lwjglVersion}:${lwjglNatives}")
    shadowImpl(lwjglJar.outputs.files)
}

loom {
    log4jConfigs.from(file("log4j2.xml"))
    launchConfigs {
        getByName("client") {
            property("mixin.debug", "true")
            arg("--tweakClass", "org.spongepowered.asm.launch.MixinTweaker")
        }
    }
    runConfigs {
        getByName("client") {
            if (SystemUtils.IS_OS_MAC_OSX) vmArgs.remove("-XstartOnFirstThread")
        }
        remove(getByName("server"))
    }
    forge {
        pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
        mixinConfig("mixins.odin.json")
    }
    @Suppress("UnstableApiUsage")
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
            "TweakClass" to "org.spongepowered.asm.launch.MixinTweaker",
        )
        dependsOn(lwjglJar)
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
        configurations = listOf(shadowImpl, lwjglNative)
        exclude("META-INF/versions/**")
        mergeServiceFiles()
        fun relocate(name: String) = relocate(name, "odin.deps.$name") // relocate lwjgl
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
