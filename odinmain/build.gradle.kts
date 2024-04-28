import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
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

val lwjgl: Configuration by configurations.creating
val lwjglNative: Configuration by configurations.creating {
    isTransitive =true
}

val lwjglJar = tasks.create<ShadowJar>("lwjglJar") {
    group = "shadow"
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
    shadowImpl("gg.essential:loader-launchwrapper:1.1.3")
    compileOnly("gg.essential:essential-1.8.9-forge:12132+g6e2bf4dc5")
    //api("com.mojang:brigadier:1.0.18")
    shadowImpl("com.github.Stivais:Commodore:3f4a14b1cf") {
        exclude(module = "kotlin-stdlib-jdk8")
        exclude(module = "kotlin-reflect")
    }

    lwjgl("org.lwjgl:lwjgl:${lwjglVersion}")
    lwjgl("org.lwjgl:lwjgl-nanovg:${lwjglVersion}")
    lwjglNative("org.lwjgl:lwjgl:${lwjglVersion}:${lwjglNatives}")
    lwjglNative("org.lwjgl:lwjgl-nanovg:${lwjglVersion}:${lwjglNatives}")
    implementation(lwjglJar.outputs.files)
}

tasks {
    jar {
        dependsOn(lwjglJar)
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