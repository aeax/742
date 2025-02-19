plugins {
    kotlin("jvm") version "2.1.10" apply false
}

allprojects {
    group = "org.darkan"
    version = findProperty("darkanVersion") as String

    repositories {
        mavenLocal()
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "java-library")

    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(23))
        }
    }

    dependencies {
        val implementation by configurations

        //Kotlin
        implementation("org.jetbrains.kotlin:kotlin-stdlib:${findProperty("ktVersion")}")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-common:${findProperty("ktVersion")}")
        implementation("org.jetbrains.kotlin:kotlin-scripting-common:${findProperty("ktVersion")}")
        implementation("org.jetbrains.kotlin:kotlin-scripting-jvm:${findProperty("ktVersion")}")
        implementation("org.jetbrains.kotlin:kotlin-scripting-jvm-host:${findProperty("ktVersion")}")
        implementation("org.jetbrains.kotlin:kotlin-main-kts:${findProperty("ktVersion")}")
        implementation("org.jetbrains.kotlin:kotlin-script-runtime:${findProperty("ktVersion")}")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")

        //Ktor
        implementation("io.ktor:ktor-server-core-jvm:${findProperty("ktorVersion")}")
        implementation("io.ktor:ktor-network-jvm:${findProperty("ktorVersion")}")
        implementation("io.ktor:ktor-server-netty-jvm:${findProperty("ktorVersion")}")
        implementation("io.ktor:ktor-server-websockets:${findProperty("ktorVersion")}")
        implementation("io.ktor:ktor-server-sessions:${findProperty("ktorVersion")}")
        implementation("io.ktor:ktor-server-content-negotiation:${findProperty("ktorVersion")}")
        implementation("io.ktor:ktor-serialization-kotlinx-json:${findProperty("ktorVersion")}")

        implementation("io.github.cdimascio:dotenv-kotlin:6.5.0")
        implementation("org.mongodb:mongodb-driver-kotlin-coroutine:5.3.1")
        implementation("com.github.jponge:lzma-java:1.3")

        implementation("org.mongodb:mongodb-driver-sync:5.3.1")
        implementation("org.mongodb:mongodb-driver-core:5.3.1")
        implementation("it.unimi.dsi:fastutil:8.5.15")
        implementation("com.trivago:fastutil-concurrent-wrapper:0.2.2")
    }
}