import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("net.fabricmc.fabric-loom")
    kotlin("jvm")
    kotlin("plugin.compose")
    `maven-publish`
}

group = property("maven_group") as String
version = property("mod_version") as String

repositories {
    mavenCentral()
    google()
    maven("https://jitpack.io")
    maven("https://pkgs.dev.azure.com/djtheredstoner/DevAuth/_packaging/public/maven/v1")
    maven("https://maven.terraformersmc.com/")
    maven("https://api.modrinth.com/maven")
}

val bundled: Configuration by configurations.creating
configurations.implementation { extendsFrom(bundled) }

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    implementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    implementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")

    runtimeOnly("me.djtheredstoner:DevAuth-fabric:${property("devauth_version")}")

    property("commodore_version").let {
        implementation("com.github.stivais:Commodore:$it")
        include("com.github.stivais:Commodore:$it")
    }

    bundled("androidx.compose.runtime:runtime-desktop:${property("compose_runtime_version")}") {
        exclude("org.jetbrains.kotlin")
        exclude("org.jetbrains.kotlinx", "kotlinx-coroutines-core")
        exclude("org.jetbrains.kotlinx", "kotlinx-coroutines-core-jvm")
    }

    compileOnly("com.terraformersmc:modmenu:${property("modmenu_version")}")

    compileOnly("maven.modrinth:iris:${property("iris")}")
}

configurations.named("include") {
    dependencies.addAllLater(provider {
        bundled.resolvedConfiguration.resolvedArtifacts.map { project.dependencies.create(it.moduleVersion.id.toString()) }
    })
}

loom {
    accessWidenerPath = rootProject.file("src/main/resources/odin.accesswidener")
    runConfigs.named("client") {
        generateRunConfig.set(true)
        jvmArguments.addAll(
            "-Dmixin.debug.export=true",
            "-Ddevauth.enabled=true",
            "-Ddevauth.account=main",
            "-Dfabric.log.disableAnsi=false",
            "-XX:StackShadowPages=32",
            "-XX:+AllowEnhancedClassRedefinition",
            "-XX:+IgnoreUnrecognizedVMOptions", // AllowEnhancedClassRedefinition is only available on JBR
        )
    }

    runConfigs.named("server") {
        generateRunConfig.set(false)
    }
}

afterEvaluate {
    loom.runs.named("client") {
        jvmArguments.add("-javaagent:${configurations.compileClasspath.get().find { it.name.contains("sponge-mixin") }}")
    }
}

tasks {
    processResources {
        filesMatching("fabric.mod.json") {
            expand(getProperties())
        }
    }

    compileKotlin {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_25
            freeCompilerArgs.add("-Xlambdas=class") //Commodore
        }
    }

    compileJava {
        sourceCompatibility = "25"
        targetCompatibility = "25"
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Xlint:unchecked"))
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.odtheking"
            artifactId = "Odin"
            version = version
            from(components["java"])
        }
    }
}