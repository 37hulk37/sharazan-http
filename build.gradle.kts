plugins {
    kotlin("jvm") version "2.3.0"
    kotlin("plugin.serialization") version "2.3.0"
    `maven-publish`
}

group = "com.sharazan"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("com.sharazan:core:1.0-SNAPSHOT")
    implementation("com.sharazan:logging:1.0-SNAPSHOT")

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
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
        }
    }

    repositories {
        mavenLocal()
    }
}

tasks.test {
    useJUnitPlatform()
}