plugins {
    kotlin("jvm") version "2.3.0"
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

    runtimeOnly("org.jetbrains.kotlin:kotlin-reflect:2.3.20-RC")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.10.2")

    api("org.http4k:http4k-core:6.31.1.0")
    implementation("org.http4k:http4k-format-jackson:6.31.1.0")

    implementation("io.netty:netty-all:4.2.10.Final")

    runtimeOnly("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.10.0")

    testImplementation(kotlin("test"))
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