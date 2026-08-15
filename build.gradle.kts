plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.serialization") version "2.3.0"
    `maven-publish`
}

group = "com.sharazan"
version = "1.0-SNAPSHOT"

val gitVersion: String = try {
    providers.exec {
        commandLine("git", "describe", "--tags", "--abbrev=0")
    }.standardOutput.asText.get().trim()
} catch (_: Exception) {
    "0.0.0-dev"
}

repositories {
    mavenCentral()
    mavenLocal()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.37hulk37:sharazan-core:1.0.2")
    implementation("com.github.37hulk37:sharazan-logging:1.0.1")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.11.0")
    runtimeOnly("org.jetbrains.kotlin:kotlin-reflect:2.3.20-RC")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.10.2")

    implementation("org.http4k:http4k-format-jackson:6.31.1.0")

    implementation("io.netty:netty-all:4.2.10.Final")

    runtimeOnly("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.10.0")

    testImplementation(kotlin("test"))
    // no SLF4J provider is wired up anywhere in the project yet (see CLAUDE.md) - without one,
    // MDC silently falls back to a no-op adapter, so tests asserting on MDC content need a real
    // one. slf4j-simple specifically hardcodes NOPMDCAdapter (it doesn't support MDC at all),
    // so this has to be logback-classic, not the simpler binding.
    testRuntimeOnly("ch.qos.logback:logback-classic:1.5.15")
}

kotlin {
    jvmToolchain(25)
}

publishing {
    publications {
        create<MavenPublication>("publish") {
            from(components["java"])
            groupId = "com.github.37hulk37"
            artifactId = "sharazan-${project.name}"
            version = gitVersion
        }
    }

    repositories {
        mavenLocal()
    }
}

tasks.test {
    useJUnitPlatform()
}
