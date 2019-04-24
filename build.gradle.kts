import java.net.URI

plugins {
    java
    maven
    kotlin("jvm").version("1.3.30")
    id("nebula.dependency-lock").version("2.2.4")
}

group = "SeichiAssist"
version = "1.0.0-SNAPSHOT"
description = """ギガンティック☆整地鯖の独自要素を司るプラグイン"""

project.sourceSets {
    getByName("main") {
        java.srcDir("src/main/java")
    }
    getByName("test") {
        java.srcDir("src/test/java")
    }
}

repositories {
    maven { url = URI("https://jitpack.io") }
    maven { url = URI("http://maven.sk89q.com/repo/") }
    maven { url = URI("http://repo.spring.io/plugins-release/") }
    maven { url = URI("https://repo.spongepowered.org/maven") }
    maven { url = URI("https://repo.maven.apache.org/maven2") }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "localDependencies", "include" to arrayOf("*.jar"))))

    implementation("org.jetbrains:annotations:17.0.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.2")
    testImplementation("junit:junit:4.4")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.4.2")
}

