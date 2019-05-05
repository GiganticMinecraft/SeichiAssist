import org.apache.tools.ant.filters.ReplaceTokens
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
    getByName("main") { java.srcDir("src/main/java") }
    getByName("test") { java.srcDir("src/test/java") }
}

repositories {
    maven { url = URI("https://jitpack.io") }
    maven { url = URI("http://maven.sk89q.com/repo/") }
    maven { url = URI("http://repo.spring.io/plugins-release/") }
    maven { url = URI("https://repo.spongepowered.org/maven") }
    maven { url = URI("https://repo.maven.apache.org/maven2") }
    maven { url = URI("https://hub.spigotmc.org/nexus/content/repositories/snapshots")}
    maven { url = URI("https://oss.sonatype.org/content/repositories/snapshots")}
    mavenCentral()
}

val embed: Configuration by configurations.creating

configurations.implementation { extendsFrom(embed) }

dependencies {
    implementation(fileTree(mapOf("dir" to "localDependencies", "include" to arrayOf("*.jar"))))

    implementation("org.jetbrains:annotations:17.0.0")

    compile("org.eclipse.collections:eclipse-collections-api:9.2.0")
    compile("org.eclipse.collections:eclipse-collections:9.2.0")

    implementation("org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT")

    implementation("org.apache.commons:commons-lang3:3.9")

    implementation("commons-codec:commons-codec:1.12")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.4.2")
    testImplementation("junit:junit:4.4")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.4.2")

    embed("org.flywaydb:flyway-core:5.2.4")
}

tasks.processResources {
    filteringCharset = "UTF-8"
    from(sourceSets.main.get().resources.srcDirs) {
        include("**")

        val tokenReplacementMap = mapOf(
                "version" to project.version,
                "name" to project.rootProject.name
        )

        filter<ReplaceTokens>("tokens" to tokenReplacementMap)
    }
    from(projectDir) { include("LICENSE") }
}


tasks.withType(JavaCompile::class.java).all {
    this.options.encoding = "UTF-8"
}

tasks.jar {
    // Configurationをコピーしないと変更を行っているとみなされて怒られる
    val embedConfiguration = embed.copy()

    from(embedConfiguration.map { if (it.isDirectory) it else zipTree(it) })
}
