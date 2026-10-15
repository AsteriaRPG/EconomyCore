plugins {
    kotlin("jvm") version "2.3.0"
    id("com.gradleup.shadow") version "9.3.0"
    `maven-publish`
}

group = "jp.asteria"
version = "0.3.0"

repositories {
    mavenCentral()
    maven("https://repo.powernukkitx.org/releases")
    maven("https://repo.opencollab.dev/maven-releases")
    maven("https://repo.opencollab.dev/maven-snapshots")
    maven {
        url = uri("https://maven.pkg.github.com/xxpmmperxx/DBConnectorN")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
    maven {
        url = uri("https://maven.pkg.github.com/xxpmmperxx/PlayersID")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    testImplementation(kotlin("test-junit"))
    compileOnly(kotlin("stdlib"))
    compileOnly("org.powernukkitx:server:2.0.0-SNAPSHOT")
    compileOnly("jp.asteria:db-connector-n:1.3.0")
    compileOnly("jp.asteria:players-id:1.3.1")
    compileOnly("org.jetbrains.exposed:exposed-core:1.0.0")
    compileOnly("org.jetbrains.exposed:exposed-dao:1.0.0")
    compileOnly("org.jetbrains.exposed:exposed-jdbc:1.0.0")
    compileOnly("org.jetbrains.exposed:exposed-kotlin-datetime:1.0.0")
}

kotlin {
    jvmToolchain(21)
}

configurations.testCompileClasspath { extendsFrom(configurations.compileOnly.get()) }
configurations.testRuntimeClasspath { extendsFrom(configurations.compileOnly.get()) }

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/xxpmmperxx/EconomyCore")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
    publications {
        register<MavenPublication>("gpr") {
            artifactId = "economy-core"
            from(components["java"])
        }
    }
}
