plugins {
    kotlin("jvm") version "2.2.21"
}

group = "jp.asteria"
version = "1.0-SNAPSHOT"

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
}

dependencies {
    compileOnly("org.powernukkitx:server:2.0.0-SNAPSHOT")
    compileOnly("jp.asteria:db-connector-n:1.0.0")
}

kotlin {
    jvmToolchain(21)
}
